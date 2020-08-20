package no.fint.p360;

import lombok.Data;
import no.fint.p360.data.CaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fint.p360.defaults")
public class CaseDefaults {
    private CaseProperties tilskuddfartoy;
}
