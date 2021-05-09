package server.services;

import server.data.Access;
import server.data.Permissions;

import java.util.List;

public interface IAccessService {

    List<Access> getAccess(String requesterId);

    Access getAccess(String requesterId, String resourceId);

    boolean addPermission(String userId, String requesterId, String resourceId, int compoundPermission);

    boolean removePermission(String userId, String requesterId, String resourceId, int compoundPermission);

    boolean addPermission(String userId, String requesterId, String resourceId, Permissions permission);

    boolean removePermission(String userId, String requesterId, String resourceId, Permissions permission);

    void removeAllPermissions(String userId, String requesterId, String resourceId);

    String generateAccessToken(String userId, Access access);
}
