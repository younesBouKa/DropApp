package server.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndexes(
        @CompoundIndex(name = "memberId_groupId",def = "{'memberId':1,'groupId':1}",unique = true)
)
@Document("membership")
public class GroupMembership {
    @Id
    private String id;
    private String groupId;
    private String memberId;
    private boolean enable = true;
    private String role;

    public GroupMembership(String groupId, String memberId, String role) {
        this.memberId = memberId;
        this.groupId = groupId;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
