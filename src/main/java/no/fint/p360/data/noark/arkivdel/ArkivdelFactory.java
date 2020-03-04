package no.fint.p360.data.noark.arkivdel;

import no.fint.model.resource.administrasjon.arkiv.ArkivdelResource;
import no.p360.model.SupportService.CodeTableRow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static no.fint.p360.data.utilities.FintUtils.createIdentifikator;
import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class ArkivdelFactory {
    public ArkivdelResource toFintResource(CodeTableRow codeTableRowResult) {
        ArkivdelResource resource = new ArkivdelResource();
        resource.setSystemId(createIdentifikator(String.valueOf(codeTableRowResult.getRecno())));

        optionalValue(codeTableRowResult.getCode())
                .filter(StringUtils::isNotBlank)
                .ifPresent(resource::setTittel);

        return resource;
    }
}
