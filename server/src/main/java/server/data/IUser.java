package server.data;



import java.util.Set;

public interface IUser {

    String getId();

    String getUsername();

    String getPassword();

    boolean isEnabled();

    int getMaxIdleTime();

    String getHomeDirectory();

    boolean isAdmin();

    Set<IRole> getRoles();

    String getEmail();
}
