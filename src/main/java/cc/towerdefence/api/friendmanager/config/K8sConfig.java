package cc.towerdefence.api.friendmanager.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
public class K8sConfig {

    @Bean
    @Profile("development")
    public ApiClient apiClient() throws IOException {
        ApiClient apiClient = Config.defaultClient();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }

    @Bean
    @Profile("production")
    public ApiClient productionApiClient() throws IOException {
        ApiClient apiClient = ClientBuilder.cluster().build();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }

    @Bean
    public CoreV1Api coreV1Api(ApiClient client) {
        return new CoreV1Api(client);
    }
}
