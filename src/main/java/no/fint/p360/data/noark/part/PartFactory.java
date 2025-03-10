package no.fint.p360.data.noark.part;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.PartRolleResource;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.repository.KodeverkRepository;
import no.p360.model.CaseService.Contact;
import no.p360.model.CaseService.Contact__1;
import no.p360.model.CaseService.UnregisteredContact;
import no.p360.model.ContactService.PostAddress__4;
import no.p360.model.ContactService.PostAddress__5;
import no.p360.model.ContactService.SynchronizeEnterpriseArgs;
import no.p360.model.ContactService.SynchronizePrivatePersonArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.createAdresseResource;
import static no.fint.p360.data.utilities.FintUtils.optionalValue;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@SuppressWarnings("Duplicates")
@Slf4j
@Service
public class PartFactory {

    @Autowired
    KodeverkRepository kodeverkRepository;

    public PartResource getPartsinformasjon(Contact__1 caseContactResult) {
        PartResource part = new PartResource();
        part.setPartNavn(caseContactResult.getContactName());

        ofNullable(caseContactResult.getReferencenumber())
                .filter(organisationNumber -> StringUtils.length(organisationNumber) == 9)
                .ifPresent(part::setOrganisasjonsnummer);

        ofNullable(caseContactResult.getReferencenumber())
                .filter(nin -> StringUtils.length(nin) == 11)
                .ifPresent(part::setFodselsnummer);

        if (caseContactResult.getAddress() != null) {
            part.setAdresse(createAdresseResource(caseContactResult.getAddress()));
        }

        // TODO part.setKontaktinformasjon();
        // TODO part.setKontaktperson();

        optionalValue(caseContactResult.getRole())
                .flatMap(role ->
                        kodeverkRepository
                                .getPartRolle()
                                .stream()
                                .filter(v -> StringUtils.equalsIgnoreCase(role, v.getKode()))
                                .findAny())
                .map(PartRolleResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(PartRolleResource.class, "systemid"))
                .ifPresent(part::addPartRolle);

        return part;
    }

    public Contact createCaseContact(Integer recno, PartResource part) {
        Contact contact = new Contact();
        contact.setReferenceNumber("recno:" + recno);

        applyParameterFromLink(
                part.getPartRolle(),
                contact::setRole
        );

        log.debug("Part with reference number {} and role {} created.", contact.getReferenceNumber(),
                contact.getRole());
        return contact;
    }

    public UnregisteredContact createUnregisteredContact(PartResource part) {
        UnregisteredContact unregisteredContact = new UnregisteredContact();

        if (StringUtils.isNotBlank(part.getOrganisasjonsnummer())) {
            unregisteredContact.setReferenceNumber(part.getOrganisasjonsnummer());
        } else if (StringUtils.isNotBlank(part.getFodselsnummer())) {
            unregisteredContact.setReferenceNumber(part.getFodselsnummer());
        }

        unregisteredContact.setContactName(part.getPartNavn());

        ofNullable(part.getAdresse())
                .map(AdresseResource::getAdresselinje)
                .map(line -> String.join("\n", line))
                .filter(StringUtils::isNotBlank)
                .ifPresent(unregisteredContact::setAddress);

        ofNullable(part.getAdresse())
                .map(AdresseResource::getPostnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(unregisteredContact::setZipCode);

        ofNullable(part.getAdresse())
                .map(AdresseResource::getPoststed)
                .filter(StringUtils::isNotBlank)
                .ifPresent(unregisteredContact::setZipPlace);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .filter(StringUtils::isNotBlank)
                .ifPresent(unregisteredContact::setEmail);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(unregisteredContact::setMobilePhone);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(unregisteredContact::setPhone);

        return unregisteredContact;
    }

    public SynchronizePrivatePersonArgs toPrivatePerson(PartResource part) {
        SynchronizePrivatePersonArgs synchronizePrivatePerson = new SynchronizePrivatePersonArgs();

        synchronizePrivatePerson.setFirstName(FintUtils.parsePersonnavn(
                part.getPartNavn()).getFornavn());
        synchronizePrivatePerson.setLastName(FintUtils.parsePersonnavn(
                part.getPartNavn()).getEtternavn());

        ofNullable(part.getAdresse()).ifPresent(adresse ->
                synchronizePrivatePerson.setPostAddress(createPostAddress(adresse, new PostAddress__5())));

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .filter(StringUtils::isNotBlank)
                .ifPresent(synchronizePrivatePerson::setEmail);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(synchronizePrivatePerson::setMobilePhone);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(synchronizePrivatePerson::setPhoneNumber);

        return synchronizePrivatePerson;
    }

    public SynchronizeEnterpriseArgs toEnterprise(PartResource part) {
        SynchronizeEnterpriseArgs synchronizeEnterprise = new SynchronizeEnterpriseArgs();

        synchronizeEnterprise.setEnterpriseNumber(part.getOrganisasjonsnummer());
        synchronizeEnterprise.setName(part.getPartNavn());

        ofNullable(part.getAdresse()).ifPresent(adresse ->
                synchronizeEnterprise.setPostAddress(createPostAddress(adresse, new PostAddress__4())));

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .filter(StringUtils::isNotBlank)
                .ifPresent(synchronizeEnterprise::setEmail);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(synchronizeEnterprise::setMobilePhone);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(synchronizeEnterprise::setPhoneNumber);

        return synchronizeEnterprise;
    }

    private PostAddress__4 createPostAddress(AdresseResource adresseResource, PostAddress__4 postAddress) {
        postAddress.setCountry("NOR");
        ofNullable(adresseResource.getAdresselinje())
                .map(line -> String.join("\n", line))
                .ifPresent(postAddress::setStreetAddress);

        ofNullable(adresseResource.getPostnummer())
                .ifPresent(postAddress::setZipCode);

        ofNullable(adresseResource.getPoststed())
                .ifPresent(postAddress::setZipPlace);

        return postAddress;
    }

    private PostAddress__5 createPostAddress(AdresseResource adresseResource, PostAddress__5 postAddress) {
        postAddress.setCountry("NOR");
        ofNullable(adresseResource.getAdresselinje())
                .map(line -> String.join("\n", line))
                .ifPresent(postAddress::setStreetAddress);

        ofNullable(adresseResource.getPostnummer())
                .ifPresent(postAddress::setZipCode);

        ofNullable(adresseResource.getPoststed())
                .ifPresent(postAddress::setZipPlace);

        return postAddress;
    }
}
