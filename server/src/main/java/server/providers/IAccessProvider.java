package server.providers;

import server.data.Permissions;
import server.exceptions.CustomException;

public interface IAccessProvider{

     boolean hasPermission(String resourceId, String requesterId, Permissions permission);
     void assertPermission(String resourceId, String requesterId, Permissions permission) throws CustomException;
     boolean addPermission(String resourceId, String requesterId, Permissions permission);
     boolean addAllPermissions(String resourceId, String requesterId);
     boolean deletePermission(String resourceId, String requesterId, Permissions permission);

}
