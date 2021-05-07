package server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.Access;
import server.data.Permissions;
import server.exceptions.CustomException;
import server.repositories.IAccessRepo;
import server.tools.Cache;

import static server.exceptions.Message.NO_PERMISSION;

@Component
public class AccessProvider implements IAccessProvider{

    Cache<String, Access> accessCache = Cache.getInstance("access");

    @Autowired
    IAccessRepo accessRepo;

    @Override
    public boolean hasPermission(String resourceId, String requesterId, Permissions permission){
        Access access = getAccess(resourceId, requesterId);
        return access!=null && permission.isIn(access.getPermission());
    }

    @Override
    public void assertPermission(String resourceId, String requesterId, Permissions permission) throws CustomException {
        if(!hasPermission(resourceId, requesterId, permission))
            throw new CustomException(NO_PERMISSION, Permissions.READ, resourceId);
    }

    @Override
    public boolean addPermission(String resourceId, String requesterId, Permissions permission){
        Access foundAccess = getAccess(resourceId, requesterId);
        if (foundAccess==null)
            foundAccess = accessRepo.insert(new Access(resourceId, requesterId, permission.getCode()));
        else if(! permission.isIn(foundAccess.getPermission())){
            foundAccess.setPermission( permission.addTo(foundAccess.getPermission()));
            foundAccess = accessRepo.save(foundAccess);
        }
        updateCache(resourceId, requesterId, foundAccess);
        return permission.isIn(foundAccess.getPermission());
    }

    @Override
    public boolean addAllPermissions(String resourceId, String requesterId){
        Access foundAccess = getAccess(resourceId, requesterId);
        if (foundAccess==null)
            foundAccess = accessRepo.insert(new Access(resourceId, requesterId, Permissions.all()));
        else {
            foundAccess.setPermission( Permissions.all());
            foundAccess = accessRepo.save(foundAccess);
        }
        updateCache(resourceId, requesterId, foundAccess);
        return (foundAccess.getPermission()&Permissions.all()) == Permissions.all();
    }

    @Override
    public boolean deletePermission(String resourceId, String requesterId, Permissions permission){
        Access foundAccess = getAccess(resourceId, requesterId);
        if (foundAccess!=null){
            foundAccess.setPermission( permission.removeFrom(foundAccess.getPermission()));
            foundAccess = accessRepo.save(foundAccess);
            updateCache(resourceId, requesterId, foundAccess);
            return (foundAccess.getPermission()&permission.getCode()) != permission.getCode();
        }else
            return true;
    }

    /********** tools *****************************/
    public Access getAccess(String resourceId, String requesterId){
        Access access = accessCache.get(resourceId, (acc)-> requesterId.equals(acc.getRequesterId()));
        if(access==null){
            access = accessRepo.findByResourceIdAndRequesterId(resourceId, requesterId);
            accessCache.add(resourceId,access);
        }
        return access;
    }

    public boolean updateCache(String resourceId, String requesterId, Access access){
        Access updatedAccess = accessCache.update(resourceId, access, acc-> requesterId.equals(acc.getRequesterId()));
        return updatedAccess!=null;
    }
}
