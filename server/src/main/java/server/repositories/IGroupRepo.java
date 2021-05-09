package server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.data.Group;

import java.util.List;
import java.util.Optional;

public interface IGroupRepo extends MongoRepository<Group, String> {

    Optional<Group> findById(String id);
    Group findByIdAndAdminIdAndName(String groupId, String adminId, String name);
    Group findByIdAndAdminId(String groupId, String adminId);
    List<Group> findByIdIn(List<String> groupIds);
    List<Group> findByAdminId(String adminId);
    void deleteByIdAndAdminId(String groupId, String adminId);

}
