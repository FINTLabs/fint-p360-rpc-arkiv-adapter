package no.fint.p360.data.p360;

import no.fint.p360.AdapterProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
public abstract class P360Service {

    @Autowired
    private WebClient p360Client;

    @Autowired
    private AdapterProps adapterProps;

    protected <T> T call(String uri, Object args, Class<T> responseType) {

        return p360Client.post().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "authkey " + adapterProps.getP360Password())
                .bodyValue(Collections.singletonMap("parameter", args))
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public boolean getHealth(String url) {
        return p360Client.post().uri(url)
                .header(HttpHeaders.AUTHORIZATION, "authkey " + adapterProps.getP360Password())
                .retrieve()
                .toBodilessEntity()
                .blockOptional()
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .getStatusCode().is2xxSuccessful();
    }
}
