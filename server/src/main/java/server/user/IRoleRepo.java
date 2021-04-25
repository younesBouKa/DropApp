package server.user;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface IRoleRepo extends MongoRepository<Role,String> {

    Role findByRole(String role);
}
