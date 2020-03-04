package no.fint.p360;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Getter
@Component
public class AdapterProps {

    @Value("${fint.p360.user}")
    private String p360User;

    @Value("${fint.p360.password}")
    private String p360Password;

    @Value("${fint.p360.endpoint-base-url}")
    private String endpointBaseUrl;

    @Value("${fint.file-cache.directory:file-cache}")
    private Path cacheDirectory;

    @Value("${fint.file-cache.spec:expireAfterAccess=5m,expireAfterWrite=7m}")
    private String cacheSpec;

}