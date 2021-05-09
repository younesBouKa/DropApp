package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import server.data.Group;
import server.data.IUser;
import server.data.UserGroup;
import server.exceptions.CustomException;
import server.services.IGroupService;
import server.user.services.CustomUserDetails;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v0/group")
public class GroupController {

    private static final Logger logger = Logger.getLogger(GroupController.class.getName());

    private final IGroupService groupService;

    @Autowired
    public GroupController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("")
    public List<Group> listOwnGroups() throws CustomException {
        return groupService.getOwnGroups(currentUser().getId());
    }

    @GetMapping("/bellow")
    public List<Group> listGroupsToBellow() throws CustomException {
        return groupService.getGroupsToBellow(currentUser().getId());
    }

    @PostMapping
    public Group addGroup(@RequestBody Group group) throws CustomException {
        // TODO add dot here
        return groupService.addGroup(currentUser().getId(), group);
    }

    @PutMapping("/{groupId}")
    public Group updateGroup(@PathVariable(name = "groupId") String groupId , @RequestBody Group group) throws CustomException {
        // TODO add dot here
        return groupService.updateGroup(currentUser().getId(), groupId, group);
    }

    @DeleteMapping("/{groupId}")
    public boolean addGroup(@PathVariable(name = "groupId") String groupId ) throws CustomException {
        return groupService.deleteGroup(currentUser().getId(), groupId);
    }

    @PostMapping("/addUser")
    public UserGroup addUserToGroup(@RequestBody UserGroup userGroup) throws CustomException {
        // TODO add dot here
        return groupService.addUserToGroup(currentUser().getId(), userGroup.getUserId(), userGroup.getGroupId());
    }

    @DeleteMapping("/{groupId}/{userId}")
    public boolean removeUserFromGroup(@PathVariable(name = "groupId") String groupId, @PathVariable(name = "userId") String userId ) throws CustomException {
        return groupService.removeUserFromGroup(currentUser().getId(), userId, groupId);
    }
    /***** tools *******************************/
    public static IUser currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
