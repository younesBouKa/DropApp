package server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.data.Access;

import java.util.List;

public interface IAccessRepo  extends MongoRepository<Access, String> {

    List<Access> findAllByRequesterId(String requesterId);
    List<Access> findAllByResourceId(String resourceId);
    List<Access> findAllByResourceIdAndRequesterIdIn(String resourceId, List<String> requesterList);
    Access findByResourceIdAndRequesterId(String resourceId, String requesterId);
    void deleteByResourceIdAndRequesterId(String resourceId, String requesterId);
}
