package server.services;

import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.data.IUser;
import server.data.NodeType;
import server.exceptions.CustomException;
import server.data.Node;
import server.models.NodeFtpRequest;
import server.models.NodeWebRequest;
import server.models.ZipRequest;
import server.repositories.IFileRepo;
import server.repositories.INodeRepo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static server.exceptions.Message.*;
import static server.tools.MimeTypesMap.getMimeType;

@Service
public class NodeService implements INodeService{
    private final Logger logger = Logger.getLogger(NodeService.class.getName());

    @Autowired
    private INodeRepo nodeRepo;
    @Autowired
    private IFileRepo fileRepo;

    /********** for nodes from web *******************/
    @Override
    public List<Node> getNodes(IUser user, String parentId, int page, int size, String sortField, String direction, List<String> status, String search) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        return nodeRepo.findAllByOwnerIdAndParentIdAndNameContains(user.getId(), parentId, search, pageRequest)
                .stream()
                .map(this::addParent) // adding parents
                .collect(Collectors.toList());
    }

    @Override
    public Node getNodeById(IUser user, String nodeId) throws CustomException {
        Node node = nodeRepo.findByIdAndOwnerId(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        return addChildren(addParent(node));
    }

    @Override
    public Node getNodeContent(IUser user, String nodeId) throws CustomException {
        Node node = nodeRepo.findByIdAndOwnerId(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        node = addContentWithFail(node);
        return addParent(node);
    }

    @Override
    public Node createZipNode(IUser user, ZipRequest zipRequest) throws CustomException {
        String parentId = zipRequest.getParentId();
        // if parent exist update path of node
        Node parentNode = null;
        List<String> path = new ArrayList<>();
        if(parentId != null){
            parentNode = nodeRepo.findByIdAndOwnerId(parentId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
            zipRequest.setParentId(parentId);
            path = parentNode.getPath();
        }
        path.add(zipRequest.getName());
        // creating zip file
        File zipFile = zipNodesByIds(user, zipRequest.getNodesId(), zipRequest.getName());
        // add zip file to zip request in order to make some operation for us
        // like file size and contentType, ...
        zipRequest.setFile(zipFile);
        // creating node
        Node zipNode = new Node(zipRequest.getName(), user.getId(), parentId, path);
        zipNode.setType(NodeType.FILE);
        zipNode.setOriginalName(zipFile.getName());
        zipNode.setExtension(zipRequest.getExtension());
        zipNode.getFields().putAll(zipRequest.getFields());
        zipNode = fixNameConflict(zipNode);
        // saving zip file content to database
        saveFile(zipNode, zipRequest.getFileContent(), zipRequest.getContentType(), zipRequest.getFileSize());
       // saving node
        zipNode = nodeRepo.insert(zipNode);
        return zipNode;
    }

    @Override
    public Node insertNode(IUser user, NodeWebRequest nodeWebRequest) throws CustomException {
        String parentId = nodeWebRequest.getParentId();
        Node parentNode = null;
        List<String> path = new ArrayList<>();
        if(parentId != null){
            parentNode = nodeRepo.findByIdAndOwnerId(parentId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
            nodeWebRequest.setParentId(parentId);
            path = parentNode.getPath();
        }
        path.add(nodeWebRequest.getName());
        nodeWebRequest.setPath(path);
        Node node = NodeWebRequest.toNode(nodeWebRequest);
        node.setOwnerId(user.getId());
        saveFile(node, nodeWebRequest.getFileContent(), nodeWebRequest.getContentType(), nodeWebRequest.getFileSize());
        try {
            // fix name
            node = fixNameConflict(node);
            Node createdNode = nodeRepo.insert(node);
            return addParent(createdNode); // just more details
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), user.getId(), parentId);
        }catch (Exception e){
            throw new CustomException(e, UNKNOWN_EXCEPTION, node.getName(), user.getId(), parentId);
        }
    }

    @Override
    public Node updateNode(IUser user, String nodeId, NodeWebRequest nodeWebRequest) throws CustomException {
        Node node = nodeRepo.findByIdAndOwnerId(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, nodeId));
        String parentId = nodeWebRequest.getParentId();
        Node parentNode = null;
        List<String> path = new ArrayList<>();
        if(parentId != null){
            parentNode = nodeRepo.findByIdAndOwnerId(parentId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
            nodeWebRequest.setParentId(parentId);
            path = parentNode.getPath();

        }
        path.add(nodeWebRequest.getName());
        nodeWebRequest.setPath(path);
        NodeWebRequest.updateNodeWith(node, nodeWebRequest);
        saveFile(node, nodeWebRequest.getFileContent(), nodeWebRequest.getContentType(), nodeWebRequest.getFileSize());
        try {
            Node createdNode = nodeRepo.save(node);
            return addParent(createdNode);
        }catch (org.springframework.dao.DuplicateKeyException e){// catch duplicate key exception
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), user.getId(), parentId);
        }catch (Exception e){
            throw new CustomException(e, UNKNOWN_EXCEPTION, node.getName(), user.getId(), parentId);
        }
    }

    @Override
    public int deleteNode(IUser user, String nodeId) throws CustomException {
        // just counting before and after deleting
        int count = nodeRepo.countById(nodeId);
        nodeRepo.deleteById(nodeId);
        return count - nodeRepo.countById(nodeId);
    }

    @Override
    public List<Node> getRootNodes(IUser user){
        return nodeRepo.findByOwnerIdAndParentIdNull(user.getId());
    }

    /********** for nodes from ftp server *******************/
    @Override
    public Node insertNode(NodeFtpRequest nodeFtpRequest) throws CustomException {
        IUser user = nodeFtpRequest.getUser();
        // build file node and add it to list
        Node node = getNodeFromPath(user, nodeFtpRequest.getPath(), true);
        if(node != null){
            NodeFtpRequest.updateNodeWith(node, nodeFtpRequest);
            saveFile(node, nodeFtpRequest.getFileContent(), nodeFtpRequest.getContentType(), nodeFtpRequest.getFileSize());
            node = fixNameConflict(node);
            node = nodeRepo.save(node);
            logger.log(Level.INFO, node.toString());
            return node;
        }
        throw new CustomException(ERROR_WHILE_SAVING_FILE, nodeFtpRequest.getName());
    }

    @Override
    public Node updateNode(NodeFtpRequest nodeFtpRequest) throws CustomException {
        return insertNode(nodeFtpRequest);
    }

    @Override
    public int deleteNode(NodeFtpRequest nodeFtpRequest) throws CustomException {
        IUser user = nodeFtpRequest.getUser();
        Node node = getNodeFromPath(user, nodeFtpRequest.getPath(), false);
        if(node != null){
            deleteNode(user, node.getId());
            return 1;
        }
        return 0;
    }

    /********** compressing *****************/
    public File zipNodesByIds(IUser user, List<String> nodesId, String zipName) throws CustomException {
        if(nodesId==null)
            throw new CustomException(NULL_PARAMS, "nodesId");
        List<Node> children = new ArrayList<>();
        for (String id: nodesId){
            Node node = getNodeById(user, id);
            if(node!=null)
                children.add(node);
        }
        children = children.stream()
                .filter(nodeNew -> NodeType.FILE.equals(nodeNew.getType()))
                .map(this::addContent)
                .collect(Collectors.toList());
        if(children.size()==0)
            throw new CustomException(EMPTY_RESULT, nodesId);
        return zipNodes(user, children, zipName);
    }

    private File zipNodes(IUser user, List<Node> nodes, String zipName) throws CustomException {
        try {
            if(zipName==null)
                zipName = nodes.get(0).getName();
            zipName = zipName.replaceAll(".zip","");
            String userDir = user.getUsername()+"_"+user.getId();
            Path path = Paths.get("uploads",userDir,user.getHomeDirectory(),zipName+".zip").normalize();
            if (!Files.exists(path)) {
                path = Files.createTempFile(userDir+"_","_"+zipName+".zip");//Files.createFile(path);
            }
            List<ZipEntry> entries = new ArrayList<>();
            try(
                    FileOutputStream fos = new FileOutputStream(path.toFile());
                    ZipOutputStream zos = new ZipOutputStream(fos)
            ){
                zos.setMethod(ZipOutputStream.DEFLATED);
                for (Node node : nodes) {
                    try {
                        String entryName = node.getName();
                        if(entryName.contains(".")){// validate extension
                            String extension = FilenameUtils.getExtension(node.getName());
                            if(getMimeType(extension)==null)
                                entryName = entryName.substring(0, entryName.lastIndexOf(".")).replaceAll("\\.","_");
                        }
                        if(!entryName.contains(".")){
                            entryName +='.'+node.getExtension();
                        }
                        if(!entryName.contains(".")){
                            entryName +='.'+FilenameUtils.getExtension(node.getOriginalName());
                        }
                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zos.putNextEntry(zipEntry);
                        byte[] bytes = new byte[(int) node.getFileSize()];
                        try(BufferedInputStream bis = new BufferedInputStream(node.getContent());) {
                            int readBytes = bis.read(bytes);
                            if(readBytes>0)
                                zos.write(bytes, 0, bytes.length);
                        }finally {
                            zos.closeEntry();
                            entries.add(zipEntry);
                        }
                    } catch (Exception e) {
                        logger.log(SEVERE, String.format("Error inserting file in the zip : %s, Error: %s %n", node.getName(), e.getMessage()));
                    }
                }

            }
            if(entries.size()==0)
                throw new CustomException(ERROR_WHILE_ZIPPING_FILE, path);
            return path.toFile();
        } catch (FileNotFoundException e) {
            logger.log(SEVERE, String.format("Error while zipping, Error: %s %n", e.getMessage()));
            throw new CustomException(e, ZIP_FILE_PATH_NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            logger.log(SEVERE, String.format("Error while zipping, Error: %s %n",e.getMessage()));
            throw new CustomException(e, ERROR_WHILE_ZIPPING_FILE, e.getMessage());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Error while zipping nodes Error: %s %n", e.getMessage()));
            throw new CustomException(e, UNKNOWN_EXCEPTION, e.getMessage());
        }
    }

    /********** Tools *******************/
    public Node saveFile(Node node, InputStream inputStream, String contentType, long fileSize) throws CustomException {
        if(inputStream!=null && fileSize>0){
            node.setType(NodeType.FILE);
            node.setContentType(contentType);
            node.setFileSize(fileSize);
            ObjectId fileId = fileRepo.saveFile(inputStream, node);
            node.setFileId(fileId.toString());
        }
        return node;
    }

    public Node getNodeFromPath(IUser user, List<String> pathParts, boolean allowInsert){
        //List<NodeNew> pathNodes = new ArrayList<>();
        String parentId = null;
        Node createdNode = null;
        for (int i = 0 ; i < pathParts.size(); i++){
            String nodeName = pathParts.get(i);
            Node foundedNode = getByNameAndOwnerIdAndParentId(nodeName, user.getId(), parentId);
            if(foundedNode==null){
                if(!allowInsert)
                    return null;
                List<String> path = i==0 ? Collections.singletonList(nodeName) : pathParts.subList(0,i);
                createdNode = new Node(nodeName, user.getId(), parentId, path);
                createdNode = nodeRepo.insert(createdNode);
            }else{
                 createdNode = foundedNode;
            }
            parentId = createdNode.getId();
            //pathNodes.add(createdNode);
        }
        return createdNode;
    }

    public Node getByNameAndOwnerIdAndParentId(String name, String ownerId, String parentId){
        if(parentId!=null)
            return nodeRepo.findByNameAndOwnerIdAndParentIdNotNull(name, ownerId, parentId);
        return nodeRepo.findByNameAndOwnerIdAndParentIdNull(name, ownerId);
    }

    public Node addChildren(Node node){
        // add children if exist
        List<Node> children = nodeRepo.findByOwnerIdAndParentId(node.getOwnerId(), node.getId());
        node.setChildren(children);
        return node;
    }

    public Node addParent(Node node){
        // add parent if exist
        if(node.getParentId()==null)
            return node;
        Node parent = nodeRepo.findByIdAndOwnerId(node.getParentId(), node.getOwnerId()).orElse(null);
        node.setParent(parent);
        return node;
    }

    public Node addContent(Node node){
        try {
            return addContentWithFail(node);
        } catch (Exception e) {
            logger.log(WARNING, e.getMessage());
        }
        return node;
    }

    public Node addContentWithFail(Node node) throws CustomException {
        if(node.getFileId()==null)
            return node;
        try {
            InputStream inputStream = fileRepo.getResource(node.getFileId()).getInputStream();
            node.setContent(inputStream);
        } catch (IOException e) {
            throw new CustomException(e, ERROR_WHILE_READING_FILE_CONTENT, node.getName());
        }
        return node;
    }

    public boolean existWithSameKey(Node node){
        return getByNameAndOwnerIdAndParentId(node.getName(), node.getOwnerId(), node.getParentId())!=null;
    }

    public Node fixNameConflict(Node node){
        boolean existsWithName = existWithSameKey(node);
        if(existsWithName){
            String oldName = node.getName();
            String generatedName = oldName+"_"+
                    new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")
                            .format(Date.from(Instant.now()));
            if(oldName.contains(".")){
                int index = oldName.lastIndexOf(".");
                generatedName = oldName.substring(0,index)+"_"+Instant.now()+oldName.substring(index);
            }
            node.setName(generatedName);
        }
        return node;
    }

}
