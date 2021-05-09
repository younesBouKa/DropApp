package server.dot;

import server.data.NodeType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeDotIn implements Serializable {

    private String name;
    private NodeType type;
    private String description;
    private String label;
    private List<String> path;
    private String parentId;
    private Map<String, Object> fields = new HashMap<>();
}
