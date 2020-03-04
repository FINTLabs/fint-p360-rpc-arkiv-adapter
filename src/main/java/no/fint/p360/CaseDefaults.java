package no.fint.p360;

import lombok.Data;
import no.fint.p360.data.CaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fint.p360.defaults")
public class CaseDefaults {
    private Map<String, CaseProperties> casetype;
}
