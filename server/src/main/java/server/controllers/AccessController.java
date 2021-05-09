package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import server.data.Access;
import server.data.IUser;
import server.exceptions.CustomException;
import server.services.IAccessService;
import server.user.services.CustomUserDetails;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v0/access")
public class AccessController {

    private static final Logger logger = Logger.getLogger(AccessController.class.getName());

    private final IAccessService accessService;

    @Autowired
    public AccessController(IAccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/{resourceId}")
    public Access getPermission(@PathVariable(name = "resourceId") String resourceId) throws CustomException {
        return accessService.getAccess(currentUser().getId(),resourceId);
    }

    @PostMapping("/add")
    public boolean addPermission(@RequestBody Access access) throws CustomException {
        // TODO must create a DOT for access request
        Assert.notNull(access.getResourceId(), "Resource id can't be null");
        Assert.notNull(access.getRequesterId(), "Requester id can't be null");
        return accessService.addPermission(currentUser().getId(),access.getRequesterId(), access.getResourceId(), access.getPermission());
    }

    @DeleteMapping("/remove")
    public boolean removePermission(@RequestBody Access access) throws CustomException {
        // TODO must create a DOT for access request
        Assert.notNull(access.getResourceId(), "Resource id can't be null");
        Assert.notNull(access.getRequesterId(), "Requester id can't be null");
        return accessService.removePermission(currentUser().getId(),access.getRequesterId(), access.getResourceId(), access.getPermission());
    }

    @DeleteMapping("/removeAll")
    public void removeAllPermission(@RequestBody Access access) throws CustomException {
        // TODO must create a DOT for access request
        Assert.notNull(access.getResourceId(), "Resource id can't be null");
        Assert.notNull(access.getRequesterId(), "Requester id can't be null");
        accessService.removeAllPermissions(currentUser().getId(),access.getRequesterId(), access.getResourceId());
    }

    @PostMapping("/generateToken")
    public String generateAccessToken(@RequestBody Access access) throws CustomException {
        // TODO must create a DOT for access token request
        Assert.notNull(access.getResourceId(), "Resource id can't be null");
        return accessService.generateAccessToken(currentUser().getId(),access);
    }

    /***** tools *******************************/
    public static IUser currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
