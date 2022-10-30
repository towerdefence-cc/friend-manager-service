package cc.towerdefence.api.friendmanager.repository;

import cc.towerdefence.api.friendmanager.model.PendingFriendConnection;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingFriendConnectionRepository extends MongoRepository<PendingFriendConnection, ObjectId> {

    Optional<PendingFriendConnection> findByRequesterIdAndTargetId(UUID requesterId, UUID targetId);

    @Query("{$or: [ {'requesterId': ?0}, {'targetId': ?0} ]}")
    List<PendingFriendConnection> findByPlayerId(UUID playerId, Sort sort);

    List<PendingFriendConnection> findAllByRequesterId(UUID requesterId, Sort sort);

    List<PendingFriendConnection> findAllByTargetId(UUID targetId, Sort sort);


    int deleteAllByTargetId(UUID targetId);

    int deleteAllByRequesterId(UUID requesterId);

    int deleteByRequesterIdAndTargetId(UUID requesterId, UUID targetId);

    @Query(value = "{$and: [{$or: [ {'requesterId': ?0}, {'requesterId': ?1} ]}, {$or: [ {'targetId': ?0}, {'targetId': ?1} ]}]}", delete = true)
    int deleteByMutualIds(UUID requesterId, UUID targetId);
}
