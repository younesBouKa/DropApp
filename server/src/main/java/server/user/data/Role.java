package server.user.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import server.data.IRole;

@CompoundIndexes({
        @CompoundIndex(name = "name", def = "{'name':1}", unique = true),
        @CompoundIndex(name = "_id", def = "{'_id':1}", unique = true)
})
@Document("role")
public class Role implements IRole {
    @Id
    private String id;
    private String name;

    public Role(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String role) {
        this.name = role;
    }
}
