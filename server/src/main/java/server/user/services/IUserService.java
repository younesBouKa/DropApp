package server.user.services;

import server.data.IUser;
import server.exceptions.CustomException;
import server.user.models.JwtResponse;
import server.user.models.SignInRequest;
import server.user.models.SignUpRequest;

import java.util.List;

public interface IUserService {

    JwtResponse authenticate(SignInRequest signInRequest);
    JwtResponse refreshToken(String token) throws CustomException;
    IUser registerUser(SignUpRequest signUpRequest) throws CustomException;
    IUser saveUser(IUser user);
    IUser getUserByUsername(String name);
    List<IUser> getUsersByName(String name);
    List<String> getAllUsernames();
    int deleteByUsername(String name);
    IUser updateUserByName(String name, String password, boolean enabled, boolean admin);
}
