package no.novari.fint.p360.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(FintClientOAuthProperties.class)
public class FintClientConfiguration {

    @Bean
    public ReactiveClientRegistrationRepository fintClientRegistrationRepository(FintClientOAuthProperties oauthProperties) {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("fint-client")
                .clientId(oauthProperties.getClientId())
                .clientSecret(oauthProperties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .tokenUri(oauthProperties.getAccessTokenUri())
                .clientName("fint-client");

        if (oauthProperties.getScope() != null && !oauthProperties.getScope().isBlank()) {
            builder.scope(oauthProperties.getScope());
        }

        return new InMemoryReactiveClientRegistrationRepository(builder.build());
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager fintClientAuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        InMemoryReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);

        ReactiveOAuth2AuthorizedClientProvider provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .password()
                .build();
        manager.setAuthorizedClientProvider(provider);
        manager.setContextAttributesMapper(request -> {
            Authentication principal = request.getPrincipal();
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, principal.getName());
            attributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, principal.getCredentials());
            return Mono.just(attributes);
        });

        return manager;
    }

    @Bean
    public Authentication fintClientPrincipal(FintClientOAuthProperties oauthProperties) {
        return new UsernamePasswordAuthenticationToken(
                oauthProperties.getUsername(),
                oauthProperties.getPassword(),
                AuthorityUtils.NO_AUTHORITIES);
    }
}
