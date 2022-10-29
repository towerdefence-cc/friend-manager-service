package cc.towerdefence.api.friendmanager.service;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.velocity.VelocityFriendGrpc;
import cc.towerdefence.api.service.velocity.VelocityFriendProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendNotificationService.class);

    private final PlayerTrackerGrpc.PlayerTrackerBlockingStub playerTracker;
    private final CoreV1Api kubernetesClient;

    @Async
    public void notifyFriendRequest(UUID issuerId, String issuerUsername, UUID targetId) {
        String targetServerIp = this.getServerIpForPlayer(targetId);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(targetServerIp, 9090)
                .usePlaintext()
                .build();

        VelocityFriendGrpc.VelocityFriendBlockingStub stub = VelocityFriendGrpc.newBlockingStub(channel);

        stub.receiveFriendRequest(VelocityFriendProto.ReceiveFriendRequestRequest.newBuilder()
                .setSenderId(issuerId.toString())
                .setSenderUsername(issuerUsername)
                .setRecipientId(targetId.toString())
                .build());
    }

    public String getServerIpForPlayer(UUID playerId) {
        PlayerTrackerProto.GetPlayerServerResponse response = this.playerTracker.getPlayerServer(PlayerTrackerProto.GetPlayerServerRequest.newBuilder()
                .setPlayerId(playerId.toString())
                .build());

        String proxyId = response.getServer().getProxyId();

        try {
            V1Pod pod = this.kubernetesClient.readNamespacedPod(proxyId, "towerdefence", null);
            return pod.getStatus().getPodIP();
        } catch (ApiException e) {
            LOGGER.error("Failed to get pod for proxy id {}:\nK8s Error: ({}) {}\n{}", proxyId, e.getCode(), e.getResponseBody(), e);
            return null;
        }
    }
}
