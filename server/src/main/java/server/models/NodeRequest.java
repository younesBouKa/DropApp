package server.models;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import server.data.NodeType;

import java.io.Serializable;
import java.util.Map;

@Component
public class NodeRequest implements Serializable {

    String name;
    NodeType type;
    String path;
    String parentId;
    Map<String, Object> fields;
    MultipartFile file;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
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
