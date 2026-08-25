package no.novari.fint.p360.data.noark.arkivressurs;

import no.novari.fint.model.administrasjon.personal.Personalressurs;
import no.novari.fint.model.resource.Link;
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.novari.fint.p360.data.utilities.FintUtils;
import no.novari.fint.p360.repository.PersonalressursRepository;
import no.p360.model.ContactService.ContactPerson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArkivressursFactory {

    @Autowired
    private PersonalressursRepository personalressursRepository;

    public ArkivressursResource toFintResource(ContactPerson contactPerson) {
        ArkivressursResource arkivressurs = new ArkivressursResource();
        arkivressurs.setSystemId(FintUtils.createIdentifikator(String.valueOf(contactPerson.getRecno())));

        FintUtils.optionalValue(contactPerson.getExternalId())
                .filter(StringUtils::isNotBlank)
                .map(FintUtils::createIdentifikator)
                .ifPresent(arkivressurs::setKildesystemId);


        personalressursRepository.getByEmail(contactPerson.getEmail()).ifPresent(
                personalressurs -> arkivressurs.addPersonalressurs(
                        Link.with(Personalressurs.class, "ansattnummer",
                                personalressurs.getAnsattnummer().getIdentifikatorverdi()))
        );

        return arkivressurs;
    }
}
