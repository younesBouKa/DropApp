package server.services;

import server.data.Group;
import server.data.GroupMembership;
import server.dot.GroupDotIn;
import server.dot.MembershipDotIn;
import server.exceptions.CustomException;

import java.util.List;

public interface IGroupService {

    Group addGroup(String adminId, GroupDotIn groupDotIn);

    Group updateGroup(String adminId, String groupId, GroupDotIn groupDotIn) throws CustomException;

    boolean deleteGroup(String adminId, String groupId) throws CustomException;

    GroupMembership addMemberToGroup(String adminId, MembershipDotIn membershipDotIn) throws CustomException;

    boolean removeMemberFromGroup(String adminId, String memberId, String groupId) throws CustomException;

    List<Group> getGroupsToBellow(String memberId);
}
