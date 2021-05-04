package server.models;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.data.NodeType;
import server.exceptions.CustomException;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class NodeWebRequest implements Serializable {

    private String name;
    private NodeType type;
    private String description;
    private String label;
    private List<String> path;
    private String parentId;
    private byte[] content;
    private long fileSize;
    private String contentType;
    private String originalName;
    private Map<String, Object> fields = new HashMap<>();

    public NodeWebRequest(){
        this.fields.put("from","WEB");
        this.type = NodeType.FILE;
    }

    public void updateWithFile(MultipartFile file){
        if(file==null)
            return;
        try{
            setFileSize(file.getSize());
            try (InputStream in = file.getInputStream()){
                setContent(IOUtils.toByteArray(in));
            }
            setOriginalName(file.getOriginalFilename());
            setContentType(file.getContentType());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void updateWithFile(File file){
        if(file==null || !file.canRead()) // TODO add some details here
            return;
        try{
            setFileSize(Files.size(file.toPath()));
            try (InputStream in = new FileInputStream(file)){
                setContent(IOUtils.toByteArray(in));
            }
            setOriginalName(file.getName());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Node toNode(NodeWebRequest nodeRequest) {
        Node node = updateNodeWith(new Node(), nodeRequest);;
        return node;
    }

    public static Node updateNodeWith(Node node, NodeWebRequest nodeRequest) {
        Map<String, Object> fields = nodeRequest.getFields();
        if (fields!=null){
            for(String key : fields.keySet()){
                node.getFields().put(key, fields.get(key));
            }
        }
        node.getFields().put("from","WEB");
        node.setName(nodeRequest.getName()!=null ? nodeRequest.getName() : node.getName());
        node.setParentId(nodeRequest.getParentId()!=null ? nodeRequest.getParentId() : node.getParentId());
        node.setPath(nodeRequest.getPath()!=null ? nodeRequest.getPath() : node.getPath());
        node.setType(nodeRequest.getType()!=null ? nodeRequest.getType() : node.getType());
        node.setModificationDate(Instant.now());
        return node;
    }

    public byte[] getFileContent() throws CustomException {
        return content;
    }

    public String getOriginalName(){
        return originalName!=null ? originalName : name;
    }

    public String getContentType(){
        return contentType;
    }

    public String getExtension(){
        return FilenameUtils.getExtension(getOriginalName());
    }

    public long getFileSize(){
        return content!=null  ? content.length : fileSize;
    }

    public String getName() {
        return name!=null ? name : getOriginalName();
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

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public byte[] getContent() {
        return content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
}
