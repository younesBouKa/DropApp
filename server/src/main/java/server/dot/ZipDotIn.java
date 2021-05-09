package server.dot;

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

}
