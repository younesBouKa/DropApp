package server.models;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.data.NodeType;
import server.exceptions.CustomException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.exceptions.Message.ERROR_WHILE_READING_FILE_CONTENT;

@Component
public class NodeWebRequest implements Serializable {

    private String name;
    private NodeType type;
    private List<String> path;
    private String parentId;
    private Map<String, Object> fields = new HashMap<>();
    private MultipartFile file;

    public NodeWebRequest(){
        this.fields.put("from","WEB");
        this.type = NodeType.FILE;
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
        node.setExtension(FilenameUtils.getExtension(nodeRequest.getOriginalName()));
        node.setContentType(nodeRequest.getContentType());
        node.setFileSize(nodeRequest.getFileSize());
        node.setOriginalName(nodeRequest.getOriginalName());
        node.setName(nodeRequest.getName());
        node.setParentId(nodeRequest.getParentId());
        node.setPath(nodeRequest.getPath());
        node.setType(nodeRequest.getType());
        node.setModificationDate(Instant.now());
        return node;
    }

    public InputStream getFileContent() throws CustomException {
        try {
            return getFile().getInputStream();
        }catch (IOException e){
            throw new CustomException(e, ERROR_WHILE_READING_FILE_CONTENT, getName());
        }
    }

    public String getOriginalName(){
        return getFile()!=null ? getFile().getOriginalFilename() : name;
    }

    public String getContentType(){
        return getFile()!=null ? getFile().getContentType() : "";
    }

    public long getFileSize(){
        return getFile()!=null ? getFile().getSize() : 0L;
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public static String getFormat(){
        return "{" +
                "name: String, " +
                "type: ['FOLDER' | 'FILE']," +
                "parentId: String, " +
                "fields : Map<String,Object>, " +
                "file: MultipartFile" +
                "}";
    }
}
