package server.data;

import org.apache.commons.io.FilenameUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.tools.MimeTypesMap.getMimeType;

@CompoundIndexes({
        @CompoundIndex(name = "name_ownerId_parentId", def = "{'name':1, 'parentId':1, 'ownerId':1}", unique = true),
        @CompoundIndex(name = "_id", def = "{'_id':1}", unique = true)
})
@Document(collection = "node")
public class Node implements Serializable {
    @Id
    private String id;
    private String name;
    private String ownerId;
    private NodeType type = NodeType.FOLDER;

    private String parentId;
    private List<String> path;

    private FileVersion currentFileVersion = new FileVersion();
    private List<FileVersion> versions = new ArrayList<>();

    private String label;
    private String description;

    private Instant creationDate = Instant.now();
    private Instant modificationDate = Instant.now();

    private Map<String, Object> fields = new HashMap<>();

    @Transient
    private Node parent;
    @Transient
    private List<Node> children;
    @Transient
    private InputStream content;

    public Node(){}

    public Node(String name, String ownerId){
        this.name = name;
        this.ownerId = ownerId;
        this.currentFileVersion.setUserId(ownerId);
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
        currentFileVersion.setUserId(id);
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
        return currentFileVersion.getFileId();
    }

    public long getFileSize() {
        return currentFileVersion.getFileSize();
    }

    public String getContentType() {
        return currentFileVersion.getContentType();
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
        return currentFileVersion.getOriginalName() !=null ? currentFileVersion.getOriginalName() : getName();
    }

    public String getExtension() {
        return currentFileVersion.getExtension();
    }

    public FileVersion getCurrentFileVersion() {
        return currentFileVersion;
    }

    public void setCurrentFileVersion(FileVersion currentFileVersion) {
        this.currentFileVersion = currentFileVersion;
    }

    public List<FileVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<FileVersion> versions) {
        this.versions = versions;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isFile(){
        return NodeType.FILE.equals(getType());
    }

    public boolean isFolder(){
        return NodeType.FOLDER.equals(getType());
    }
}
