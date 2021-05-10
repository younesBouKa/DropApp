package server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.data.GroupMembership;

import java.util.List;
import java.util.Optional;

public interface IUserGroupRepo extends MongoRepository<GroupMembership, String> {

    Optional<GroupMembership> findById(String groupId);
    GroupMembership findByGroupIdAndMemberId(String groupId, String memberId);
    List<GroupMembership> findByMemberId(String memberId);
    List<GroupMembership> findByMemberIdAndEnable(String memberId, boolean enable);
    void deleteById(String groupId);
    void deleteAllByGroupId(String groupId);
    void deleteByMemberIdAndGroupId(String memberId, String groupId);
}
