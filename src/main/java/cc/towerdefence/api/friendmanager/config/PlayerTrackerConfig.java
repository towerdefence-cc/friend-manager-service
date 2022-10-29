package cc.towerdefence.api.friendmanager.config;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayerTrackerConfig {

    private ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("player-tracker.towerdefence.svc", 9090)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();
    }

    @Bean
    public PlayerTrackerGrpc.PlayerTrackerBlockingStub playerTrackerBlockingStub() {
        return PlayerTrackerGrpc.newBlockingStub(this.managedChannel());
    }
}
