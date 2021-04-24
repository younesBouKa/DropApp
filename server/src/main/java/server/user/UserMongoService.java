package server.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserMongoService implements IUserService {

    @Autowired
    IRoleRepo roleRepo;
    @Autowired
    IUserRepo userRepo;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public List<User> getUsersByName(String name) {
        return userRepo.getAllByUsername(name);
    }

    @Override
    public List<String> getAllUsernames() {
        return userRepo.getDistinctByUsername().stream().map(user -> user.getUsername()).collect(Collectors.toList());
    }

    @Override
    public int deleteByUsername(String name) {
        this.userRepo.deleteByUsername(name);
        return 1;
    }

    @Override
    public User saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        Role userRole = roleRepo.findByRole("USER");
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));
        return userRepo.save(user);
    }

    @Override
    public User updateUserByName(String name, String password, boolean enabled, boolean admin) {
        User user = new User();
        user.setUsername(name);
        user.setEnabled(enabled);
        user.setPassword(password);
        user.setAdmin(admin);
        return this.userRepo.save(user);
    }
}
