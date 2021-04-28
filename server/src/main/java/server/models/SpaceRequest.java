package server.models;

import server.data.IUser;
import server.data.Space;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SpaceRequest implements Serializable {

    private String name;
    Map<String, Object> fields = new HashMap<>();

    public static Space spaceFromRequest(IUser user, SpaceRequest spaceRequest) {
        Space space = updateSpaceWithRequest(new Space(), user, spaceRequest);
        return space;
    }

    public static Space updateSpaceWithRequest(Space space, IUser user, SpaceRequest spaceRequest) {
        space.setName(spaceRequest.getName());
        space.setOwnerId(user.getId());
        space.setRootPath(user.getHomeDirectory());
        Map<String, Object> fields = spaceRequest.getFields();
        if(fields!=null){
            for(String key : fields.keySet()){
                space.getFields().put(key, fields.get(key));
            }
        }
        return space;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
