package no.novari.p360.data.noark.codes.dokumentstatus;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.DokumentStatusResource;
import no.novari.p360.data.p360.SupportService;
import no.novari.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class DokumentstatusService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.document-status:code table: FileStatus}")
    private String documentStatusTable;

    public Stream<DokumentStatusResource> getDocumentStatusTable() {
        return supportService.getCodeTableRowResultStream(documentStatusTable)
                .map(BegrepMapper.mapValue(DokumentStatusResource::new));
    }
}
