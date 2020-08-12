package no.fint.p360.data.noark.korrespondansepart;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Personnavn;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fint.p360.data.utilities.FintUtils;
import no.p360.model.ContactService.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.*;

@SuppressWarnings("Duplicates")
@Service
public class KorrespondansepartFactory {

    public KorrespondansepartResource toFintResource(PrivatePerson result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setAdresse(FintUtils.createAdresse(result));
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(FintUtils.getFullNameString(result));
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));
        optionalValue(result.getPersonalIdNumber())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(korrespondansepartResource::setFodselsnummer);

        return korrespondansepartResource;
    }

    public KorrespondansepartResource toFintResource(ContactPerson result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setAdresse(FintUtils.createAdresse(result));
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(FintUtils.getFullNameString(result));
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));

        return korrespondansepartResource;
    }

    public KorrespondansepartResource toFintResource(Enterprise result) {

        if (result == null) {
            return null;
        }

        KorrespondansepartResource korrespondansepartResource = new KorrespondansepartResource();
        korrespondansepartResource.setAdresse(FintUtils.createAdresse(result));
        korrespondansepartResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        korrespondansepartResource.setKorrespondansepartNavn(result.getName());
        korrespondansepartResource.setKontaktperson(FintUtils.getKontaktpersonString(result));
        korrespondansepartResource.setSystemId(createIdentifikator(result.getRecno().toString()));
        optionalValue(result.getEnterpriseNumber())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(korrespondansepartResource::setOrganisasjonsnummer);

        return korrespondansepartResource;
    }

    public SynchronizePrivatePersonArgs toPrivatePerson(KorrespondansepartResource korrespondansepartResource) {
        SynchronizePrivatePersonArgs synchronizePrivatePersonArgs = new SynchronizePrivatePersonArgs();
        Personnavn personnavn = parsePersonnavn(korrespondansepartResource.getKorrespondansepartNavn());
        synchronizePrivatePersonArgs.setFirstName(personnavn.getFornavn());
        synchronizePrivatePersonArgs.setLastName(personnavn.getEtternavn());
        synchronizePrivatePersonArgs.setPersonalIdNumber(
                korrespondansepartResource.getFodselsnummer().getIdentifikatorverdi());

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
        synchronizeEnterpriseArgs.setEnterpriseNumber(korrespondansepartResource.getOrganisasjonsnummer().getIdentifikatorverdi());

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
}
