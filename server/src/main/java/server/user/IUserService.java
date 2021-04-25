package server.user;

import java.util.List;

public interface IUserService {
    User saveUser(User user);
    List<User> getUsersByName(String name);
    List<String> getAllUsernames();
    int deleteByUsername(String name);
    User updateUserByName(String name, String password, boolean enabled, boolean admin);
}
