package de.unistuttgart.iste.meitrex.scrumgame.config;

import de.unistuttgart.iste.meitrex.scrumgame.service.auth.AudienceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    @Profile("dev")
    @SuppressWarnings({"java:S5122"}) // only dev profile, no security issue
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the security for the development environment.
     */
    @Bean
    @Profile("dev")
    @SuppressWarnings({"java:S4502", "java:S3330"})
    // only dev profile, no security issue
    DefaultSecurityFilterChain springDevWebFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/graphql**")
                        .ignoringRequestMatchers("/webhook**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                // disable CORS
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        // allow access to the GraphQL endpoint (required for schema introspection)
                        .requestMatchers("/graphql**").permitAll()
                        // for now: allow access to the WebSocket endpoint
                        .requestMatchers("/graphql-ws**").permitAll()
                        // allow access to the GraphiQL interface
                        .requestMatchers("/graphiql**").permitAll()
                        // allow access to webhooks
                        .requestMatchers(HttpMethod.POST, "/webhook**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }

    /**
     * Configures the security for the production environment.
     */
    @Bean
    @Profile("prod")
    @SuppressWarnings({"java:S3330"})
    // currently CSRF uses cookie with httpOnly=false, which could be improved
    DefaultSecurityFilterChain prodSpringWebFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        // allow OPTIONS requests to the GraphQL endpoint
                        .requestMatchers(HttpMethod.OPTIONS, "/graphql**").permitAll()
                        // for now: allow access to the WebSocket endpoint
                        .requestMatchers("/graphql-ws**").permitAll()
                        // allow access to the GraphiQL interface
                        .requestMatchers("/graphiql**").permitAll()
                        .anyRequest().authenticated()) // All requests require authentication
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }

    @Bean
    @Profile({"prod", "dev"})
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = initNimbusDecoder();
        OAuth2TokenValidator<Jwt> withAudience = createAudienceValidator();

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    @Value("${gropius.auth.secret}")
    private String secret;

    private OAuth2TokenValidator<Jwt> createAudienceValidator() {
        return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),
                new AudienceValidator("backend"));
    }

    private NimbusJwtDecoder initNimbusDecoder() {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

}
