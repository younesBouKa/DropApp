package server.user.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import server.exceptions.CustomException;
import server.security.jwt.JwtUtils;
import server.user.data.Role;
import server.user.data.User;
import server.user.models.JwtResponse;
import server.user.models.SignInRequest;
import server.user.models.SignUpRequest;
import server.user.repositories.IRoleRepo;
import server.user.repositories.IUserRepo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static server.exceptions.Message.EMAIL_ALREADY_TAKEN;
import static server.exceptions.Message.USERNAME_ALREADY_TAKEN;

@Component
public class UserMongoService implements IUserService {

    @Autowired
    IRoleRepo roleRepo;
    @Autowired
    IUserRepo userRepo;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;

    public Function<SignUpRequest, User> mapSignUpRequestToUser = (signUpRequest -> {
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setEnabled(true);
        user.setHomeDirectory(signUpRequest.getHomeDirectory());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setAdmin(false); // to fill authorities
        Set<Role> roles = signUpRequest.getRoles()
                .stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .map(name-> roleRepo.findByName(name))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if(roles.isEmpty()){
            roles.add(roleRepo.findByName("USER"));// by default
        }
        user.setRoles(roles);
        return user;
    });

    @Override
    public List<User> getUsersByName(String name) {
        return userRepo.getAllByUsername(name);
    }

    @Override
    public List<String> getAllUsernames() {
        return userRepo.getDistinctByUsername().stream().map(User::getUsername).collect(Collectors.toList());
    }

    @Override
    public int deleteByUsername(String name) {
        this.userRepo.deleteByUsername(name);
        return 1;
    }

    @Override
    public JwtResponse authenticate(SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Set<Role> roles = userDetails.getAuthorities().stream()
                .map(item -> roleRepo.findByName(item.getAuthority()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        Role userRole = roleRepo.findByName("USER");
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        return userRepo.save(user);
    }

    @Override
    public User registerUser(SignUpRequest signUpRequest) throws CustomException {
        if (userRepo.existsByUsername(signUpRequest.getUsername())) {
            throw new CustomException(USERNAME_ALREADY_TAKEN, signUpRequest.getUsername());
        }
        if (userRepo.existsByEmail(signUpRequest.getEmail())) {
            throw new CustomException(EMAIL_ALREADY_TAKEN, signUpRequest.getEmail());
        }
        // Create new user's account
        User user = mapSignUpRequestToUser.apply(signUpRequest);
        return userRepo.save(user);
    }

    @Override
    public User getUserByUsername(String name) {
        return userRepo.findByUsername(name);
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
