package cc.towerdefence.api.friendmanager.service;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.velocity.VelocityNotificationReceiverGrpc;
import cc.towerdefence.api.service.velocity.VelocityNotificationReceiverProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendNotificationService {
    private final PlayerTrackerGrpc.PlayerTrackerBlockingStub playerTracker;
    private final CoreV1Api kubernetesClient;

    @Async
    public void notifyFriendAdd(UUID issuerId, UUID targetId) {
        String targetServerIp = this.getServerIpForPlayer(targetId);
        System.out.println("Target server ip: " + targetServerIp);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(targetServerIp, 9090)
                .usePlaintext()
                .build();

        VelocityNotificationReceiverGrpc.VelocityNotificationReceiverBlockingStub stub = VelocityNotificationReceiverGrpc.newBlockingStub(channel);

        stub.receiveFriendRequest(VelocityNotificationReceiverProto.ReceiveFriendRequestRequest.newBuilder()
                .setSenderId(issuerId.toString())
                .setRecipientId(targetId.toString())
                .build());
    }

    public String getServerIpForPlayer(UUID playerId) {
        System.out.println("Getting server for player " + playerId);
        PlayerTrackerProto.GetPlayerServerResponse response = this.playerTracker.getPlayerServer(PlayerTrackerProto.GetPlayerServerRequest.newBuilder()
                .setPlayerId(playerId.toString())
                .build());

        System.out.println("Response: " + response);

        String proxyId = response.getServer().getProxyId();
        System.out.println("Proxy ID: " + proxyId);

        try {
            V1Pod pod = this.kubernetesClient.readNamespacedPod(proxyId, "towerdefence", null);
            System.out.println("Pod: " + pod);
            return pod.getStatus().getPodIP();
        } catch (ApiException e) {
            System.out.println("Exception: " + e.getCode() + " " + e.getMessage() + " " + e.getResponseBody() + " " + e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }
    }
}
