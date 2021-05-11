package server.providers;

import server.data.Group;
import server.data.GroupMembership;
import server.exceptions.CustomException;

import java.util.List;

public interface IGroupProvider {

    Group insertGroup(String adminId, Group group);

    Group saveGroup(String adminId, Group group) throws CustomException;

    Group getGroupById(String adminId, String groupId);

    List<Group> getAllGroupsByMemberId(String memberId);

    List<GroupMembership> getAllMembersOf(String groupId);

    List<GroupMembership> getEnabledMembershipForMemberId(String memberId);

    boolean deleteGroupById(String adminId, String groupId) throws CustomException;

    boolean removeMemberFromGroup(String adminId, String userId, String groupId) throws CustomException;

    GroupMembership addMemberToGroup(String adminId, GroupMembership groupMembership) throws CustomException;
}
