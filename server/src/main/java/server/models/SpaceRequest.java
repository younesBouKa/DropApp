package server.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SpaceRequest implements Serializable {

    private String name;
    Map<String, Object> fields = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
