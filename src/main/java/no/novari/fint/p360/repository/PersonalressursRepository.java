package no.novari.fint.p360.repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.novari.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.novari.fint.p360.client.FintClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PersonalressursRepository {

    @Autowired
    private FintClient fintClient;

    @Getter
    private List<PersonalressursResource> personalressurser = Collections.emptyList();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.personalressurs.refresh-interval:3600000}")
    public void refresh() {
        try {
            List<PersonalressursResource> result = fintClient.getPersonalressursResources().block();
            personalressurser = result != null ? result : Collections.emptyList();
            log.info("Refreshed {} personalressurser", personalressurser.size());
        } catch (Exception e) {
            log.error("Failed to refresh personalressurser", e);
        }
    }

    public Optional<PersonalressursResource> getByEmail(String email) {
        return personalressurser.stream()
                .filter(p -> p.getKontaktinformasjon() != null)
                .filter(p -> StringUtils.isNotBlank(email) &&
                        email.equalsIgnoreCase(p.getKontaktinformasjon().getEpostadresse()))
                .findFirst();
    }
}
