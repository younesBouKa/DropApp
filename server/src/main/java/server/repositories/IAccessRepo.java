package server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.data.Access;

import java.util.List;

public interface IAccessRepo  extends MongoRepository<Access, String> {

    List<Access> findAllByRequesterId(String requesterId);
    List<Access> findAllByResourceId(String resourceId);
    Access findByResourceIdAndRequesterId(String resourceId, String requesterId);
}
