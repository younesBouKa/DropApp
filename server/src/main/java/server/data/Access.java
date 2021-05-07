package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndexes({
        @CompoundIndex(name = "resourceId_requesterId", def = "{'resourceId':1, 'requesterId':1}", unique = true),
        @CompoundIndex(name = "_id", def = "{'_id':1}", unique = true)
})
@Document("access")
public class Access {
    @Id
    private String id;
    private String resourceId;
    private String requesterId;
    private int permission;

    public Access() {
    }

    public Access(String resourceId, String requesterId, int permission) {
        this.resourceId = resourceId;
        this.requesterId = requesterId;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }
}
