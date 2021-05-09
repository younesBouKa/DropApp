package server.services;

import server.data.Group;
import server.data.UserGroup;

import java.util.List;

public interface IGroupService {

    Group addGroup(String adminId, Group group);

    Group updateGroup(String adminId, String groupId, Group group);

    boolean deleteGroup(String adminId, String groupId);

    UserGroup addUserToGroup(String adminId, String userId, String groupId);

    boolean removeUserFromGroup(String adminId, String userId, String groupId);

    List<Group> getOwnGroups(String adminId);

    List<Group> getGroupsToBellow(String userId);
}
