package server.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.Access;
import server.data.GroupMembership;
import server.data.Permissions;
import server.exceptions.CustomException;
import server.repositories.IAccessRepo;
import server.tools.Cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static server.exceptions.Message.*;

@Component
public class AccessProvider implements IAccessProvider{

    private final Cache<String, Access> accessCache = Cache.getInstance("access");
    private final Set<String> groupsAlreadyVisited = new HashSet<>();

    @Autowired
    IAccessRepo accessRepo;
    @Autowired
    IGroupProvider groupProvider;

    @Override
    public boolean hasPermission(String resourceId, String requesterId, Permissions permission){
        Access access = getAccess(resourceId, requesterId);
        if(access!=null && permission.isIn(access.getPermission()))
            return true;
        // following lines can be removed if no groups are implemented
        try {
            return hasAccessFromParents(resourceId, requesterId, permission);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
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

    @Override
    public void deleteAllPermissions(String resourceId, String requesterId){
        accessRepo.deleteByResourceIdAndRequesterId(resourceId, requesterId);
        removeFromCache(resourceId, requesterId);
    }

    @Override
    public List<Access> getAllAccessForRequester(String requesterId) {
        return accessRepo.findAllByRequesterId(requesterId);
    }

    @Override
    public Access getPermission(String requesterId, String resourceId) {
        return getAccess(resourceId, requesterId);
    }

    /********** tools *****************************/
    public boolean hasAccessFromParents(String resourceId, String requesterId, Permissions permission) throws CustomException {
        groupsAlreadyVisited.clear();
        groupsAlreadyVisited.add(requesterId);
        return walkThroughParents(resourceId, requesterId, permission);
    }

    private boolean walkThroughParents(String resourceId, String requesterId, Permissions permission) throws CustomException {
        // test access exist directly
        Access access = getAccess(resourceId, requesterId);
        if(access!=null && permission.isIn(access.getPermission()))
            return true;
        // else pass to parents
        List<GroupMembership> userGroupList = groupProvider.getEnabledMembershipForMemberId(requesterId); // get only enabled ones
        if(userGroupList!=null && !userGroupList.isEmpty()){
            List<String> groupIds = userGroupList
                    .stream()
                    .map(GroupMembership::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());
            for(String groupId : groupIds){
                if(groupsAlreadyVisited.contains(groupId)){
                    throw new CustomException(ACCESS_LOOP_DETECTED, groupId, requesterId);
                }
                groupsAlreadyVisited.add(groupId);
                boolean hasAccess = walkThroughParents(resourceId, groupId, permission);
                if(hasAccess){
                    return true;
                }
            }
        }
        return false;
    }

    public Access getAccess(String resourceId, String requesterId){
        Access access = accessCache.get(resourceId, (acc)-> requesterId.equals(acc.getRequesterId()));
        if(access==null){
            access = accessRepo.findByResourceIdAndRequesterId(resourceId, requesterId);
            accessCache.add(resourceId,access);
        }
        return access;
    }

    public void updateCache(String resourceId, String requesterId, Access access){
        accessCache.update(resourceId, access, acc-> requesterId.equals(acc.getRequesterId()));
    }

    public void removeFromCache(String resourceId, String requesterId){
        accessCache.delete(resourceId, acc -> requesterId.equals(acc.getRequesterId()));
    }
}
