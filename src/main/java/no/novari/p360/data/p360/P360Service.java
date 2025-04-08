package no.novari.p360.data.p360;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import no.novari.p360.AdapterProps;
import no.novari.p360.model.FilterSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
@Slf4j
public abstract class P360Service {

    @Autowired
    private WebClient p360Client;

    @Autowired
    private AdapterProps adapterProps;

    private static final ObjectWriter WRITER = newWriter();

    private static ObjectWriter newWriter() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writer();
    }

    protected <T> T call(FilterSet filterSet, String uri, Object args, Class<T> responseType) {
        if (log.isTraceEnabled()) {
            try {
                log.trace("POST {} {}", uri, WRITER.writeValueAsString(args));
                log.trace("..using authkey starting with: {}", filterSet.authkey().substring(0,3));
            } catch (JsonProcessingException ignore) {
            }
        }
        return p360Client.post().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "authkey " + filterSet.authkey())
                .header("clientid", filterSet.clientId())
                .bodyValue(Collections.singletonMap("parameter", args))
                .retrieve()
                .bodyToMono(responseType)
                .map(it -> {
                    if (log.isTraceEnabled()) {
                        try {
                            log.trace("POST {} response: {}", uri, WRITER.writeValueAsString(it));
                        } catch (JsonProcessingException ignore) {
                        }
                    }
                    return it;
                })
                .block();
    }

    public boolean getHealth(FilterSet filterSet, String url) {
        return p360Client.post().uri(url)
                .header(HttpHeaders.AUTHORIZATION, "authkey " + filterSet.authkey())
                .header("clientid", filterSet.clientId())
                .retrieve()
                .toBodilessEntity()
                .blockOptional()
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .getStatusCode()
                .is2xxSuccessful();
    }
}
