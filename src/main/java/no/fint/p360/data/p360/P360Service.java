package no.fint.p360.data.p360;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public abstract class P360Service {

    @Autowired
    private WebClient p360Client;

    @Value("${fint.p360.rpc.authkey}")
    private String p360AuthKey;

    protected <T> T call(String uri, Object args, Class<T> responseType) {

        return p360Client.post().uri(uri + "?authkey={p360AuthKey}", p360AuthKey)
                .bodyValue(args).retrieve().bodyToMono(responseType).block();
    }

    public boolean getHealth(String url) {
        return p360Client.post().uri(url + "?authkey={p360AuthKey}", p360AuthKey)
                .retrieve()
                .toBodilessEntity()
                .blockOptional()
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .getStatusCode().is2xxSuccessful();
    }
}
