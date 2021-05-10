package server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.data.Group;
import server.data.GroupMembership;
import server.dot.GroupDotIn;
import server.dot.MembershipDotIn;
import server.exceptions.CustomException;
import server.providers.IGroupProvider;

import java.util.List;

import static server.exceptions.Message.GROUP_NOT_FOUND;

@Service
public class GroupService implements IGroupService{

    @Autowired
    IGroupProvider groupProvider;

    @Override
    public Group addGroup(String adminId, GroupDotIn groupDotIn) {
        Group group = groupDotIn.toGroup();
        return groupProvider.insertGroup(adminId, group);
    }

    @Override
    public Group updateGroup(String adminId, String groupId, GroupDotIn groupDotIn) throws CustomException {
        Group group = groupProvider.getGroupById(adminId, groupId);
        if(group==null)
            throw new CustomException(GROUP_NOT_FOUND, groupId);
        group = groupDotIn.toGroup(group);
        return groupProvider.saveGroup(adminId, group);
    }

    @Override
    public boolean deleteGroup(String adminId, String groupId) throws CustomException {
        return groupProvider.deleteGroupById(adminId, groupId);
    }

    @Override
    public GroupMembership addMemberToGroup(String adminId, MembershipDotIn membershipDotIn) throws CustomException {
        String role = membershipDotIn.getRole()!=null ? membershipDotIn.getRole() : "USER";
        GroupMembership groupMembership = new GroupMembership(membershipDotIn.getGroupId(), membershipDotIn.getMemberId(), role);
        return groupProvider.addMemberToGroup(adminId, groupMembership);
    }

    @Override
    public boolean removeMemberFromGroup(String adminId, String userId, String groupId) throws CustomException {
        return groupProvider.removeMemberFromGroup(adminId, userId, groupId);
    }

    @Override
    public List<Group> getGroupsToBellow(String userId) {
        return groupProvider.getAllGroupsByMemberId(userId);
    }
}
