package server.models;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.springframework.stereotype.Component;
import server.data.IUser;
import server.data.Node;
import server.data.NodeType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NodeFtpRequest implements Serializable {

    private String name;
    private String ownerId;
    private IUser user;
    private List<String> path;
    private long fileSize;
    private String contentType;
    private NodeType type;
    private Map<String, Object> fields = new HashMap<>();
    private byte[] content;

    public NodeFtpRequest(){
        this.fields.put("from","FTP");
    }

    public NodeFtpRequest(IUser user, FtpFile file, List<String> path){
        this();
        this.setUser(user);
        this.setOwnerId(user.getId());
        this.setPath(path);
        // add file infos
        this.updateWithFile(file);
    }

    public void updateWithFile(FtpFile file){
        try{
            if(file==null || !file.isReadable() || !file.isFile())
                return;
            setFileSize(file.getSize());
            try(InputStream in = file.createInputStream(0)){
                setContent(IOUtils.toByteArray(in));
            }
            setName(file.getName());
            setType(file.isFile()? NodeType.FILE: NodeType.FOLDER);
        }catch (IOException e){
            e.printStackTrace();
        }
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
        if(nodeFtpRequest.getName()!=null)
            node.setName(nodeFtpRequest.getName());
        if(nodeFtpRequest.getUser()!=null)
            node.setOwnerId(nodeFtpRequest.getUser().getId());
        if(nodeFtpRequest.getPath()!=null)
            node.setPath(nodeFtpRequest.getPath());
        if(nodeFtpRequest.getType()!=null)
            node.setType(nodeFtpRequest.getType());
        node.setModificationDate(Instant.now());
        return node;
    }

    public byte[] getContent() {
        return content;
    }

    public String getExtension(){
        return FilenameUtils.getExtension(getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type!=null ? type : NodeType.FOLDER;
    }

    public String getContentType() {
        return contentType!=null ? contentType: URLConnection.getFileNameMap().getContentTypeFor(getName());
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

    public long getFileSize() {
        return content!=null ? content.length : fileSize;
    }

    public IUser getUser() {
        return user;
    }

    public void setUser(IUser user) {
        this.user = user;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setType(NodeType type) {
        this.type = type;
    }
}
