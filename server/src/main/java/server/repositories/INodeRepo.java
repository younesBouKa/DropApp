package server.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import server.data.Node;

import java.util.List;
import java.util.Optional;

public interface INodeRepo extends MongoRepository<Node, String> {

    Optional<Node> findByIdAndOwnerId(String id, String ownerId);
    List<Node> findAllByOwnerIdAndParentIdAndNameContains(String ownerId, String parentId, String name, PageRequest pageRequest);
    List<Node> findByOwnerIdAndParentId(String ownerId, String parentId);
    @Query("{ 'name': ?0 , 'ownerId' : ?1, 'parentId': ?2 }")
    Node findByNameAndOwnerIdAndParentIdNotNull(String name, String ownerId, String parentId);
    @Query("{'ownerId' : ?0, 'parentId': {$exists: false} }")
    List<Node> findByOwnerIdAndParentIdNull(String ownerId);
    @Query("{ 'name': ?0 , 'ownerId' : ?1, 'parentId': {$exists: false} }")
    Node findByNameAndOwnerIdAndParentIdNull(String name, String ownerId);

    int countById(String id);
    void deleteById(String id);
}
