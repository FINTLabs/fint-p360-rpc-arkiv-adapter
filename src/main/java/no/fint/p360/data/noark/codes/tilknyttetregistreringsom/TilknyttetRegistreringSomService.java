package no.fint.p360.data.noark.codes.tilknyttetregistreringsom;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.TilknyttetRegistreringSomResource;
import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class TilknyttetRegistreringSomService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.document-relation:Attribute value: VersionFile - ToRelationType}")
    private String documentRelationTable;

    public Stream<TilknyttetRegistreringSomResource> getDocumentRelationTable() {
        return supportService.getCodeTableRowResultStream(documentRelationTable)
                .map(BegrepMapper.mapValue(TilknyttetRegistreringSomResource::new));
    }
}
