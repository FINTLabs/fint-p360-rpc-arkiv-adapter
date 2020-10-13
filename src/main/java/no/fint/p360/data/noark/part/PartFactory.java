package no.fint.p360.data.noark.part;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.arkiv.kodeverk.PartRolleResource;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.repository.KodeverkRepository;
import no.p360.model.CaseService.Contact__1;
import no.p360.model.ContactService.ContactPerson;
import no.p360.model.ContactService.Enterprise;
import no.p360.model.ContactService.PrivatePerson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@SuppressWarnings("Duplicates")
@Service
public class PartFactory {

    @Autowired
    KodeverkRepository kodeverkRepository;

    public PartResource toFintResource(PrivatePerson result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(FintUtils.getFullNameString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;
    }

    public PartResource toFintResource(ContactPerson result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(FintUtils.getFullNameString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;
    }

    public PartResource toFintResource(Enterprise result) {

        if (result == null) {
            return null;
        }

        PartResource partResource = new PartResource();
        partResource.setAdresse(FintUtils.createAdresse(result));
        partResource.setKontaktinformasjon(FintUtils.createKontaktinformasjon(result));
        partResource.setPartNavn(result.getName());
        partResource.setKontaktperson(FintUtils.getKontaktpersonString(result));
        partResource.setPartId(FintUtils.createIdentifikator(result.getRecno().toString()));

        return partResource;

    }

    public PartResource getPartsinformasjon(Contact__1 caseContactResult) {
        PartResource PartResource = new PartResource();

        optionalValue(caseContactResult.getRecno())
                .map(String::valueOf)
                .map(Link.apply(PartResource.class, "partid"))
                .ifPresent(PartResource::addPart);

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
                .ifPresent(PartResource::addPartRolle);

        return PartResource;
    }

}
