package no.fint.p360.data.noark.codes.klasse;


import lombok.extern.slf4j.Slf4j;
import no.fint.model.arkiv.noark.Klassifikasjonssystem;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.p360.model.CaseService.ArchiveCode__1;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlasseFactory {

    public KlasseResource toFintResource(ArchiveCode__1 archiveCode) {
        KlasseResource klasseResource = new KlasseResource();

        // TODO
        klasseResource.setRekkefolge(archiveCode.getSort());
        klasseResource.addKlassifikasjonssystem(Link.with(Klassifikasjonssystem.class, "systemid", archiveCode.getArchiveType()));

        return klasseResource;

    }
}
