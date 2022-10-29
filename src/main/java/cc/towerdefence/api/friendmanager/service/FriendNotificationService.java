package cc.towerdefence.api.friendmanager.service;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.velocity.VelocityNotificationReceiverGrpc;
import cc.towerdefence.api.service.velocity.VelocityNotificationReceiverProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
    private final CoreV1Api kubernetesClient = new CoreV1Api();

    @Async
    public void notifyFriendAdd(UUID issuerId, UUID targetId) {
        String targetServerIp = this.getServerIpForPlayer(targetId);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(targetServerIp, 9090)
                .usePlaintext()
                .build();

        VelocityNotificationReceiverGrpc.VelocityNotificationReceiverBlockingStub stub = VelocityNotificationReceiverGrpc.newBlockingStub(channel);

        stub.receiveFriendRequest(VelocityNotificationReceiverProto.ReceiveFriendRequestRequest.newBuilder()
                .setSenderId(issuerId.toString())
                .setRecipientId(targetId.toString())
                .build());
    }

    @SneakyThrows
    public String getServerIpForPlayer(UUID playerId) {
        PlayerTrackerProto.GetPlayerServerResponse response = this.playerTracker.getPlayerServer(PlayerTrackerProto.GetPlayerServerRequest.newBuilder()
                .setPlayerId(playerId.toString())
                .build());

        System.out.println("Response: " + response);

        String serverId = response.getServer().getServerId();
        System.out.println("Server ID: " + serverId);

        V1Pod pod = this.kubernetesClient.readNamespacedPod(serverId, "default", null);
        System.out.println("Pod: " + pod);

        return pod.getSpec().getHostname();
    }
}
