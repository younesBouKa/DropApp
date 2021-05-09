package server.providers;

import server.data.Group;
import server.data.UserGroup;

import java.util.List;

public interface IGroupProvider {

    Group insertGroup(String adminId, Group group);

    Group saveGroup(String adminId, Group group);

    Group getGroupById(String groupId);

    List<Group> getGroupsByAdminId(String adminId);

    List<Group> getAllGroupsByUserId(String userId);

    List<UserGroup> getEnabledGroupsForUserId(String userId);

    boolean deleteById(String adminId, String groupId);

    boolean removeUserFromGroup(String adminId, String userId, String groupId);

    UserGroup addUserToGroup(String adminId, String userId, String groupId);
}
