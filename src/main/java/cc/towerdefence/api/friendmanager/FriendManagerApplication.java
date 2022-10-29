package cc.towerdefence.api.friendmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FriendManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendManagerApplication.class, args);
    }

}
