package server.repositories;

import com.mongodb.lang.NonNullApi;
import com.sun.istack.internal.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import server.data.Space;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ISpaceRepo extends MongoRepository<Space, String>{


    List<Space> findAllByOwnerId(String ownerId, PageRequest pageRequest);
    List<Space> findAllByOwnerIdAndNameContains(String ownerId, String name, PageRequest pageRequest);
    //@Query("{ 'ownedId': ?1 , 'name' : { $regex: ?2 } }")
    //List<Space> findAllByOwnerIdAndNameRegex(String ownerId, String regex, PageRequest pageRequest);
    //List<Space> findAllByOwnerIdAndNameContaining(String ownerId, String name, PageRequest pageRequest);
    //List<Space> findAllByOwnerIdAndNameMatchesRegex(String ownerId, String regex, PageRequest pageRequest);

    boolean existsByNameAndOwnerId(String name, String id);

    @Override
    Optional<Space> findById( String id);
    Optional<Space> findByName(String name);
    Optional<Space> findByOwnerId(String id);
    List<Space> findByRootPath(String rootPath);

    List<Space> findByCreationDateAfter(Instant data);
    List<Space> findByCreationDateBefore(Instant data);
    List<Space> findByCreationDateBetween(Instant data1, Instant date2);
    List<Space> findByLastModificationDateAfter(Instant data);
    List<Space> findByLastModificationDateBefore(Instant data);
    List<Space> findByLastModificationDateBetween(Instant data1, Instant date2);

    int countById(String id);
    void deleteById(String id);

    /*@Query(value="{ 'firstName' : ?0 }", fields="{ 'firstName' : 1, 'lastname' : 1}", sort="{'field1':'acc'}")
    List<Space> find(String firstName);*/
}
