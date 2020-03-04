package no.fint.p360.data.noark.part;

import no.fint.model.resource.administrasjon.arkiv.PartResource;
import no.fint.p360.data.exception.PartNotFound;
import no.fint.p360.data.p360.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class PartService {

    @Autowired
    private PartFactory partFactory;

    @Autowired
    private ContactService contactService;

    public PartResource getPartByPartId(int id) throws PartNotFound {

        Supplier<PartResource> enterpriseContact = () ->
                partFactory.toFintResource(contactService.getEnterpriseByRecno(id));
        Supplier<PartResource> privateContact = () ->
                partFactory.toFintResource(contactService.getPrivatePersonByRecno(id));
        Supplier<PartResource> contact = () ->
                partFactory.toFintResource(contactService.getContactPersonByRecno(id));

        return Stream.of(enterpriseContact, privateContact, contact)
                .parallel()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new PartNotFound("Recno " + id + " not found"));
    }
}
