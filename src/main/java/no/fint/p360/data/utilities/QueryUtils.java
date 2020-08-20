package no.fint.p360.data.utilities;

import org.jooq.lambda.Unchecked;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public enum QueryUtils {
    ;

    public static Map<String, String> getQueryParams(String query) {
        return UriComponentsBuilder.fromUriString(query)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Unchecked.function(e -> URLDecoder.decode(e.getValue(), "UTF-8"))));
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }}
