package server.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IUserRepo extends MongoRepository<User,String> {

    User findByUsername(String username);
    List<User> getAllByUsername(String name);
    List<User> getDistinctByUsername();
    void deleteByUsername(String name);
}
