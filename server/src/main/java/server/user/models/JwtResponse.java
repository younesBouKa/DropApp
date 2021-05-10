package server.user.models;

import server.user.data.Role;

import java.io.Serializable;
import java.util.Set;

public class JwtResponse implements Serializable {

    private String id;
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private Set<Role> roles;

    public JwtResponse(String token, String id, String username, String email, Set<Role> roles) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
