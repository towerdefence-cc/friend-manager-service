package cc.towerdefence.api.friendmanager.controller;

import cc.towerdefence.api.friendmanager.model.FriendConnection;
import cc.towerdefence.api.friendmanager.model.PendingFriendConnection;
import cc.towerdefence.api.friendmanager.service.FriendService;
import cc.towerdefence.api.friendmanager.utils.Pair;
import cc.towerdefence.api.service.FriendGrpc;
import cc.towerdefence.api.service.FriendProto;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@GrpcService
@Controller
@RequiredArgsConstructor
public class FriendController extends FriendGrpc.FriendImplBase {
    private final FriendService friendService;

    @Override
    public void addFriend(FriendProto.AddFriendRequest request, StreamObserver<FriendProto.AddFriendResponse> responseObserver) {
        Pair<FriendProto.AddFriendResponse.AddFriendResult, FriendConnection> result = this.friendService.addFriendRequest(request);

        FriendProto.AddFriendResponse.Builder response = FriendProto.AddFriendResponse.newBuilder()
                .setResult(result.getFirst());

        if (result.getSecond() != null)
            response.setFriendsSince(Timestamp.newBuilder().setSeconds(result.getSecond().getId().getTimestamp()).build());

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeFriend(FriendProto.RemoveFriendRequest request, StreamObserver<FriendProto.RemoveFriendResponse> responseObserver) {
        responseObserver.onNext(FriendProto.RemoveFriendResponse.newBuilder()
                .setResult(this.friendService.removeFriend(request))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void denyFriendRequest(FriendProto.DenyFriendRequestRequest request, StreamObserver<FriendProto.DenyFriendRequestResponse> responseObserver) {
        responseObserver.onNext(FriendProto.DenyFriendRequestResponse.newBuilder()
                .setResult(this.friendService.denyFriendRequest(request))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFriendList(FriendProto.PlayerRequest request, StreamObserver<FriendProto.FriendListResponse> responseObserver) {
        List<FriendConnection> result = this.friendService.getFriends(UUID.fromString(request.getIssuerId()));

        List<FriendProto.FriendListResponse.FriendListPlayer> friends = result
                .stream()
                .map(friendConnection -> FriendProto.FriendListResponse.FriendListPlayer.newBuilder()
                        .setId(friendConnection.getPlayerOneId().toString())
                        .setFriendsSince(Timestamp.newBuilder().setSeconds(friendConnection.getId().getTimestamp()).build())
                        .build()
                ).toList();

        responseObserver.onNext(FriendProto.FriendListResponse.newBuilder()
                .addAllFriends(friends)
                .build());

        responseObserver.onCompleted();
    }

    @Override
    public void getPendingFriendRequestList(FriendProto.PlayerRequest request, StreamObserver<FriendProto.PendingFriendListResponse> responseObserver) {
        List<PendingFriendConnection> result = this.friendService.getPendingFriendRequests(UUID.fromString(request.getIssuerId()));

        List<FriendProto.PendingFriendListResponse.RequestedFriendPlayer> friends = result
                .stream()
                .map(friendConnection -> FriendProto.PendingFriendListResponse.RequestedFriendPlayer.newBuilder()
                        .setRequesterId(friendConnection.getRequesterId().toString())
                        .setTargetId(friendConnection.getTargetId().toString())
                        .setRequestTime(Timestamp.newBuilder().setSeconds(friendConnection.getId().getTimestamp()).build())
                        .build()
                ).toList();

        responseObserver.onNext(FriendProto.PendingFriendListResponse.newBuilder()
                .addAllRequests(friends)
                .build());

        responseObserver.onCompleted();
    }
}
