package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndexes(
        @CompoundIndex(name = "userId_groupId",def = "{'userId':1,'groupId':1}",unique = true)
)
@Document("user_group")
public class UserGroup {
    @Id
    private String id;
    private String userId;
    private String groupId;
    private boolean enable = true;
    private String role;

    public UserGroup(String userId, String groupId, String role) {
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
