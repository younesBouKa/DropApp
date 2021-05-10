package server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.data.Group;

import java.util.List;
import java.util.Optional;

public interface IGroupRepo extends MongoRepository<Group, String> {

    Optional<Group> findById(String id);
    List<Group> findByIdIn(List<String> groupIds);
}
