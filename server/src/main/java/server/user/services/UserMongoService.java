package server.user.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import server.data.IRole;
import server.data.IUser;
import server.exceptions.CustomException;
import server.security.jwt.JwtUtils;
import server.user.data.MongoUser;
import server.user.data.Role;
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

@Service
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

    public Function<SignUpRequest, IUser> mapSignUpRequestToUser = (signUpRequest -> {
        MongoUser user = new MongoUser();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setEnabled(true);
        //user.setHomeDirectory(signUpRequest.getHomeDirectory());// TODO perhaps i'll use it later
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setAdmin(true); // to fill authorities
        Set<IRole> roles = signUpRequest.getRoles()
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
    public List<IUser> getUsersByName(String name) {
        return userRepo.getAllByUsername(name)
                .stream()
                .map(user-> (IUser) user)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllUsernames() {
        return userRepo.getDistinctByUsername().stream().map(IUser::getUsername).collect(Collectors.toList());
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
    public IUser saveUser(IUser user) {
        MongoUser mongoUser = (MongoUser)user;
        mongoUser.setPassword(passwordEncoder.encode(user.getPassword()));
        mongoUser.setEnabled(true);
        mongoUser.setAdmin(true);
        Role role = roleRepo.findByName("ADMIN");
        mongoUser.setRoles(new HashSet<>(Collections.singletonList(role)));
        return userRepo.save(mongoUser);
    }

    @Override
    public MongoUser registerUser(SignUpRequest signUpRequest) throws CustomException {
        if (userRepo.existsByUsername(signUpRequest.getUsername())) {
            throw new CustomException(USERNAME_ALREADY_TAKEN, signUpRequest.getUsername());
        }
        if (userRepo.existsByEmail(signUpRequest.getEmail())) {
            throw new CustomException(EMAIL_ALREADY_TAKEN, signUpRequest.getEmail());
        }
        // Create new user's account
        IUser user = mapSignUpRequestToUser.apply(signUpRequest);
        return userRepo.save((MongoUser) user);
    }

    @Override
    public MongoUser getUserByUsername(String name) {
        return userRepo.findByUsername(name);
    }

    @Override
    public MongoUser updateUserByName(String name, String password, boolean enabled, boolean admin) {
        MongoUser user = new MongoUser();
        user.setUsername(name);
        user.setEnabled(enabled);
        user.setPassword(password);
        user.setAdmin(admin);
        return this.userRepo.save(user);
    }
}
