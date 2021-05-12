package server.user.models;

import server.data.IRole;

import java.io.Serializable;
import java.util.Set;

public class JwtResponse implements Serializable {

    private String id;
    private String token;
    private long refreshDelayInMillis;
    private String type;
    private String username;
    private String email;
    private Set<IRole> roles;

    public JwtResponse(String token, long refreshDelayInMillis, String id, String username, String email, Set<IRole> roles) {
        this.id = id;
        this.token = token;
        this.refreshDelayInMillis = refreshDelayInMillis;
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

    public Set<IRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<IRole> roles) {
        this.roles = roles;
    }

    public long getRefreshDelayInMillis() {
        return refreshDelayInMillis;
    }

    public void setRefreshDelayInMillis(long refreshDelayInMillis) {
        this.refreshDelayInMillis = refreshDelayInMillis;
    }
}
