package no.fint.p360.data.noark.korrespondansepart;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;
import no.fint.p360.data.p360.ContactService;
import no.p360.model.ContactService.Enterprise;
import no.p360.model.ContactService.PrivatePerson;
import no.p360.model.ContactService.SynchronizeEnterpriseArgs;
import no.p360.model.ContactService.SynchronizePrivatePersonArgs;
import no.p360.model.DocumentService.Contact;
import no.p360.model.DocumentService.UnregisteredContact;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class KorrespondansepartService {

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private ContactService contactService;

    public Pair<List<Contact>, List<UnregisteredContact>> getContactsFromKorrespondansepart(List<KorrespondansepartResource> korrespondansepart) {
        final LinkedList<Contact> contacts = new LinkedList<>();
        final LinkedList<UnregisteredContact> unregisteredContacts = new LinkedList<>();

        for (KorrespondansepartResource resource : korrespondansepart) {
            try {
                if (StringUtils.isNotBlank(resource.getFodselsnummer())) {
                    final PrivatePerson privatePerson = getPrivatePersonByPersonalIdNumber(resource.getFodselsnummer());
                    final SynchronizePrivatePersonArgs synchronizePrivatePerson = korrespondansepartFactory.toPrivatePerson(resource);
                    if (!StringUtils.equals(privatePerson.getFirstName(), synchronizePrivatePerson.getFirstName())
                            || !StringUtils.equals(privatePerson.getMiddleName(), synchronizePrivatePerson.getMiddleName())
                            || !StringUtils.equals(privatePerson.getLastName(), synchronizePrivatePerson.getLastName())
                            || !StringUtils.equals(privatePerson.getPersonalIdNumber(), synchronizePrivatePerson.getPersonalIdNumber())
                            || !StringUtils.equals(privatePerson.getMobilePhone(), synchronizePrivatePerson.getMobilePhone())
                            || !StringUtils.equals(privatePerson.getEmail(), synchronizePrivatePerson.getEmail())
                    ) {
                        log.info("Updating private person {}", resource.getFodselsnummer());
                        final Integer recno = contactService.synchronizePrivatePerson(synchronizePrivatePerson);
                        log.info("Private person recno = {}", recno);
                        contacts.add(korrespondansepartFactory.createDocumentContact(recno, resource));
                    }
                } else if (StringUtils.isNotBlank(resource.getOrganisasjonsnummer())) {
                    final Enterprise enterprise = getEnterpriseByEnterpriseNumber(resource.getOrganisasjonsnummer());
                    final SynchronizeEnterpriseArgs synchronizeEnterprise = korrespondansepartFactory.toEnterprise(resource);
                    if (!StringUtils.equals(enterprise.getName(), synchronizeEnterprise.getName())
                            || !StringUtils.equals(enterprise.getEnterpriseNumber(), synchronizeEnterprise.getEnterpriseNumber())
                            || !StringUtils.equals(enterprise.getMobilePhone(), synchronizeEnterprise.getMobilePhone())
                            || !StringUtils.equals(enterprise.getPhoneNumber(), synchronizeEnterprise.getPhoneNumber())
                            || !StringUtils.equals(enterprise.getEmail(), synchronizeEnterprise.getEmail())
                            || !StringUtils.equals(enterprise.getWeb(), synchronizeEnterprise.getWeb())
                    ) {
                        log.info("Updating enterprise {}", resource.getOrganisasjonsnummer());
                        final Integer recno = contactService.synchronizeEnterprise(synchronizeEnterprise);
                        contacts.add(korrespondansepartFactory.createDocumentContact(recno, resource));
                    }
                } else {
                    log.info("Adding unregistered contact {}", resource.getKorrespondansepartNavn());
                    unregisteredContacts.add(korrespondansepartFactory.createDocumentUnregisteredContact(resource));
                }
            } catch (CreateContactException | CreateEnterpriseException e) {
                log.warn("Creating unregistered contact {} due to error", resource.getKorrespondansepartNavn(), e);
                unregisteredContacts.add(korrespondansepartFactory.createDocumentUnregisteredContact(resource));
            }
        }

        return new ImmutablePair<>(contacts, unregisteredContacts);
    }

    private Enterprise getEnterpriseByEnterpriseNumber(String organisasjonsnummer) {
        try {
            return contactService.getEnterpriseByEnterpriseNumber(organisasjonsnummer);
        } catch (EnterpriseNotFound enterpriseNotFound) {
            return new Enterprise();
        }
    }

    private PrivatePerson getPrivatePersonByPersonalIdNumber(String fodselsnummer) {
        try {
            return contactService.getPrivatePersonByPersonalIdNumber(fodselsnummer);
        } catch (PrivatePersonNotFound privatePersonNotFound) {
            return new PrivatePerson();
        }
    }

}