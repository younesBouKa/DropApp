package server.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import server.data.NodeNew;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface INodeRepo extends MongoRepository<NodeNew, String> {

    /*
    String id;
    String name;
    NodeType type;
    String fileId;
    long fileSize;
    String contentType;
    String parentId;
    String spaceId;
    Map<String, Object> fields;
    Instant creationDate = Instant.now();
    Instant modificationDate = Instant.now();
    NodeNew parent; //
    Space space; //
     */

    List<NodeNew> findAllByParentId(String id, PageRequest pageRequest);
    List<NodeNew> findAllByParentIdAndType(String id, String type, PageRequest pageRequest);
    List<NodeNew> findAllByParentIdAndNameContains(String id, String name, PageRequest pageRequest);

    @Query("{ 'name': ?1 , 'space.id' : ?2 }")
    List<NodeNew> findByNameAndSpaceId(String name, String spaceId);

    @Override
    Optional<NodeNew> findById(String id);
    Optional<NodeNew> findByFileId(String id);
    List<NodeNew> findByName(String name);
    List<NodeNew> findByType(String type);
    List<NodeNew> findByContentType(String contentType);
    List<NodeNew> findBySpaceId(String id);
    List<NodeNew> findByParentId(String id);
    List<NodeNew> findBySpaceIdAndParentId(String spaceId, String parentId);

    List<NodeNew> findByCreationDateAfter(Instant data);
    List<NodeNew> findByCreationDateBefore(Instant data);
    List<NodeNew> findByCreationDateBetween(Instant data1, Instant date2);
    List<NodeNew> findByModificationDateAfter(Instant data);
    List<NodeNew> findByModificationDateBefore(Instant data);
    List<NodeNew> findByModificationDateBetween(Instant data1, Instant date2);

    int countById(String id);
    void deleteById(String Id);
}
