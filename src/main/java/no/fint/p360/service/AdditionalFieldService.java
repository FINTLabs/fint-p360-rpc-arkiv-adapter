package no.fint.p360.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.fint.p360.CustomFormats;
import no.fint.p360.data.utilities.BeanPropertyLookup;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class AdditionalFieldService {
    private final Map<String, Map<String,String>> fieldFormats;

    public AdditionalFieldService(CustomFormats customFormats) {
        this.fieldFormats = customFormats.getField();
    }

    @PostConstruct
    public void init() {
        log.debug("Custom Fields: {}", fieldFormats);
    }

    public <T> Stream<Field> getFieldsForResource(T resource) {
        String type = TitleService.resourceName(resource);
        Map<String,String> fields = fieldFormats.get(type);
        if (fields == null) {
            log.warn("No custom fields for {}", type);
            return Stream.empty();
        }
        final StringSubstitutor substitutor = new StringSubstitutor(new BeanPropertyLookup<>(resource));
        return fields.entrySet().stream()
                .map(e -> new Field(e.getKey(),
                        substitutor.replace(e.getValue())));
    }

    @Data
    public static class Field {
        private final String name,value;
    }
}
