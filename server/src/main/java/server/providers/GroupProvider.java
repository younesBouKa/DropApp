package server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.Group;
import server.data.GroupMembership;
import server.exceptions.CustomException;
import server.repositories.IGroupRepo;
import server.repositories.IUserGroupRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static server.exceptions.Message.*;

@Component
public class GroupProvider implements IGroupProvider{

    @Autowired
    IGroupRepo groupRepo;
    @Autowired
    IUserGroupRepo userGroupRepo;
    private final Set<String> groupsAlreadyVisited = new HashSet<>();

    @Override
    public Group insertGroup(String adminId, Group group) {
        Group savedGroup = groupRepo.insert(group);
        GroupMembership groupMembership = userGroupRepo.insert(
                    new GroupMembership(savedGroup.getId(), adminId, "ADMIN")
            );
        return savedGroup;
    }

    @Override
    public Group saveGroup(String adminId, Group group) throws CustomException {
        Group foundedGroup = groupRepo.findById(group.getId()).orElseThrow(()->new CustomException(GROUP_NOT_FOUND, group.getId()));
        if(isNotAdminInGroup(adminId, foundedGroup.getId()))
                throw new CustomException(NOT_ADMIN_IN_GROUP, foundedGroup.getId());
        return groupRepo.save(group);
    }

    @Override
    public Group getGroupById(String adminId, String groupId) {
        // TODO perhaps add some control here
        return groupRepo.findById(groupId).orElse(null);
    }

    @Override
    public List<Group> getAllGroupsByMemberId(String memberId) {
        List<GroupMembership> userGroupList = userGroupRepo.findByMemberIdAndEnable(memberId,true);
        List<String> groupIds = userGroupList.stream().map(GroupMembership::getGroupId).collect(Collectors.toList());
        return groupRepo.findByIdIn(groupIds);
    }

    @Override
    public List<GroupMembership> getAllMembersOf(String groupId) {
        return userGroupRepo.findByGroupId(groupId);
    }

    @Override
    public List<GroupMembership> getEnabledMembershipForMemberId(String memberId) {
        return userGroupRepo.findByMemberIdAndEnable(memberId, true);
    }

    @Override
    public boolean deleteGroupById(String adminId, String groupId) throws CustomException {
        groupRepo.findById(groupId).orElseThrow(()->new CustomException(GROUP_NOT_FOUND, groupId));
        if(isNotAdminInGroup(adminId, groupId))
            throw new CustomException(NOT_ADMIN_IN_GROUP, groupId);
        groupRepo.deleteById(groupId);
        userGroupRepo.deleteAllByGroupId(groupId);
        return true;
    }

    @Override
    public boolean removeMemberFromGroup(String userId, String memberId, String groupId) throws CustomException {
        groupRepo.findById(groupId).orElseThrow(()->new CustomException(GROUP_NOT_FOUND, groupId));
        // TODO user can remove himself from group AND admin in group can remove anyone
        if(!userId.equals(memberId) && isNotAdminInGroup(userId, groupId))
            throw new CustomException(NOT_ADMIN_IN_GROUP, groupId);
        GroupMembership groupMembership = userGroupRepo.findByGroupIdAndMemberId(groupId, memberId);
        if(groupMembership==null)
            throw new CustomException(MEMBERSHIP_NOT_FOUND,groupId, memberId);
        userGroupRepo.deleteByMemberIdAndGroupId(memberId, groupId);
        return true;
    }

    @Override
    public GroupMembership addMemberToGroup(String adminId, GroupMembership groupMembership) throws CustomException {
        String memberId = groupMembership.getMemberId();
        String groupId = groupMembership.getGroupId();
        groupRepo.findById(groupId).orElseThrow(()->new CustomException(GROUP_NOT_FOUND, groupId));
        if(memberId.equals(groupId))
            throw new CustomException(GROUP_CANT_BE_MEMBER_OF_ITSELF, groupId);
        if(isNotAdminInGroup(adminId, groupId))
            throw new CustomException(NOT_ADMIN_IN_GROUP, groupId);
        boolean loopDetected = isMembershipLoopDetected(groupId, memberId);
        if(loopDetected)
            throw new CustomException(GROUPS_MEMBERSHIP_LOOP, groupId, memberId);
        // create a new membership or update if already exists
        GroupMembership foundedMembership = userGroupRepo.findByGroupIdAndMemberId(groupId, memberId);
        if(foundedMembership!=null && foundedMembership.isEnable())// exist and enable
            return foundedMembership;
        else if(foundedMembership==null){ // doesn't exist
            return userGroupRepo.insert(groupMembership);
        }else{ // exist and disabled
            foundedMembership.setEnable(true);
            return userGroupRepo.save(foundedMembership);
        }
    }

    /********** tools *********************************/
    public boolean isMembershipLoopDetected(String groupId, String memberId) throws CustomException {
        groupsAlreadyVisited.clear();
        groupsAlreadyVisited.add(groupId);
        groupsAlreadyVisited.add(memberId);
        return walkThroughParents(groupId, memberId);
    }

    private boolean walkThroughParents(String groupId, String memberId){
        // test if reverse relation exists
        GroupMembership byGroupIdAndMemberId = userGroupRepo.findByGroupIdAndMemberId(memberId, groupId);
        if(byGroupIdAndMemberId!=null){
            return true;
        }
        // else pass to parents
        List<GroupMembership> groupList = userGroupRepo.findByMemberId(groupId); // get also disabled membership
        for(GroupMembership membership : groupList){
            if(groupsAlreadyVisited.contains(groupId)){
                return true;
            }
            groupsAlreadyVisited.add(membership.getGroupId());
            boolean loopDetected = walkThroughParents(membership.getGroupId(), groupId);
            if(loopDetected)
                return true;
        }
        return false;
    }

    public boolean isNotAdminInGroup(String memberId, String groupId){
        // TODO can be customized later using more roles
        GroupMembership groupMembership = userGroupRepo.findByGroupIdAndMemberId(groupId, memberId);
        return !groupMembership.getRole().equals("ADMIN");
    }
}
