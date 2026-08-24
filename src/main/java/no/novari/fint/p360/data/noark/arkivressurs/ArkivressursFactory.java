package no.novari.fint.p360.data.noark.arkivressurs;

import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.novari.fint.p360.data.utilities.FintUtils;
import no.p360.model.ContactService.ContactPerson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ArkivressursFactory {
    public ArkivressursResource toFintResource(ContactPerson contactPerson) {
        ArkivressursResource arkivressurs = new ArkivressursResource();
        arkivressurs.setSystemId(FintUtils.createIdentifikator(String.valueOf(contactPerson.getRecno())));

        FintUtils.optionalValue(contactPerson.getExternalId())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(arkivressurs::setKildesystemId);

        // TODO: Sett relasjonen personalressurs

        return arkivressurs;
    }
}
