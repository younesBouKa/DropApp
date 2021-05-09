package server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.Group;
import server.data.UserGroup;
import server.repositories.IGroupRepo;
import server.repositories.IUserGroupRepo;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupProvider implements IGroupProvider{

    @Autowired
    IGroupRepo groupRepo;
    @Autowired
    IUserGroupRepo userGroupRepo;

    @Override
    public Group insertGroup(String adminId, Group group) {
        Group savedGroup = groupRepo.insert(group);
        UserGroup userGroup = userGroupRepo.insert(
                    new UserGroup(adminId, savedGroup.getId(), "ADMIN")
            );
        return savedGroup;
    }

    @Override
    public Group saveGroup(String adminId, Group group) {
        Group foundedGroup = groupRepo.findByIdAndAdminId(group.getId(), adminId);
        if(foundedGroup!=null){
            return groupRepo.save(group);
        }else{
            return null;//insertGroup(adminId, group);
        }
    }

    @Override
    public Group getGroupById(String groupId) {
        return groupRepo.findById(groupId).orElse(null);
    }

    @Override
    public List<Group> getGroupsByAdminId(String adminId) {
        return groupRepo.findByAdminId(adminId);
    }

    @Override
    public List<Group> getAllGroupsByUserId(String userId) {
        List<UserGroup> userGroupList = userGroupRepo.findByUserId(userId);
        List<String> groupIds = userGroupList.stream().map(UserGroup::getGroupId).collect(Collectors.toList());
        return groupRepo.findByIdIn(groupIds);
    }

    @Override
    public List<UserGroup> getEnabledGroupsForUserId(String userId) {
        return userGroupRepo.findByUserIdAndEnable(userId, true);
    }

    @Override
    public boolean deleteById(String adminId, String groupId) {
        Group foundedGroup = groupRepo.findById(groupId).orElse(null);
        if(foundedGroup!=null && foundedGroup.getAdminId().equals(adminId)){
            groupRepo.deleteByIdAndAdminId(groupId, adminId);
            userGroupRepo.deleteAllByGroupId(groupId);
        }
        return false;
    }

    @Override
    public boolean removeUserFromGroup(String adminId, String userId, String groupId) {
        Group foundedGroup = groupRepo.findByIdAndAdminId(groupId, adminId);
        if(foundedGroup!=null){
            userGroupRepo.deleteByUserIdAndGroupId(userId, groupId);
            return true;
        }
        return false;
    }

    @Override
    public UserGroup addUserToGroup(String adminId, String userId, String groupId) {
        Group foundedGroup = groupRepo.findByIdAndAdminId(groupId, adminId);
        if(foundedGroup!=null){
            return userGroupRepo.insert(
                    new UserGroup(adminId, foundedGroup.getId(), "USER")
            );
        }else{
            return null;
        }
    }
}
