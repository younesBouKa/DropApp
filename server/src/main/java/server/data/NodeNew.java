package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import server.models.NodeIncomingDto;

import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CompoundIndexes({@CompoundIndex(name = "name_parentId_spaceId", def = "{'name':1, 'parentId':1, 'spaceId':1}")})
@Document(collection = "node_new")
public class NodeNew implements Serializable {
    @Id
    String id;
    String name;
    NodeType type;
    String fileId;
    long fileSize;
    String contentType;
    String parentId;
    String spaceId;
    Map<String, Object> fields = new HashMap<>();
    Instant creationDate = Instant.now();
    Instant modificationDate = Instant.now();
    Space space;

    @Transient
    NodeNew parent;
    @Transient
    List<NodeNew> children;
    @Transient
    InputStream content; // TODO to remove later
    @Transient
    String path; // TODO to remove later


    public static NodeNew from(NodeIncomingDto nodeIncomingDto) {
        NodeNew node = updateWith(new NodeNew(),nodeIncomingDto);;
        return node;
    }

    public static NodeNew updateWith(NodeNew node, NodeIncomingDto nodeIncomingDto) {
        node.setName(nodeIncomingDto.getName());
        Map<String, Object> fields = nodeIncomingDto.getFields();
        if (fields!=null){
            for(String key : fields.keySet()){
                node.getFields().put(key, fields.get(key));
            }
        }
        node.setParentId(nodeIncomingDto.getParentId()); //
        node.setPath(nodeIncomingDto.getPath()); //
        node.setType(nodeIncomingDto.getType()); //
        return node;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Instant modificationDate) {
        this.modificationDate = modificationDate;
    }

    public NodeNew getParent() {
        return parent;
    }

    public void setParent(NodeNew parent) {
        this.parent = parent;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public List<NodeNew> getChildren() {
        return children;
    }

    public void setChildren(List<NodeNew> children) {
        this.children = children;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }
}
