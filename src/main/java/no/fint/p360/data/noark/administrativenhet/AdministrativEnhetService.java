package no.fint.p360.data.noark.administrativenhet;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.AdministrativEnhetResource;
import no.fint.p360.data.p360.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class AdministrativEnhetService {
    @Autowired
    private ContactService contactService;

    @Autowired
    private AdministrativEnhetFactory administrativEnhetFactory;

    public Stream<AdministrativEnhetResource> getAdministrativEnhet() {
        return contactService
                .getEnterprisesByCategory("recno:1")
                .stream()
                .map(administrativEnhetFactory::toFintResource);
    }
}
