package no.fint.p360;

import lombok.Data;
import no.fint.p360.model.FilterSet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties("fint.p360.filter-set")
public class FilterSets {
    private Map<String, FilterSet> integration;
    private Map<String, String> casetype;
}
