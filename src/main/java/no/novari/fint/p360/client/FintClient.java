package no.novari.fint.p360.client;

import no.fint.oauth.TokenService;
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class FintClient {
    private final WebClient webClient;
    private final TokenService tokenService;

    @Value("${fint.client.base-url:}")
    private String baseUrl;

    public FintClient(TokenService tokenService) {
        this.tokenService = tokenService;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public Mono<List<PersonalressursResource>> getPersonalressursResources() {
        return Mono.fromCallable(tokenService::getBearerToken)
                .flatMap(bearerToken -> webClient.get()
                        .uri(baseUrl + "/administrasjon/personal/personalressurs")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .retrieve()
                        .bodyToMono(PersonalressursResources.class))
                .map(PersonalressursResources::getContent);
    }
}
