package cc.towerdefence.api.friendmanager.service;

import cc.towerdefence.api.friendmanager.model.FriendConnection;
import cc.towerdefence.api.friendmanager.model.PendingFriendConnection;
import cc.towerdefence.api.friendmanager.repository.FriendConnectionRepository;
import cc.towerdefence.api.friendmanager.repository.PendingFriendConnectionRepository;
import cc.towerdefence.api.service.FriendProto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import cc.towerdefence.api.friendmanager.utils.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendConnectionRepository friendConnectionRepository;
    private final PendingFriendConnectionRepository pendingFriendConnectionRepository;

    public Pair<FriendProto.AddFriendResponse.AddFriendResult, FriendConnection> addFriendRequest(FriendProto.AddFriendRequest request) {
        UUID issuerId = UUID.fromString(request.getIssuerId());
        UUID targetId = UUID.fromString(request.getTargetId());

        // check if already sent a friend request to target
        boolean alreadyRequested = this.pendingFriendConnectionRepository.findByRequesterIdAndTargetId(issuerId, targetId).isPresent();
        if (alreadyRequested) return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.ALREADY_REQUESTED, null);

        // check if already friends
        boolean alreadyFriends = this.friendConnectionRepository.areFriends(issuerId, targetId);
        if (alreadyFriends) return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.ALREADY_FRIENDS, null);

        // check if already received a friend request from target
        boolean receivedRequest = this.pendingFriendConnectionRepository.findByRequesterIdAndTargetId(targetId, issuerId).isPresent();
        if (receivedRequest) {
            FriendConnection connection = this.createFriendConnection(issuerId, targetId);
            return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.FRIEND_ADDED, connection);
        }

        // TODO: check privacy settings
        boolean privacyBlocked = false;
        if (privacyBlocked) return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.PRIVACY_BLOCKED, null);

        // Add pending friend request/connection
        this.createPendingFriendConnection(issuerId, targetId);
        return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.REQUEST_SENT, null);
    }

    private FriendConnection createFriendConnection(UUID playerOneId, UUID playerTwoId) {
        this.pendingFriendConnectionRepository.deleteByMutualRequesterIdAndTargetId(playerOneId, playerTwoId); // use mutual so it doesn't matter what around they are.
        return this.friendConnectionRepository.insert(new FriendConnection(ObjectId.get(), playerOneId, playerTwoId));
    }

    private void createPendingFriendConnection(UUID requesterId, UUID targetId) {
        this.pendingFriendConnectionRepository.insert(new PendingFriendConnection(ObjectId.get(), requesterId, targetId));
    }

    public FriendProto.RemoveFriendResponse.RemoveFriendResult removeFriendRequest(FriendProto.RemoveFriendRequest request) {
        UUID issuerId = UUID.fromString(request.getIssuerId());
        UUID targetId = UUID.fromString(request.getTargetId());

        long deleteResult = this.friendConnectionRepository.deleteByPlayerAndTargetId(issuerId, targetId);

        return deleteResult == 1 ? FriendProto.RemoveFriendResponse.RemoveFriendResult.REMOVED
                : FriendProto.RemoveFriendResponse.RemoveFriendResult.NOT_FRIENDS;
    }

    public List<FriendConnection> getFriends(UUID playerId) {
        return this.friendConnectionRepository.findByPlayerId(playerId, Sort.by(Sort.Direction.DESC, "_id"));
    }

    public List<PendingFriendConnection> getPendingFriendRequests(UUID playerId) {
        return this.pendingFriendConnectionRepository.findByPlayerId(playerId, Sort.by(Sort.Direction.DESC, "_id"));
    }
}
