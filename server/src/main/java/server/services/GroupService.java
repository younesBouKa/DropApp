package server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.data.Group;
import server.data.UserGroup;
import server.providers.IGroupProvider;

import java.util.List;

@Service
public class GroupService implements IGroupService{

    @Autowired
    IGroupProvider groupProvider;

    @Override
    public Group addGroup(String adminId, Group group) {
        return groupProvider.insertGroup(adminId, group);
    }

    @Override
    public Group updateGroup(String adminId, String groupId, Group group) {
        return groupProvider.saveGroup(adminId, group);
    }

    @Override
    public boolean deleteGroup(String adminId, String groupId) {
        return groupProvider.deleteById(adminId, groupId);
    }

    @Override
    public UserGroup addUserToGroup(String adminId, String userId, String groupId) {
        return groupProvider.addUserToGroup(adminId, userId, groupId);
    }

    @Override
    public boolean removeUserFromGroup(String adminId, String userId, String groupId) {
        return groupProvider.removeUserFromGroup(adminId, userId, groupId);
    }

    @Override
    public List<Group> getOwnGroups(String adminId) {
        return groupProvider.getGroupsByAdminId(adminId);
    }

    @Override
    public List<Group> getGroupsToBellow(String userId) {
        return groupProvider.getAllGroupsByUserId(userId);
    }
}
