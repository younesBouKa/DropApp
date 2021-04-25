package server.user.models;

import java.io.Serializable;

public class SignInRequest implements Serializable {

    private String username;
    private String password;

    SignInRequest(String username, String password){
        this.username=username;
        this.password=password;
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
}
