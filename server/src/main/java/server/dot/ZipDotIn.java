package server.dot;

import server.models.ZipRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZipDotIn implements Serializable {

    private String name;
    private String description;
    private String label;
    private String parentId;
    private List<String> nodesId;
    private Map<String, Object> fields = new HashMap<>();

    public ZipRequest toZipRequest(){
        ZipRequest zipRequest = new ZipRequest();
        zipRequest.setName(getName());
        zipRequest.setLabel(getLabel());
        zipRequest.setDescription(getDescription());
        zipRequest.setParentId(getParentId());
        zipRequest.setNodesId(getNodesId());
        zipRequest.setFields(getFields());
        return zipRequest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getNodesId() {
        return nodesId;
    }

    public void setNodesId(List<String> nodesId) {
        this.nodesId = nodesId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
