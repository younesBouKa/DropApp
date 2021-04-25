package server.user.services;

import server.exceptions.CustomException;
import server.user.data.User;
import server.user.models.JwtResponse;
import server.user.models.SignInRequest;
import server.user.models.SignUpRequest;

import java.util.List;

public interface IUserService {

    JwtResponse authenticate(SignInRequest signInRequest);
    User registerUser(SignUpRequest signUpRequest) throws CustomException;
    User saveUser(User user);
    User getUserByUsername(String name);
    List<User> getUsersByName(String name);
    List<String> getAllUsernames();
    int deleteByUsername(String name);
    User updateUserByName(String name, String password, boolean enabled, boolean admin);
}
