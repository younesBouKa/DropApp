package server.models;

import org.springframework.stereotype.Component;
import server.data.IUser;
import server.data.NodeNew;
import server.data.NodeType;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Component
public class NodeFtpRequest implements Serializable {

    private String name;
    private NodeType type;
    private String contentType;
    private String parentId;
    private String spaceId;
    private String path;
    private File file;
    private String ownerName;
    private long fileSize;
    private Map<String, Object> fields = new HashMap<>();
    private IUser user;

    public static NodeNew toNode(NodeFtpRequest nodeFtpRequest) {
        NodeNew node = updateNodeWith(new NodeNew(), nodeFtpRequest);;
        return node;
    }

    public static NodeNew updateNodeWith(NodeNew node, NodeFtpRequest nodeFtpRequest) {
        node.setName(nodeFtpRequest.getName());
        Map<String, Object> fields = nodeFtpRequest.getFields();
        if (fields!=null){
            for(String key : fields.keySet()){
                node.getFields().put(key, fields.get(key));
            }
        }
        node.setSpaceId(nodeFtpRequest.getSpaceId());
        node.setParentId(nodeFtpRequest.getParentId());
        node.setPath(nodeFtpRequest.getPath());
        node.setType(nodeFtpRequest.getType());
        node.setContentType(nodeFtpRequest.getContentType());
        node.setFileSize(nodeFtpRequest.getFileSize());
        return node;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public IUser getUser() {
        return user;
    }

    public void setUser(IUser user) {
        this.user = user;
    }
}
