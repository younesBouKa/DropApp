package server.models;

import org.springframework.web.multipart.MultipartFile;
import server.data.NodeType;

import java.io.Serializable;
import java.util.Map;

public class NodeFtpModel implements Serializable {

    String name;
    NodeType type;
    String path;
    String parentId;
    Map<String, Object> fields;
    MultipartFile file;
}
