package no.fint.p360.data.noark.codes.tilgangsgruppe;

import org.springframework.stereotype.Service;

import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.fint.p360.data.utilities.FintUtils;
import no.p360.model.AccessGroupService.AccessGroup;

@Service
public class TilgangsgruppeFactory {
    
    public TilgangsgruppeResource toFintResource(AccessGroup accessGroup) {
        TilgangsgruppeResource result = new TilgangsgruppeResource();
        result.setSystemId(FintUtils.createIdentifikator(String.valueOf(accessGroup.getRecno())));
        result.setNavn(accessGroup.getCode());
        return result;
    }
}
