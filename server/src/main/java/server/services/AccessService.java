package server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.data.Access;
import server.data.Permissions;
import server.providers.IAccessProvider;

import java.util.List;

@Service
public class AccessService implements IAccessService {

    @Autowired
    IAccessProvider accessProvider;

    @Override
    public List<Access> getAccess(String requesterId) {
        return accessProvider.getAllAccessForRequester(requesterId);
    }

    @Override
    public Access getAccess(String requesterId, String resourceId) {
        return accessProvider.getPermission(requesterId, resourceId);
    }

    @Override
    public boolean addPermission(String userId, String requesterId, String resourceId, int compoundPermission) {
        // TODO add control one userId here
        List<Permissions> permissions = Permissions.getAllFrom(compoundPermission);
        boolean atLeastOneAdded = false;
        for(Permissions perm : permissions)
            atLeastOneAdded = atLeastOneAdded | accessProvider.addPermission(resourceId, requesterId, perm);
        return atLeastOneAdded;
    }

    @Override
    public boolean removePermission(String userId, String requesterId, String resourceId, int compoundPermission) {
        // TODO add control one userId here
        List<Permissions> permissions = Permissions.getAllFrom(compoundPermission);
        boolean atLeastOneRemoved = false;
        for(Permissions perm : permissions)
            atLeastOneRemoved = atLeastOneRemoved | accessProvider.deletePermission(resourceId, requesterId, perm);
        return atLeastOneRemoved;
    }

    @Override
    public boolean addPermission(String userId, String requesterId, String resourceId, Permissions permission) {
        // TODO add control one userId here
        return accessProvider.addPermission(resourceId, requesterId, permission);
    }

    @Override
    public boolean removePermission(String userId, String requesterId, String resourceId, Permissions permission) {
        // TODO add control one userId here
        return accessProvider.deletePermission(resourceId, requesterId, permission);
    }

    @Override
    public void removeAllPermissions(String userId, String requesterId, String resourceId) {
        // TODO add control one userId here
        accessProvider.deleteAllPermissions(resourceId, requesterId);
    }

    @Override
    public String generateAccessToken(String userId, Access access) {
        // TODO add control one userId here
        // TODO more work
        // 1- get user
        // 2- validate if user has permission on resource
        // 3- generate token with resource id, requester id , permission, delay
        return null;
    }
}
