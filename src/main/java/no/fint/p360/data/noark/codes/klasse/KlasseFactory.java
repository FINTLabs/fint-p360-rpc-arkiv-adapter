package no.fint.p360.data.noark.codes.klasse;


import lombok.extern.slf4j.Slf4j;
import no.fint.model.arkiv.noark.Klassifikasjonssystem;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fint.p360.data.noark.codes.klassifikasjonssystem.KlassifikasjonssystemService;
import no.p360.model.CaseService.ArchiveCode__1;
import no.p360.model.SupportService.CodeTableRow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlasseFactory {

    @Autowired
    private KlassifikasjonssystemService klassifikasjonssystemService;

    public KlasseResource toFintResource(ArchiveCode__1 archiveCode) {
        KlasseResource klasseResource = new KlasseResource();

        // TODO klasseResource.setKlasseId();
        // TODO klasseResource.setSkjerming();
        klasseResource.setTittel(archiveCode.getArchiveCode());
        klasseResource.setRekkefolge(archiveCode.getSort());

        klassifikasjonssystemService
                .getKlassifikasjonssystem()
                .filter(k -> StringUtils.equals(k.getTittel(), archiveCode.getArchiveType()))
                .map(KlassifikasjonssystemResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Klassifikasjonssystem.class, "systemid"))
                .forEach(klasseResource::addKlassifikasjonssystem);

        return klasseResource;

    }

    public KlasseResource toFintResource(CodeTableRow row) {
        final KlasseResource klasseResource = new KlasseResource();

        klasseResource.setKlasseId(String.valueOf(row.getRecno()));
        klasseResource.setTittel(row.getCode());

        return klasseResource;
    }
}
