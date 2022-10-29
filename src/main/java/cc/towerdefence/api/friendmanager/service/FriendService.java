package cc.towerdefence.api.friendmanager.service;

import cc.towerdefence.api.friendmanager.model.FriendConnection;
import cc.towerdefence.api.friendmanager.model.PendingFriendConnection;
import cc.towerdefence.api.friendmanager.repository.FriendConnectionRepository;
import cc.towerdefence.api.friendmanager.repository.PendingFriendConnectionRepository;
import cc.towerdefence.api.service.FriendProto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import cc.towerdefence.api.friendmanager.utils.Pair;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendConnectionRepository friendConnectionRepository;
    private final PendingFriendConnectionRepository pendingFriendConnectionRepository;
    private final FriendNotificationService notificationService;

    public Pair<FriendProto.AddFriendResponse.AddFriendResult, FriendConnection> addFriendRequest(FriendProto.AddFriendRequest request) {
        UUID issuerId = UUID.fromString(request.getIssuerId());
        String issuerUsername = request.getIssuerUsername();
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
            FriendConnection connection = this.createFriendConnection(issuerId, issuerUsername, targetId);
            return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.FRIEND_ADDED, connection);
        }

        // TODO: check privacy settings
        boolean privacyBlocked = false;
        if (privacyBlocked) return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.PRIVACY_BLOCKED, null);

        // Add pending friend request/connection
        this.createPendingFriendConnection(issuerId, issuerUsername, targetId);
        return Pair.of(FriendProto.AddFriendResponse.AddFriendResult.REQUEST_SENT, null);
    }

    private FriendConnection createFriendConnection(UUID issuerId, String issuerUsername, UUID targetId) {
        this.notificationService.notifyFriendAdded(issuerId, issuerUsername, targetId);
        this.pendingFriendConnectionRepository.deleteByMutualIds(issuerId, targetId); // use mutual so it doesn't matter what around they are.
        return this.friendConnectionRepository.insert(new FriendConnection(ObjectId.get(), issuerId, targetId));
    }

    private void createPendingFriendConnection(UUID issuerId, String issuerUsername, UUID targetId) {
        this.notificationService.notifyFriendRequest(issuerId, issuerUsername, targetId);
        this.pendingFriendConnectionRepository.insert(new PendingFriendConnection(ObjectId.get(), issuerId, targetId));
    }

    public FriendProto.RemoveFriendResponse.RemoveFriendResult removeFriend(FriendProto.RemoveFriendRequest request) {
        UUID issuerId = UUID.fromString(request.getIssuerId());
        UUID targetId = UUID.fromString(request.getTargetId());

        long deleteResult = this.friendConnectionRepository.deleteByMutualIds(issuerId, targetId);
        if (deleteResult == 0) return FriendProto.RemoveFriendResponse.RemoveFriendResult.NOT_FRIENDS;

        this.notificationService.notifyFriendRemoved(issuerId, targetId);
        return FriendProto.RemoveFriendResponse.RemoveFriendResult.REMOVED;
    }

    public FriendProto.DenyFriendRequestResponse.DenyFriendRequestResult denyFriendRequest(FriendProto.DenyFriendRequestRequest request) {
        UUID issuerId = UUID.fromString(request.getIssuerId());
        UUID targetId = UUID.fromString(request.getTargetId());

        // bidirectional so we can allow the command to revoke both an outgoing and incoming request.
        int recordsDeleted = this.pendingFriendConnectionRepository.deleteByMutualIds(issuerId, targetId);

        return recordsDeleted == 1 ? FriendProto.DenyFriendRequestResponse.DenyFriendRequestResult.DENIED
                : FriendProto.DenyFriendRequestResponse.DenyFriendRequestResult.NO_REQUEST;
    }

    public List<FriendConnection> getFriends(UUID playerId) {
        return this.friendConnectionRepository.findByPlayerId(playerId, Sort.by(Sort.Direction.DESC, "_id"));
    }

    public List<PendingFriendConnection> getPendingFriendRequests(UUID playerId) {
        return this.pendingFriendConnectionRepository.findByPlayerId(playerId, Sort.by(Sort.Direction.DESC, "_id"));
    }
}
