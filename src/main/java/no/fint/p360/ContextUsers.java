package no.fint.p360;

import lombok.Data;
import no.fint.p360.model.ContextUser;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties("fint.p360.ad-context-user")
public class ContextUsers {

    private Map<String, ContextUser> account;
    private Map<String, String> casetype;
}
