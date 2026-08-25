package no.novari.fint.p360.client;

import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class FintClient {
    private final WebClient webClient;
    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;
    private final Authentication principal;

    @Value("${fint.client.base-url:}")
    private String baseUrl;

    public FintClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                      Authentication principal) {
        this.authorizedClientManager = authorizedClientManager;
        this.principal = principal;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public Mono<List<PersonalressursResource>> getPersonalressursResources() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("fint-client")
                .principal(principal)
                .build();

        return authorizedClientManager.authorize(authorizeRequest)
                .flatMap(client -> webClient.get()
                        .uri(baseUrl + "/administrasjon/personal/personalressurs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue())
                        .retrieve()
                        .bodyToMono(PersonalressursResources.class))
                .map(PersonalressursResources::getContent);
    }
}
