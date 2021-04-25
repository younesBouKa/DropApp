package server.user.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.user.data.Role;


public interface IRoleRepo extends MongoRepository<Role,String> {

    Role findByName(String name);
}
