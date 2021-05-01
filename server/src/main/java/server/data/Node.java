package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CompoundIndexes({
        @CompoundIndex(name = "name_ownerId_parentId", def = "{'name':1, 'parentId':1, 'ownerId':1}", unique = true),
        @CompoundIndex(name = "_id", def = "{'_id':1}", unique = true)
})
@Document(collection = "node_new")
public class Node implements Serializable {
    @Id
    String id;
    String name;
    String ownerId;
    NodeType type = NodeType.FOLDER;

    String parentId;
    List<String> path;

    String originalName;
    String fileId;
    long fileSize;
    String contentType;
    String extension;

    String label;
    String description;

    Instant creationDate = Instant.now();
    Instant modificationDate = Instant.now();

    Map<String, Object> fields = new HashMap<>();

    @Transient
    Node parent;
    @Transient
    List<Node> children;
    @Transient
    InputStream content; // TODO to remove later

    public Node(){}

    public Node(String name, String ownerId){
        this.name = name;
        this.ownerId = ownerId;
    }

    public Node(String name, String ownerId, String parentId){
        this(name, ownerId);
        this.parentId = parentId;
    }

    public Node(String name, String ownerId, List<String> path){
        this(name, ownerId, null, path);
    }

    public Node(String name, String ownerId, String parentId, List<String> path){
        this(name, ownerId, parentId);
        this.path = path;
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

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
