package server.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import server.data.Node;

import java.util.List;
import java.util.Optional;

public interface INodeRepo extends MongoRepository<Node, String> {

    Optional<Node> findById(String id);
    List<Node> findAllByParentIdAndNameContains(String parentId, String name, PageRequest pageRequest);
    List<Node> findByParentId(String parentId);
    @Query("{ 'name': ?0 , 'parentId': ?1 }")
    Node findByNameAndParentIdNotNull(String name, String parentId);
    @Query("{'parentId': {$exists: false} }")
    List<Node> findByParentIdNull();
    @Query("{ 'name': ?0 , 'parentId': {$exists: false} }")
    Node findByNameAndParentIdNull(String name);

    int countById(String id);
    void deleteById(String id);
}
