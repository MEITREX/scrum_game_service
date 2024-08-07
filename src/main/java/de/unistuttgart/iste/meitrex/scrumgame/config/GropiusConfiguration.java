package de.unistuttgart.iste.meitrex.scrumgame.config;

import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.scrumgame.service.auth.AuthConnector;
import de.unistuttgart.iste.meitrex.scrumgame.service.auth.AuthTokenFromHeaderSupplier;
import de.unistuttgart.iste.meitrex.scrumgame.service.auth.GropiusAuthConnector;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.client.WebGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.function.*;

@Configuration
public class GropiusConfiguration {

    // Maximum size of the in-memory buffer for the WebClient.
    // This is necessary to prevent the application from running out of memory when receiving
    // large responses from the Gropius server.
    // Observed responses did not exceed 1 MB, so 16 MB should be a safe value.
    private static final int MAX_IN_MEMORY_SIZE = 16 * 1024 * 1024;

    @Bean
    public Supplier<String> tokenSupplier() {
        return new AuthTokenFromHeaderSupplier();
    }

    @Value("${gropius.url}")
    private String gropiusUrl;

    @Bean
    public HttpGraphQlClient baseGraphQlClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(gropiusUrl + "/graphql")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();

        return HttpGraphQlClient.builder(webClient).build();
    }

    @Bean
    public Supplier<WebGraphQlClient> gropiusGraphQlClientWithAuthTokenSupplier() {
        return () -> baseGraphQlClient()
                .mutate()
                .headers(headers -> headers.setBearerAuth(tokenSupplier().get()))
                .build();
    }

    @Bean
    public GraphQlRequestExecutor graphQlRequestExecutor(
            Supplier<WebGraphQlClient> gropiusGraphQlClientWithAuthTokenSupplier) {
        return new GraphQlRequestExecutor(gropiusGraphQlClientWithAuthTokenSupplier);
    }

    @Value("${gropius.auth.publicKey:#{null}}")
    @Nullable
    private String publicKeyBase64Encoded;

    @Bean
    public AuthConnector authConnector(GraphQlRequestExecutor graphQlRequestExecutor) {
        Objects.requireNonNull(publicKeyBase64Encoded);
        return new GropiusAuthConnector(graphQlRequestExecutor, publicKeyBase64Encoded);
    }
}
