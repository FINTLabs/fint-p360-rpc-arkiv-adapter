package no.fint.p360.data.noark.korrespondansepart;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.p360.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.validIdentifikator;


@Slf4j
@Service
public class KorrespondansepartService {

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private ContactService contactService;

    public KorrespondansepartResource getKorrespondansepartBySystemId(int id) throws KorrespondansepartNotFound {

        Supplier<KorrespondansepartResource> enterpriseContact = () ->
                korrespondansepartFactory.toFintResource(contactService.getEnterpriseByRecno(id));
        Supplier<KorrespondansepartResource> privateContact = () ->
                korrespondansepartFactory.toFintResource(contactService.getPrivatePersonByRecno(id));
        Supplier<KorrespondansepartResource> contact = () ->
                korrespondansepartFactory.toFintResource(contactService.getContactPersonByRecno(id));

        return Stream.of(enterpriseContact, privateContact, contact)
                .parallel()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new KorrespondansepartNotFound("Recno " + id + " not found"));

    }

    public KorrespondansepartResource getKorrespondansepartByFodselsnummer(String fodselsnummer) throws KorrespondansepartNotFound {
        try {
            return korrespondansepartFactory.toFintResource(
                    contactService.getPrivatePersonByPersonalIdNumber(fodselsnummer)
            );
        } catch (PrivatePersonNotFound e) {
            throw new KorrespondansepartNotFound(e.getMessage());
        }

    }

    public KorrespondansepartResource getKorrespondansepartByOrganisasjonsnummer(String organisasjonsNummer) throws KorrespondansepartNotFound {
        try {
            return korrespondansepartFactory.toFintResource(
                    contactService.getEnterpriseByEnterpriseNumber(organisasjonsNummer));
        } catch (EnterpriseNotFound e) {
            throw new KorrespondansepartNotFound(e.getMessage());
        }
    }

    public Stream<KorrespondansepartResource> search(Map<String, String> queryParams) {
        Supplier<Stream<KorrespondansepartResource>> enterpriseContacts = () ->
                contactService.searchEnterprise(queryParams).map(korrespondansepartFactory::toFintResource);
        Supplier<Stream<KorrespondansepartResource>> privateContacts = () ->
                contactService.searchPrivatePerson(queryParams).map(korrespondansepartFactory::toFintResource);
        Supplier<Stream<KorrespondansepartResource>> contacts = () ->
                contactService.searchContactPerson(queryParams).map(korrespondansepartFactory::toFintResource);

        return Stream.of(enterpriseContacts, privateContacts, contacts)
                .parallel()
                .flatMap(Supplier::get);
    }

    public KorrespondansepartResource createKorrespondansepart(KorrespondansepartResource korrespondansepartResource) throws CreateContactException, CreateEnterpriseException {
        if (validIdentifikator(korrespondansepartResource.getFodselsnummer())) {
            int recNo = contactService.createPrivatePerson(korrespondansepartFactory.toPrivatePerson(korrespondansepartResource));
            return korrespondansepartFactory.toFintResource(contactService.getPrivatePersonByRecno(recNo));
        } else if (validIdentifikator(korrespondansepartResource.getOrganisasjonsnummer())) {
            int recNo = contactService.createEnterprise(korrespondansepartFactory.toEnterprise(korrespondansepartResource));
            return korrespondansepartFactory.toFintResource(contactService.getEnterpriseByRecno(recNo));
        } else {
            throw new IllegalArgumentException("Invalid Korrespondansepart - neither fodselsnummer nor organisasjonsnummer is set.");
        }
    }

    public boolean health()  {
        return  contactService.ping();
    }
}
