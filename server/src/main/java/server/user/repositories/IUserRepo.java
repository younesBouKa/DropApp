package server.user.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.user.data.MongoUser;

import java.util.List;

public interface IUserRepo extends MongoRepository<MongoUser,String> {

    MongoUser findByUsername(String username);
    List<MongoUser> getAllByUsername(String name);
    List<MongoUser> getDistinctByUsername();
    void deleteByUsername(String name);

    boolean existsByUsername(String username);
    boolean existsByEmail(String username);
}
