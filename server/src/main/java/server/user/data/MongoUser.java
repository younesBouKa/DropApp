package server.user.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import server.data.IRole;
import server.data.IUser;

import java.time.Instant;
import java.util.*;

@CompoundIndexes({
        @CompoundIndex(name = "username_email", def = "{'username':1,'email':1}", unique = true),
        @CompoundIndex(name = "_id", def = "{'_id':1}", unique = true)
})
@Document("user")
public class MongoUser implements IUser {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private boolean enabled;
    private boolean admin;
    private int maxIdleTime = -1;
    private Instant birthDate;
    private Set<IRole> roles = new HashSet<>(); // for web authentication
    private String homeDirectory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public String getHomeDirectory() {
        return Optional.of(homeDirectory).orElse("home");
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Set<IRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<IRole> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getBirthDay() {
        return birthDate;
    }

    public void setBirthDay(Instant birthDay) {
        this.birthDate = birthDay;
    }
}
