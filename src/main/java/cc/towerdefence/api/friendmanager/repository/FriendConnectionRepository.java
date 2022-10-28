package cc.towerdefence.api.friendmanager.repository;

import cc.towerdefence.api.friendmanager.model.FriendConnection;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendConnectionRepository extends MongoRepository<FriendConnection, ObjectId> {

    @Query("{$or: [ {'playerOneId': ?0}, {'playerTwoId': ?0} ]}")
    List<FriendConnection> findByPlayerId(UUID playerId, Sort sort);

    @Query(value = "{$and: [{$or: [ {'playerOneId': ?0}, {'playerOneId': ?1} ]}, {$or: [ {'playerOneId': ?0}, {'playerTwoId': ?1} ]}]}", exists = true)
    boolean areFriends(UUID playerOneId, UUID playerTwoId);

    // remove FriendConnection where a friend connection is mutual
    @Query(value = "{$and: [{$or: [ {'playerOneId': ?0}, {'playerOneId': ?1} ]}, {$or: [ {'playerOneId': ?0}, {'playerTwoId': ?1} ]}]}", delete = true)
    long deleteByPlayerAndTargetId(UUID playerOneId, UUID playerTwoId);

}
