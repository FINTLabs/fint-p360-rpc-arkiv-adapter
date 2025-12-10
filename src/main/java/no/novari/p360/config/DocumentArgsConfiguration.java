package no.novari.p360.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "fint.p360.documentargs")
public class DocumentArgsConfiguration {
    private List<SakmappetypeMapping> sakmappetypeMapping;

    @Setter
    @Getter
    public static class SakmappetypeMapping {
        private String sakmappetype;
        private String documentarchive;
    }
}
