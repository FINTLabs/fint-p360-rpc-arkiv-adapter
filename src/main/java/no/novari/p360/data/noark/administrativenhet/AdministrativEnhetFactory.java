package no.novari.p360.data.noark.administrativenhet;

import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.novari.p360.data.utilities.FintUtils;
import no.p360.model.ContactService.Enterprise;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AdministrativEnhetFactory {
    public AdministrativEnhetResource toFintResource(Enterprise enterprise) {
        AdministrativEnhetResource result = new AdministrativEnhetResource();
        result.setSystemId(FintUtils.createIdentifikator(String.valueOf(enterprise.getRecno())));
        result.setNavn(enterprise.getName());
        if (StringUtils.isNotBlank(enterprise.getEnterpriseNumber())) {
            result.addOrganisasjonselement(Link.with(Organisasjonselement.class, "organisasjonsnummer", enterprise.getEnterpriseNumber()));
        }
        if (StringUtils.isNotBlank(enterprise.getCreatedDate())) {
            result.setGyldighetsperiode(FintUtils.createPeriode(FintUtils.parseIsoDate(enterprise.getCreatedDate())));
        }
        return result;
    }
}
