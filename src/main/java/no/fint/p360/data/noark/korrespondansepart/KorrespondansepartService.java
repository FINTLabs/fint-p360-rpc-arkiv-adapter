package no.fint.p360.data.noark.korrespondansepart;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.noark.skjerming.SkjermingService;
import no.fint.p360.data.p360.ContactService;
import no.p360.model.ContactService.Enterprise;
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

import static no.fint.p360.data.utilities.P360Utils.getLinkTargets;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
public class KorrespondansepartService {

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    @Autowired
    private ContactService contactService;

    public Pair<List<Contact>, List<UnregisteredContact>> getContactsFromKorrespondansepart(List<KorrespondansepartResource> korrespondansepart,
                                                                                            boolean isRestricted) {
        final LinkedList<Contact> contacts = new LinkedList<>();
        final LinkedList<UnregisteredContact> unregisteredContacts = new LinkedList<>();

        for (KorrespondansepartResource resource : korrespondansepart) {
            // TODO Should be controlled by a default false property.
            // According to TietoEvry we should never insert internal contacts.
            if (getLinkTargets(resource.getKorrespondanseparttype())
                    .anyMatch(s -> StringUtils.equalsAnyIgnoreCase(s, "IA", "IM", "IK"))) {
                log.debug("Ignoring internal contact {}", resource);
                continue;
            }
            try {
                if (isNotBlank(resource.getFodselsnummer())) {
                    final SynchronizePrivatePersonArgs synchronizePrivatePerson = korrespondansepartFactory.toPrivatePerson(
                            resource);
                    final Integer recno = contactService.synchronizePrivatePerson(synchronizePrivatePerson);
                    log.info("Private person recno = {}", recno);
                    contacts.add(maybeUnofficial(isRestricted,
                            resource.getSkjerming(),
                            korrespondansepartFactory.createDocumentContact(recno, resource)));
                } else if (isNotBlank(resource.getOrganisasjonsnummer())) {
                    final SynchronizeEnterpriseArgs synchronizeEnterprise = korrespondansepartFactory.toEnterprise(
                            resource);
                    final Integer recno = contactService.synchronizeEnterprise(synchronizeEnterprise);
                    log.info("Enterprise recno = {}", recno);
                    contacts.add(maybeUnofficial(isRestricted,
                            resource.getSkjerming(),
                            korrespondansepartFactory.createDocumentContact(recno, resource)));
                } else {
                    log.info("Adding unregistered contact {}", resource.getKorrespondansepartNavn());
                    unregisteredContacts.add(maybeUnofficial(isRestricted,
                            resource.getSkjerming(),
                            korrespondansepartFactory.createDocumentUnregisteredContact(resource)));
                }
            } catch (CreateContactException | CreateEnterpriseException e) {
                log.warn("Creating unregistered contact {} due to error", resource.getKorrespondansepartNavn(), e);
                unregisteredContacts.add(maybeUnofficial(isRestricted,
                        resource.getSkjerming(),
                        korrespondansepartFactory.createDocumentUnregisteredContact(resource)));
            }
        }

        return new ImmutablePair<>(contacts, unregisteredContacts);
    }

    private Contact maybeUnofficial(boolean isRestricted, SkjermingResource skjerming, Contact input) {
        if (isRestricted && SkjermingService.hasTilgangsrestriksjon(skjerming)) {
            input.setIsUnofficial(true);
        }
        return input;
    }

    private UnregisteredContact maybeUnofficial(boolean isRestricted,
                                                SkjermingResource skjerming,
                                                UnregisteredContact input) {
        if (isRestricted && SkjermingService.hasTilgangsrestriksjon(skjerming)) {
            input.setIsUnofficial(true);
        }
        return input;
    }

    private Enterprise getEnterpriseByEnterpriseNumber(String organisasjonsnummer) {
        try {
            return contactService.getEnterpriseByEnterpriseNumber(organisasjonsnummer);
        } catch (EnterpriseNotFound enterpriseNotFound) {
            return new Enterprise();
        }
    }

}
