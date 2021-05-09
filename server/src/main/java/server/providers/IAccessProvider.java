package server.providers;

import server.data.Access;
import server.data.Permissions;
import server.exceptions.CustomException;

import java.util.List;

public interface IAccessProvider{

     boolean hasPermission(String resourceId, String requesterId, Permissions permission);
     void assertPermission(String resourceId, String requesterId, Permissions permission) throws CustomException;
     boolean addPermission(String resourceId, String requesterId, Permissions permission);
     boolean addAllPermissions(String resourceId, String requesterId);
     boolean deletePermission(String resourceId, String requesterId, Permissions permission);

     void deleteAllPermissions(String resourceId, String requesterId);

     List<Access> getAllAccessForRequester(String requesterId);
    Access getPermission(String requesterId, String resourceId);
}
