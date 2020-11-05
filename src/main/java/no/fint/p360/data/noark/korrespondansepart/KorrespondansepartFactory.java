package no.fint.p360.data.noark.korrespondansepart;

import no.fint.model.arkiv.kodeverk.KorrespondansepartType;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.KorrespondansepartTypeResource;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fint.p360.repository.KodeverkRepository;
import no.p360.model.ContactService.PostAddress__4;
import no.p360.model.ContactService.PrivateAddress__3;
import no.p360.model.ContactService.SynchronizeEnterpriseArgs;
import no.p360.model.ContactService.SynchronizePrivatePersonArgs;
import no.p360.model.DocumentService.Contact;
import no.p360.model.DocumentService.Contact__1;
import no.p360.model.DocumentService.UnregisteredContact;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.*;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@Service
public class KorrespondansepartFactory {

    private final KodeverkRepository kodeverkRepository;

    public KorrespondansepartFactory(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    public UnregisteredContact createDocumentUnregisteredContact(KorrespondansepartResource korrespondansepart) {
        UnregisteredContact contact = new UnregisteredContact();

        if (StringUtils.isNotBlank(korrespondansepart.getOrganisasjonsnummer())) {
            contact.setReferenceNumber(korrespondansepart.getOrganisasjonsnummer());
        } else if (StringUtils.isNotBlank(korrespondansepart.getFodselsnummer())) {
            contact.setReferenceNumber(korrespondansepart.getFodselsnummer());
        }
        contact.setContactName(korrespondansepart.getKorrespondansepartNavn());

        setAddress(contact, korrespondansepart.getAdresse());
        setPhones(contact, korrespondansepart.getKontaktinformasjon());

        applyParameterFromLink(
                korrespondansepart.getKorrespondanseparttype(),
                contact::setRole);

        return contact;
    }

    public Contact createDocumentContact(Integer recno, KorrespondansepartResource resource) {
        Contact contact = new Contact();
        contact.setReferenceNumber("recno:" + recno);
        applyParameterFromLink(
                resource.getKorrespondanseparttype(),
                contact::setRole);
        return contact;
    }


    public SynchronizePrivatePersonArgs toPrivatePerson(KorrespondansepartResource korrespondansepartResource) {
        SynchronizePrivatePersonArgs synchronizePrivatePersonArgs = new SynchronizePrivatePersonArgs();
        Personnavn personnavn = parsePersonnavn(korrespondansepartResource.getKorrespondansepartNavn());
        synchronizePrivatePersonArgs.setFirstName(personnavn.getFornavn());
        synchronizePrivatePersonArgs.setLastName(personnavn.getEtternavn());
        synchronizePrivatePersonArgs.setPersonalIdNumber(
                korrespondansepartResource.getFodselsnummer());

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .ifPresent(synchronizePrivatePersonArgs::setEmail);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .ifPresent(synchronizePrivatePersonArgs::setMobilePhone);

        synchronizePrivatePersonArgs.setPrivateAddress(createAddress(korrespondansepartResource.getAdresse(), new PrivateAddress__3()));

        return synchronizePrivatePersonArgs;
    }

    public SynchronizeEnterpriseArgs toEnterprise(KorrespondansepartResource korrespondansepartResource) {
        SynchronizeEnterpriseArgs synchronizeEnterpriseArgs = new SynchronizeEnterpriseArgs();

        synchronizeEnterpriseArgs.setName(korrespondansepartResource.getKorrespondansepartNavn());
        synchronizeEnterpriseArgs.setEnterpriseNumber(korrespondansepartResource.getOrganisasjonsnummer());

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .ifPresent(synchronizeEnterpriseArgs::setEmail);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .ifPresent(synchronizeEnterpriseArgs::setMobilePhone);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)
                .ifPresent(synchronizeEnterpriseArgs::setPhoneNumber);

        ofNullable(korrespondansepartResource.getKontaktinformasjon())
                .map(Kontaktinformasjon::getNettsted)
                .ifPresent(synchronizeEnterpriseArgs::setWeb);

        synchronizeEnterpriseArgs.setPostAddress(createAddress(korrespondansepartResource.getAdresse(), new PostAddress__4()));

        return synchronizeEnterpriseArgs;
    }

    private PrivateAddress__3 createAddress(AdresseResource adresse, PrivateAddress__3 address) {
        address.setCountry("NOR");
        ofNullable(adresse.getAdresselinje())
                .map(l -> l.get(0))
                .ifPresent(address::setStreetAddress);
        address.setZipCode(adresse.getPostnummer());
        address.setZipPlace(adresse.getPoststed());

        return address;
    }

    private PostAddress__4 createAddress(AdresseResource adresse, PostAddress__4 address) {
        address.setCountry("NOR");
        ofNullable(adresse.getAdresselinje())
                .map(l -> l.get(0))
                .ifPresent(address::setStreetAddress);
        address.setZipCode(adresse.getPostnummer());
        address.setZipPlace(adresse.getPoststed());

        return address;
    }

    private void setPhones(UnregisteredContact contact, Kontaktinformasjon kontaktinformasjon) {
        if (kontaktinformasjon == null)
            return;

        contact.setMobilePhone(kontaktinformasjon.getMobiltelefonnummer());
        contact.setPhone(kontaktinformasjon.getTelefonnummer());
        contact.setEmail(kontaktinformasjon.getEpostadresse());
    }

    private void setAddress(UnregisteredContact contact, AdresseResource adresse) {
        if (adresse == null)
            return;

        contact.setZipCode(adresse.getPostnummer());
        contact.setZipPlace(adresse.getPoststed());
        contact.setAddress(String.join("\n", adresse.getAdresselinje()));

        applyParameterFromLink(adresse.getLand(), contact::setCountry);
    }


    public KorrespondansepartResource toFintResource(Contact__1 contact) {
        KorrespondansepartResource result = new KorrespondansepartResource();
        result.setAdresse(createAdresseResource(contact));
        result.setKontaktinformasjon(createKontaktinformasjon(contact));
        optionalValue(contact.getRole())
                .flatMap(role ->
                        kodeverkRepository
                                .getKorrespondansepartType()
                                .stream()
                                .filter(v -> StringUtils.equalsIgnoreCase(role, v.getKode()))
                                .findAny())
                .map(KorrespondansepartTypeResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(KorrespondansepartType.class, "systemid"))
                .ifPresent(result::addKorrespondanseparttype);
        return result;
    }
}
