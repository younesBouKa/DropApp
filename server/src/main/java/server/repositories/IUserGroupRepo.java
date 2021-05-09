package server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.data.UserGroup;

import java.util.List;
import java.util.Optional;

public interface IUserGroupRepo extends MongoRepository<UserGroup, String> {

    Optional<UserGroup> findById(String id);
    List<UserGroup> findByUserId(String userId);
    List<UserGroup> findByUserIdAndEnable(String userId, boolean enable);
    void deleteById(String id);
    void deleteAllByGroupId(String groupId);
    void deleteByUserIdAndGroupId(String userId, String groupId);
}
