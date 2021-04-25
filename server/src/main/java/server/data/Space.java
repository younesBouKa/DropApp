package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import server.models.SpaceRequest;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CompoundIndexes({@CompoundIndex(name = "name_ownerId", def = "{'name':1, 'ownerId':1}")})
@Document(collection = "space")
public class Space implements Serializable {
    public static final String SEPARATOR = "/";
    public static final String ROOT_PATH = "/";

    @Id
    String id;
    String name;
    String ownerId;
    String rootPath;
    Instant creationDate = Instant.now();
    Instant modificationDate = Instant.now();
    Map<String, Object> fields = new HashMap<>();

    @Transient
    List<NodeNew> roots;

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getLastModificationDate() {
        return modificationDate;
    }

    public void setLastModificationDate(Instant modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public List<NodeNew> getRoots() {
        return roots;
    }

    public void setRoots(List<NodeNew> roots) {
        this.roots = roots;
    }
}
