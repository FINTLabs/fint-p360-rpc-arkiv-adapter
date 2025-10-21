package no.novari.p360.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "fint.p360.documentargs")
public class DocumentArgsConfiguration {
    private List<SakmappetypeMapping> sakmappetypeMapping;

    public List<SakmappetypeMapping> getSakmappetypeMapping() {
        return sakmappetypeMapping;
    }

    public void setSakmappetypeMapping(List<SakmappetypeMapping> sakmappetypeMappings) {
        this.sakmappetypeMapping = sakmappetypeMappings;
    }

    public static class SakmappetypeMapping {
        private String sakmappetype;
        private String documentarchive;

        public String getSakmappetype() {
            return sakmappetype;
        }

        public void setSakmappetype(String sakmappetype) {
            this.sakmappetype = sakmappetype;
        }

        public String getDocumentarchive() {
            return documentarchive;
        }

        public void setDocumentarchive(String documentarchive) {
            this.documentarchive = documentarchive;
        }
    }
}
