package no.novari.p360.data.noark.codes.dokumenttype;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.DokumentTypeResource;
import no.novari.p360.data.p360.SupportService;
import no.novari.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class DokumenttypeService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.document-type:code table: File category}")
    private String documentStatusTable;

    public Stream<DokumentTypeResource> getDocumenttypeTable() {
        return supportService.getCodeTableRowResultStream(documentStatusTable)
                .map(BegrepMapper.mapValue(DokumentTypeResource::new));
    }
}
