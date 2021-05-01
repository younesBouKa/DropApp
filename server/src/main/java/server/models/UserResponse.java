package server.models;

import server.data.IRole;
import server.data.IUser;

import java.util.Set;

public class UserResponse {

    private final String id;
    private final String username;
    private final String email;
    private final String homeDirectory;
    private final Set<IRole> roles;

    public UserResponse(IUser user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.homeDirectory = user.getHomeDirectory();
        this.roles = user.getRoles();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public Set<IRole> getRoles() {
        return roles;
    }
}
