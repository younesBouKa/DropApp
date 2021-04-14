package server.services;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.data.NodeType;
import server.data.Permission;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NodeService {
    
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private FsFilesService fileService;

    /****************** get Nodes ************************/
    public Node getNodeById(String id) {
        if(id==null)
            return null;
        return mongoOperations.findOne(new Query(Criteria.where("_id").is(id)), Node.class);
    }

    public Node getNodeByPath(String path){
        if(path==null)
            return null;
        path = path.endsWith(Node.SEPARATOR) && !path.equals(Node.ROOT_PATH) ? path.substring(0, path.lastIndexOf(Node.SEPARATOR)) : path ;
        Node node = mongoOperations.findOne(new Query(Criteria.where("path").is(path)),Node.class);
        return node;
    }

    public List<Node> getNodesByParentPath(String parentPath) {
        Node parentNode = getNodeByPath(parentPath);
        List<Node> list =  parentNode!=null ? getNodesByParentId(parentNode.getId()) : new ArrayList<>();
        return list;
    }

    public List<Node> getNodesByParentId(String parentId) {
        if(parentId==null)
            return null;
        return mongoOperations.find(new Query(Criteria.where("parentId").is(parentId)), Node.class);
    }

    public List<Node> getNodesByQuery(Map<String, Object> query) {
        if(query.get("parentId")!=null){
            return getNodesByParentId((String) query.get("parentId"));
        }

        if(query.get("parentPath")!=null){
            return getNodesByParentPath((String)query.get("parentPath"));
        }
        return new ArrayList<>();
    }

    public Node getRoot(){
        Node root = mongoOperations.findOne(new Query(Criteria.where("path").is(Node.ROOT_PATH)),Node.class);
        if(root!=null)
            return root;
        return upsertOneNode(new Node());
    }

    /****************** save Nodes ************************/
    public List<Node> upsertNodes(List<Node> list) {
        return list
                .stream()
                .map(this::upsertOneNode)
                .collect(Collectors.toList());
    }

    public Node upsertOneNode(Node node) {
        if(node==null){
            return null;
        }
        else if(node.getParentId()!=null){ // setting correct path
            Node parentNode = getNodeById(node.getParentId());
            if(parentNode!=null){
                String path = parentNode.getPath();
                path = concatTwoPaths(path,node.getName());
                node.setPath(path);
            }else{
                return null;
            }
        }else if(node.getPath()!=null){ // setting parentId
            Node parentNode = getNodeByPath(node.getParentFolderPath());
            if(parentNode!=null)
                node.setParentId(parentNode.getId());
        }

        if(node.getId()!=null && existsWithId(node.getId())){
            return updateNode(node);
        }else
            return mongoOperations.save(node);
    }

    public Node updateNode(Node node) {
        if(node.getId()!=null && existsWithId(node.getId())){
            Update update = node.createUpdateObj();
            UpdateResult updateResult = mongoOperations.updateFirst(new Query(Criteria.where("_id").in(node.getId())), update , Node.class);
            if(updateResult.getModifiedCount()>0){
                return getNodeById(node.getId());
            }
        }
        return null;
    }

    /****************** delete Nodes ************************/
    public List<String> deleteNodesByIds(List<String> listIds, boolean recursive) {
        if(listIds==null || listIds.size()==0)
            return null;
        return listIds
                .stream()
                .filter(nodeId ->deleteNodeById(nodeId, recursive)>0)
                .collect(Collectors.toList());
    }

    public long deleteNodeByPath(String nodePath,boolean recursive) {
        Node node = getNodeByPath(nodePath);
        if(node==null)
            return 0;
        return deleteNodeById(node.getId(), recursive);
    }

    public long deleteNodeById(String nodeId, boolean recursive) {
        Node node = getNodeById(nodeId);
        long count = 0;
        if(node==null)
            return 0;
        count = mongoOperations.remove(new Query(Criteria.where("_id").is(node.getId())), Node.class).getDeletedCount();
        if(node.isFile() && count>0)
            fileService.deleteWithId(nodeId);
        if(count>0 && node.isFolder() && recursive){
            List<Node> children = getNodesByParentId(node.getId());
            count = count + children.stream()
                    .map(child -> deleteNodeById(child.getId(), true))
                    .reduce((acc,co)-> co+acc).orElse(0l);
        }
        return count;
    }

    /****************** permissions ************************/
    public boolean hasPermissionById(String nodeId, Object permission) {
        Node node = getNodeById(nodeId);
        return hasPermission(node,Permission.getFrom(permission));
    }

    public boolean hasPermissionByPath(String path, Object permission) {
        Node node = getNodeByPath(path);
        return hasPermission(node, Permission.getFrom(permission));
    }

    public boolean hasPermission(Node node, Permission permission) {
        return node!=null && permission!=null && ((permission.getCode() & node.getPermission().getCode()) == permission.getCode());
    }

    /****************** exists ************************/
    public boolean existsWithId(String nodeId){
        return nodeId!=null && mongoOperations.exists(new Query(Criteria.where("_id").is(nodeId)),Node.class);
    }

    public boolean existsWithPath(String nodePath){
        return nodePath!=null && mongoOperations.exists(new Query(Criteria.where("path").is(nodePath)),Node.class);
    }

    /****************** create node ************************/
    public Node createFolder(Map<String, Object> metaData) {
        Node parentNode = null;
        if( metaData.get("parentId")!=null)
            parentNode = getNodeById((String)metaData.get("parentId"));
        if(parentNode==null || metaData.get("path")!=null)
            parentNode = createFolderFromPath((String) metaData.get("path"));
        if(parentNode==null)
            parentNode = getRoot();
        Permission permission = Permission.getFrom(metaData.getOrDefault("permission",parentNode.getPermission()));
        String path = parentNode.getPath();
        String name = (String)metaData.getOrDefault("name","folder_"+new Date().getTime());
        path = concatTwoPaths(path, name);
        Node node = new Node(path,permission,parentNode.getId(),new Date(), NodeType.FOLDER,name);
        Node oldNode = getNodeByPath(path);
        if(oldNode!=null){
            return oldNode;
        }
        return mongoOperations.save(node);
    }

    public Node createFolderFromPath(String nodePath){
        if (existsWithPath(nodePath))
            return getNodeByPath(nodePath);
        String[] pathParts = nodePath.split(Node.SEPARATOR);
        Node currentNode = null;
        Node parentNode = getRoot();
        int length = pathParts.length;
        String path = getRoot().getPath();
        for(int i=0; i< length; i++){
            if(pathParts[i].isEmpty()|| pathParts[i].contains("."))
                continue;
            path = concatTwoPaths(path, pathParts[i]);
            currentNode = getNodeByPath(path);
            if(currentNode==null) {
                currentNode = new Node(path, parentNode.getPermission(), parentNode.getId(), new Date(), NodeType.FOLDER, pathParts[i]);
                currentNode = upsertOneNode(currentNode);
            }
            parentNode = currentNode;
        }
        return currentNode;
    }

    public Node createFile(MultipartFile file, Map<String, Object> metaData, boolean upsert) throws Exception {
        Node parentNode = null;
        if(metaData.get("parentId")!=null){
            parentNode = getNodeById((String) metaData.get("parentId"));
        }else if(metaData.get("parentPath")!=null){
            parentNode = getNodeByPath((String) metaData.get("parentPath"));
            if(parentNode==null)
                parentNode = createFolderFromPath((String) metaData.get("parentPath"));
        }
        if(parentNode==null)
            parentNode = getRoot();

        String name = (String) metaData.getOrDefault("name",file.getOriginalFilename());
        String path = concatTwoPaths(parentNode.getPath() , name);

        Node nodeInDb = getNodeByPath(path);
        Node node = new Node(
                path,
                Permission.getFrom(metaData.getOrDefault("permission",parentNode.getPermission())),
                parentNode.getId(),
                new Date(),
                NodeType.FILE,
                name
        );

        if(nodeInDb!=null && upsert){
            node.setId(nodeInDb.getId());
        }else if(nodeInDb!=null){
            throw new Exception("file already exist with same name");
        }
        ObjectId id = fileService.saveFile(file, node);
        node.setFileId(id.toString());
        return upsertOneNode(node);
    }

    /****************** copy and move node ************************/

    public Node copyNodeWithPath(String srcPath, String destPath, boolean move){
        if (!existsWithPath(srcPath))
            return null;// TODO to see later
        Node srcNode = getNodeByPath(srcPath);
        Node destNode = getNodeByPath(destPath);
        if(destNode==null){
            destNode = createFolderFromPath(destPath);
        }
        destPath = destNode.getPath();

        if(srcNode.isFolder()){
            // copy folder
            Node folderCopy = duplicateNode(srcNode.getId());
            folderCopy.setParentId(destNode.getId());
            folderCopy.setPath(concatTwoPaths(destPath,folderCopy.getName()));
            upsertOneNode(folderCopy);
            deleteNodeById(srcNode.getId(),false);
            // copy children
            List<Node> children = getNodesByParentId(srcNode.getId());
            Node finalDestNode = destNode;
            children.stream().forEach(child->{
                String dest =  concatTwoPaths(finalDestNode.getPath(),child.getName());
                copyNodeWithPath(child.getPath(),dest, move);
            });
        }else if(srcNode.isFile()){
            // copy file
            Node fileCopy = duplicateNode(srcNode.getId());
            fileCopy.setParentId(destNode.getId());
            fileCopy.setPath(concatTwoPaths(destPath,fileCopy.getName()));
            upsertOneNode(fileCopy);
            deleteNodeById(srcNode.getId(),false);
        }
        return destNode;
    }

    public Node copyNodeWithId(String srcId, String destId, boolean andDelete){
        Node srcNode = getNodeById(srcId), destNode = getNodeById(destId);
        if(srcNode!=null && destNode !=null)
            return copyNodeWithPath(srcNode.getPath(), destNode.getPath(), andDelete);
        return null;
    }

    /****************** duplicate node ************************/
    public String concatTwoPaths (String firstPath, String secondPath){
        return firstPath +
                (firstPath.endsWith(Node.SEPARATOR) || secondPath.startsWith(Node.SEPARATOR) ? "" : Node.SEPARATOR) +
                secondPath;
    }

    public Node duplicateNode (String nodeId){
        Node node = getNodeById(nodeId);
        if(node==null)
            return null;
        node.setId(null);
        return upsertOneNode(node);
    }
}
