package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import server.data.Group;
import server.data.GroupMembership;
import server.data.IUser;
import server.dot.GroupDotIn;
import server.dot.MembershipDotIn;
import server.exceptions.CustomException;
import server.services.IGroupService;
import server.user.services.CustomUserDetails;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v0/groups")
public class GroupController {

    private static final Logger logger = Logger.getLogger(GroupController.class.getName());

    private final IGroupService groupService;

    @Autowired
    public GroupController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("")
    public List<Group> listOwnGroups() throws CustomException {
        return groupService.getGroupsToBellow(currentUser().getId());
    }

    @GetMapping("/{groupId}")
    public List<GroupMembership> listGroupMembers(@PathVariable(name = "groupId") String groupId) throws CustomException {
        return groupService.getAllMembersOfGroup(currentUser().getId(), groupId);
    }

    @PostMapping
    public Group addGroup(@RequestBody GroupDotIn groupDotIn) throws CustomException {
        return groupService.addGroup(currentUser().getId(), groupDotIn);
    }

    @PutMapping("/{groupId}")
    public Group updateGroup(@PathVariable(name = "groupId") String groupId ,
                             @RequestBody GroupDotIn groupDotIn) throws CustomException {
        return groupService.updateGroup(currentUser().getId(), groupId, groupDotIn);
    }

    @DeleteMapping("/{groupId}")
    public boolean deleteGroup(@PathVariable(name = "groupId") String groupId ) throws CustomException {
        return groupService.deleteGroup(currentUser().getId(), groupId);
    }

    @PostMapping("/{groupId}/addMember")
    public GroupMembership addMemberToGroup(@PathVariable(name = "groupId") String groupId ,
                                            @RequestBody MembershipDotIn membershipDotIn) throws CustomException {
        membershipDotIn.setGroupId(groupId);
        return groupService.addMemberToGroup(currentUser().getId(), membershipDotIn);
    }

    @DeleteMapping("/{groupId}/{memberId}")
    public boolean removeMemberFromGroup(@PathVariable(name = "groupId") String groupId,
                                         @PathVariable(name = "memberId") String memberId ) throws CustomException {
        return groupService.removeMemberFromGroup(currentUser().getId(), memberId, groupId);
    }
    /***** tools *******************************/
    public static IUser currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
