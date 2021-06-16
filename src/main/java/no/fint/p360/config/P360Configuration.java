package no.fint.p360.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

@Slf4j
@Configuration
public class P360Configuration {

    private static final String PROXY_ENABLED_PROP = "fint.proxy.enabled";

    @Value("${fint.p360.endpoint-base-url}")
    private String endpointBaseUrl;

    @Value("${https.proxyHost:localhost}")
    private String proxyHost;

    @Value("${https.proxyPort:8888}")
    private int proxyPort;

    @Bean
    @ConditionalOnProperty(name = PROXY_ENABLED_PROP, havingValue = "false", matchIfMissing = true)
    public WebClient p360Client() {
        log.debug("Running with no proxy.");
        return getWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PROXY_ENABLED_PROP, havingValue = "true")
    public WebClient p360ClientWithProxy() {
        log.info("Running with proxy configuration: proxyHost: {}, proxyPort: {}", proxyHost, proxyPort);
        return getWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(getHttpClient().tcpConfiguration(tcpClient -> tcpClient
                        .proxy(proxy -> proxy
                                .type(ProxyProvider.Proxy.HTTP)
                                .host(proxyHost)
                                .port(proxyPort)))))
                .build();
    }

    private WebClient.Builder getWebClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(endpointBaseUrl);
    }

    private HttpClient getHttpClient() {
        return HttpClient
                .create()
                .wiretap(true);
    }
}
