package server.user.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.data.IUser;
import server.exceptions.CustomException;
import server.models.UserResponse;
import server.user.models.JwtResponse;
import server.user.models.SignInRequest;
import server.user.models.SignUpRequest;
import server.user.services.CustomUserDetails;
import server.user.services.IUserService;

import java.util.logging.Logger;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v0/auth")
public class AuthController {
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    private final IUserService userService;

    @Autowired
    public AuthController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/signup",consumes = APPLICATION_JSON_VALUE)
    public UserResponse register(@RequestBody SignUpRequest signUpRequest) throws CustomException {
        IUser user =  userService.registerUser(signUpRequest);
        return new UserResponse(user);
    }

    @PostMapping(value = "/login",consumes = APPLICATION_JSON_VALUE)
    public JwtResponse authenticate(@RequestBody SignInRequest signInRequest) throws CustomException {
        return userService.authenticate(signInRequest);
    }

    /********** tools **********************/
    public static IUser currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

}
