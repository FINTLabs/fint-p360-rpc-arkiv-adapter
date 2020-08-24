package no.fint.p360.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.fint.p360.CustomFormats;
import no.fint.p360.data.utilities.BeanPropertyLookup;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@Slf4j
public class AdditionalFieldService {
    private final Map<String, Map<String, String>> fieldFormats;

    public AdditionalFieldService(CustomFormats customFormats) {
        this.fieldFormats = customFormats.getField();
    }

    @PostConstruct
    public void init() {
        log.debug("Custom Fields: {}", fieldFormats);
    }

    public <T> Stream<Field> getFieldsForResource(T resource) {
        if (fieldFormats == null) {
            return Stream.empty();
        }
        String type = TitleService.resourceName(resource);
        Map<String, String> fields = fieldFormats.get(type);
        if (fields == null) {
            log.warn("No custom fields for {}", type);
            return Stream.empty();
        }
        final StringSubstitutor substitutor = new StringSubstitutor(new BeanPropertyLookup<>(resource));
        return fields.entrySet().stream()
                .map(e -> new Field(e.getKey(),
                        substitutor.replace(e.getValue())));
    }

    public <U> void parseFields(U resource, List<Field> fields) {
        if (fieldFormats == null || fields == null || fields.isEmpty()) {
            return;
        }
        Map<String, String> fieldMap = fieldFormats.get(TitleService.resourceName(resource));
        if (fieldMap == null) {
            return;
        }

        Pattern names = Pattern.compile("\\$\\{([^}]+)}");

        fields
                .stream()
                .filter(f -> fieldMap.containsKey(f.getName()))
                .forEach(f -> {
                    final String format = fieldMap.get(f.getName());
                    log.debug("Parsing field {} -> {} -> {}", f.getName(), format, f.getValue());
                    List<String> nameList = getNameList(names, format);
                    Matcher fieldMatcher = getFieldMatcher(names, format, f.getValue());
                    if (fieldMatcher.matches()) {
                        for (int i = 1; i <= fieldMatcher.groupCount(); i++) {
                            try {
                                log.debug("Setting attribute {} to {}", nameList.get(i-1), fieldMatcher.group(i));
                                BeanUtils.setProperty(resource, nameList.get(i-1), fieldMatcher.group(i));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private Matcher getFieldMatcher(Pattern names, String fieldPattern, String fieldValue) {
        String pattern = RegExUtils.replaceAll(fieldPattern, names, "(.+)");
        Pattern titlePattern = Pattern.compile("^" + pattern + "$");
        return titlePattern.matcher(fieldValue);
    }

    private List<String> getNameList(Pattern names, String value) {
        List<String> nameList = new LinkedList<>();
        Matcher nameMatcher = names.matcher(value);
        while (nameMatcher.find()) {
            nameList.add(nameMatcher.group(1));
        }
        log.debug("nameList = {}", nameList);
        return nameList;
    }

    @Data
    public static class Field {
        private final String name, value;
    }
}
