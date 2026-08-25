package no.novari.fint.p360.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("fint.client.oauth")
public class FintClientOAuthProperties {
    private String accessTokenUri;
    private String scope;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
}
