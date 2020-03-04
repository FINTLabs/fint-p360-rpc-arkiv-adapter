package no.fint.p360.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class P360Configuration {

    @Value("${fint.p360.endpoint-base-url}")
    private String endpointBaseUrl;

    @Bean
    public WebClient p360Client() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .baseUrl(endpointBaseUrl)
                .build();
    }
}
