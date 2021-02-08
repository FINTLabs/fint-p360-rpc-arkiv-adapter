package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.LinkResolver;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.kodeverk.FylkeResource;
import no.fint.model.resource.felles.kodeverk.KommuneResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class LinkResolverService implements LinkResolver {
    private final RestTemplate restTemplate;

    public LinkResolverService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object resolve(Link link) {
        final String href = link.getHref();
        if (!StringUtils.startsWith(href, "https://")) {
            return null;
        }
        if (StringUtils.contains(href, "/felles/kodeverk/kommune/")) {
            try {
                final KommuneResource kommune = restTemplate.getForObject(href, KommuneResource.class);
                log.info("Found {}", kommune);
                return kommune;
            } catch (HttpStatusCodeException e) {
                throw new IllegalArgumentException("HTTP status " + e.getStatusCode() + " when resolving link " + href);
            }
        }
        if (StringUtils.contains(href, "/felles/kodeverk/fylke/")) {
            try {
                final FylkeResource fylke = restTemplate.getForObject(href, FylkeResource.class);
                log.info("Found {}", fylke);
                return fylke;
            } catch (HttpStatusCodeException e) {
                throw new IllegalArgumentException("HTTP status " + e.getStatusCode() + " when resolving link " + href);
            }
        }
        log.info("No known resource for link {}", href);
        return null;
    }
}
