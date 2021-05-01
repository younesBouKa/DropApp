package server.models;

import org.apache.commons.io.FilenameUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.springframework.stereotype.Component;
import server.data.IUser;
import server.data.Node;
import server.data.NodeType;
import server.exceptions.CustomException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.exceptions.Message.ERROR_WHILE_READING_FILE_CONTENT;

@Component
public class NodeFtpRequest implements Serializable {

    private String name;
    private IUser user;
    private FtpFile file;
    private NodeType type;
    private String ownerId;
    private long fileSize;
    private List<String> path;
    private String contentType;
    private Map<String, Object> fields = new HashMap<>();

    public NodeFtpRequest(){
        this.fields.put("from","FTP");
        this.type = NodeType.FILE;
    }

    public NodeFtpRequest(IUser user, FtpFile file, List<String> path){
        this();
        // owner infos
        this.setUser(user);
        this.setOwnerId(user.getId());
        // file infos
        this.setType(file.isFile() ? NodeType.FILE : NodeType.FOLDER);
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
        this.setContentType(contentType);
        this.setPath(path);
        this.setName(file.getName());
        this.setFile(file);
        this.setFileSize(file.getSize());
    }

    public static Node toNode(NodeFtpRequest nodeFtpRequest) {
        Node node = updateNodeWith(new Node(), nodeFtpRequest);;
        return node;
    }

    public static Node updateNodeWith(Node node, NodeFtpRequest nodeFtpRequest) {
        Map<String, Object> fields = nodeFtpRequest.getFields();
        if (fields!=null){
            for(String key : fields.keySet()){
                node.getFields().put(key, fields.get(key));
            }
        }
        node.setName(nodeFtpRequest.getName());
        node.setExtension(FilenameUtils.getExtension(nodeFtpRequest.getOriginalName()));
        node.setOriginalName(nodeFtpRequest.getOriginalName());
        node.setOwnerId(nodeFtpRequest.getUser().getId());
        node.setPath(nodeFtpRequest.getPath());
        node.setType(nodeFtpRequest.getType());
        node.setContentType(nodeFtpRequest.getContentType());
        node.setFileSize(nodeFtpRequest.getFileSize());
        node.setModificationDate(Instant.now());
        return node;
    }

    public InputStream getFileContent() throws CustomException {
        try {
            return getFile().createInputStream(0);
        }catch (IOException e){
            throw new CustomException(e, ERROR_WHILE_READING_FILE_CONTENT, getName());
        }
    }

    public String getOriginalName(){
        return getFile()!=null ? getFile().getName() : getName();
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public FtpFile getFile() {
        return file;
    }

    public void setFile(FtpFile file) {
        this.file = file;
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
