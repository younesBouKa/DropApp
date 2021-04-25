package server.user.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import server.user.data.User;
import server.user.services.CustomUserDetails;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v0/test")
public class TestController {
    private static final Logger logger = Logger.getLogger(TestController.class.getName());

    @GetMapping("/all")
    public Object allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Object userAccess() {
        return "User Content.";
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public Object moderatorAccess() {
        return "Moderator Board.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Object adminAccess() {
        return "Admin Board.";
    }

    /********** tools **********************/
    public static User currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
