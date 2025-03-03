package no.fint.p360.data.noark.part;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.p360.ContactService;
import no.p360.model.CaseService.Contact;
import no.p360.model.CaseService.UnregisteredContact;
import no.p360.model.ContactService.SynchronizeEnterpriseArgs;
import no.p360.model.ContactService.SynchronizePrivatePersonArgs;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
public class PartService {

    private final PartFactory partFactory;
    private final ContactService contactService;

    public PartService(PartFactory partFactory, ContactService contactService) {
        this.partFactory = partFactory;
        this.contactService = contactService;
    }

    public Pair<List<Contact>, List<UnregisteredContact>> getContactsFromSakspart(List<PartResource> partResource) {
        final LinkedList<Contact> contacts = new LinkedList<>();
        final LinkedList<UnregisteredContact> unregisteredContacts = new LinkedList<>();

        for (PartResource sakspart : partResource) {
            try {
                if (isNotBlank(sakspart.getFodselsnummer())) {
                    final SynchronizePrivatePersonArgs synchronizePrivatePerson = partFactory.toPrivatePerson(sakspart);
                    final Integer recno = contactService.synchronizePrivatePerson(synchronizePrivatePerson);
                    log.debug("Private person recno: {}", recno);

                    contacts.add(partFactory.createCaseContact(recno, sakspart));
                } else if (isNotBlank(sakspart.getOrganisasjonsnummer())) {
                    final SynchronizeEnterpriseArgs  synchronizeEnterprise = partFactory.toEnterprise(sakspart);
                    final Integer recno = contactService.synchronizeEnterprise(synchronizeEnterprise);
                    log.debug("Enterprise recno: {}", recno);

                    contacts.add(partFactory.createCaseContact(recno, sakspart));
                } else {
                    log.debug("Adding unregistered sakspart: {}", sakspart.getPartNavn());
                    unregisteredContacts.add(partFactory.createUnregisteredContact(sakspart));
                }
            } catch (CreateContactException | CreateEnterpriseException e) {
                log.warn("Creating unregistered sakspart due to error: {}", sakspart.getPartNavn(), e);
                unregisteredContacts.add(partFactory.createUnregisteredContact(sakspart));
            }
        }

        return new ImmutablePair<>(contacts, unregisteredContacts);
    }
}
