package no.fint.p360.data.noark.part;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.PartRolleResource;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.p360.repository.KodeverkRepository;
import no.p360.model.CaseService.Contact;
import no.p360.model.CaseService.Contact__1;
import no.p360.model.CaseService.UnregisteredContact;
import no.p360.model.ContactService.SynchronizeEnterpriseArgs;
import no.p360.model.ContactService.SynchronizePrivatePersonArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.fint.p360.data.utilities.FintUtils.createAdresseResource;
import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@SuppressWarnings("Duplicates")
@Service
public class PartFactory {

    @Autowired
    KodeverkRepository kodeverkRepository;

    public PartResource getPartsinformasjon(Contact__1 caseContactResult) {
        PartResource part = new PartResource();

        part.setPartNavn(caseContactResult.getContactName());
        part.setAdresse(createAdresseResource(caseContactResult.getAddress()));

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

        //TODO Set role on the contact?
        contact.setRole("Sakspart");

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

        // TODO Add address and contact information

        return unregisteredContact;
    }


    public SynchronizePrivatePersonArgs toPrivatePerson(PartResource sakspart) {
        SynchronizePrivatePersonArgs synchronizePrivatePerson = new SynchronizePrivatePersonArgs();

        //TODO Fix name, everything goes in the first name field for now
        synchronizePrivatePerson.setFirstName(sakspart.getPartNavn());

        return synchronizePrivatePerson;
    }

    public SynchronizeEnterpriseArgs toEnterprise(PartResource sakspart) {
        SynchronizeEnterpriseArgs synchronizeEnterprise = new SynchronizeEnterpriseArgs();

        //TODO Fix all fields, currently only name and number
        synchronizeEnterprise.setEnterpriseNumber(sakspart.getOrganisasjonsnummer());
        synchronizeEnterprise.setName(sakspart.getPartNavn());

        return synchronizeEnterprise;
    }
}
