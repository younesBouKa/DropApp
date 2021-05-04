package server.services;

import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.data.FileVersion;
import server.data.IUser;
import server.data.NodeType;
import server.exceptions.CustomException;
import server.data.Node;
import server.models.NodeFtpRequest;
import server.models.NodeWebRequest;
import server.models.ZipRequest;
import server.repositories.IFileRepo;
import server.repositories.INodeRepo;
import server.tools.tools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        zipRequest.updateWithFile(zipFile);
        // creating node
        Node zipNode = new Node(zipRequest.getName(), user.getId(), parentId, path);
        zipNode.setType(NodeType.FILE);
        zipNode.getFields().putAll(zipRequest.getFields());
        zipNode = fixNameConflict(zipNode);
        // saving zip file content to database
        String fileId = saveFile(zipNode, zipRequest.getContent());
        if(fileId!=null){
            // adding file version
            String contentHash = tools.hash(zipRequest.getContent());
            FileVersion fileVersion = new FileVersion(fileId, user.getId(),
                    zipRequest.getOriginalName(), zipRequest.getFileSize(),
                    zipRequest.getContentType(), zipRequest.getExtension(),
                    zipRequest.getFields(), contentHash).hashVersion();
            zipNode.setCurrentFileVersion(fileVersion);
        }
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
        // fix name
        node = fixNameConflict(node);
        // save file
        String fileId = saveFile(node, nodeWebRequest.getFileContent());
        // adding file version
        if(fileId!=null){
            String contentHash = tools.hash(nodeWebRequest.getContent());
            FileVersion fileVersion = new FileVersion(fileId, user.getId(),
                    nodeWebRequest.getOriginalName(), nodeWebRequest.getFileSize(),
                    nodeWebRequest.getContentType(), nodeWebRequest.getExtension(),
                    nodeWebRequest.getFields(), contentHash).hashVersion();
            node.setCurrentFileVersion(fileVersion);
        }
        try {
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
        // save file
        String fileId = saveFile(node, nodeWebRequest.getFileContent());
        // adding file version
        if(fileId!=null){
            FileVersion currentVersion = node.getCurrentFileVersion();
            String contentHash = tools.hash(nodeWebRequest.getContent());
            FileVersion fileVersion = new FileVersion(
                    fileId,
                    user.getId(),
                    Optional.ofNullable(nodeWebRequest.getOriginalName()).orElse(currentVersion.getOriginalName()),
                    Optional.of(nodeWebRequest.getFileSize()).orElse(currentVersion.getFileSize()),
                    Optional.ofNullable(nodeWebRequest.getContentType()).orElse(currentVersion.getContentType()),
                    Optional.ofNullable(nodeWebRequest.getExtension()).orElse(currentVersion.getExtension()),
                    Optional.ofNullable(nodeWebRequest.getFields()).orElse(currentVersion.getFields()),
                    contentHash
            ).hashVersion();
            // set current version in history
            if(
                    currentVersion.getFileId()!=null // file is not null
                    && !currentVersion.getVersionHash().equals(fileVersion.getVersionHash()) // and was updated
            )
                node.getVersions().add(node.getCurrentFileVersion());
            // set current version
            node.setCurrentFileVersion(fileVersion.hashVersion());
        }
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
            //node = fixNameConflict(node);
            // save file
            String fileId = saveFile(node, nodeFtpRequest.getContent());
            // build Version
            if(fileId!=null){
                FileVersion currentVersion = node.getCurrentFileVersion();
                String contentHash = tools.hash(nodeFtpRequest.getContent());
                FileVersion fileVersion = new FileVersion(
                        fileId,
                        user.getId(),
                        Optional.of(nodeFtpRequest.getOriginalName()).orElse(currentVersion.getOriginalName()),
                        Optional.of(nodeFtpRequest.getFileSize()).orElse(currentVersion.getFileSize()),
                        Optional.of(nodeFtpRequest.getContentType()).orElse(currentVersion.getContentType()),
                        Optional.of(nodeFtpRequest.getExtension()).orElse(currentVersion.getExtension()),
                        Optional.of(nodeFtpRequest.getFields()).orElse(currentVersion.getFields()),
                        contentHash).hashVersion();
                // set current version in history
                if(currentVersion.getFileId()!=null && !currentVersion.getVersionHash().equals(fileVersion.getVersionHash()))
                    node.getVersions().add(node.getCurrentFileVersion());
                // set current version
                node.setCurrentFileVersion(fileVersion);
            }
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

    /********** Tools *******************/
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

    public String saveFile(Node node, InputStream inputStream) throws CustomException {
        if(inputStream!=null){
            node.setType(NodeType.FILE);
            try (InputStream inputStream1 = inputStream){
                ObjectId fileId = fileRepo.saveFile(inputStream1, node);
                return fileId.toHexString();
            }catch (Exception e){
                throw new CustomException(e, ERROR_WHILE_SAVING_FILE, node.getName());
            }
        }
        return null;
    }

    public String saveFile(Node node, byte[] bytes) throws CustomException {
        if(bytes!=null && bytes.length>0){
            node.setType(NodeType.FILE);
            try (InputStream in = new ByteArrayInputStream(bytes)){
                return saveFile(node, in);
            }catch (Exception e){
                throw new CustomException(e, ERROR_WHILE_SAVING_FILE, node.getName());
            }
        }
        return null;
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

    public Node getWithSameKey(Node node){
        return getByNameAndOwnerIdAndParentId(node.getName(), node.getOwnerId(), node.getParentId());
    }

    public Node fixNameConflict(Node node){
        Node nodeWithSameKey = getWithSameKey(node);
        if(nodeWithSameKey!=null){
            String oldName = node.getName();
            String generatedName = oldName+"_"+System.currentTimeMillis();
            if(oldName.contains(".")){
                int index = oldName.lastIndexOf(".");
                generatedName = oldName.substring(0,index)+"_"+System.currentTimeMillis()+oldName.substring(index);
            }
            node.setName(generatedName);
        }
        return node;
    }

}
