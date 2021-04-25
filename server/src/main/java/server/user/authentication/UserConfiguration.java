package server.user.authentication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.user.repositories.IRoleRepo;
import server.user.data.Role;
import server.user.services.CustomUserDetailsService;

import java.util.Arrays;
import java.util.List;

@Configuration
public class UserConfiguration {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Bean
    public UserDetailsService mongoUserDetails() {
        return new CustomUserDetailsService();
    }

    @Bean
    CommandLineRunner init(IRoleRepo roleRepository) {
        return args -> {
            List<String> rolesName = Arrays.asList("ADMIN", "USER", "MODERATOR");
            rolesName.forEach(name ->{
                Role adminRole = roleRepository.findByName(name);
                if (adminRole == null) {
                    Role newAdminRole = new Role(name);
                    roleRepository.save(newAdminRole);
                }
            });
        };

    }
}