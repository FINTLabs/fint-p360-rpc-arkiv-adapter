package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.AdapterProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.logging.Level;

@Service
@Slf4j
public abstract class P360Service {

    @Autowired
    private WebClient p360Client;

    @Autowired
    private AdapterProps adapterProps;

    protected <T> T call(String uri, Object args, Class<T> responseType) {
        log.trace("POST {} {}", uri, args);
        return p360Client.post().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "authkey " + adapterProps.getP360Password())
                .header("clientid", adapterProps.getP360User())
                .bodyValue(Collections.singletonMap("parameter", args))
                .retrieve()
                .bodyToMono(responseType)
                .log(getClass().getName(), Level.FINEST)
                .block();
    }

    public boolean getHealth(String url) {
        return p360Client.post().uri(url)
                .header(HttpHeaders.AUTHORIZATION, "authkey " + adapterProps.getP360Password())
                .header("clientid", adapterProps.getP360User())
                .retrieve()
                .toBodilessEntity()
                .blockOptional()
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .getStatusCode()
                .is2xxSuccessful();
    }
}
