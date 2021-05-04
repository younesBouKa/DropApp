package server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import server.user.data.Role;
import server.user.repositories.IRoleRepo;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication(scanBasePackages={"server"})
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Bean
	CommandLineRunner init(IRoleRepo roleRepository) {
		return args -> {
			List<String> rolesName = Arrays.asList("ADMIN", "USER", "MODERATOR");
			rolesName.forEach(name ->{
				Role bdRole = roleRepository.findByName(name);
				if (bdRole == null) {
					Role role = new Role(name);
					roleRepository.save(role);
				}
			});
		};
	}
}
