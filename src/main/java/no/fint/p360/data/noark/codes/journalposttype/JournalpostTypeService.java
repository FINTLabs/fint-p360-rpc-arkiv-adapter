package no.fint.p360.data.noark.codes.journalposttype;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.JournalpostTypeResource;
import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class JournalpostTypeService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.document-category:code table: Document category}")
    private String documentCategoryTable;

    public Stream<JournalpostTypeResource> getDocumentCategoryTable() {
        return supportService.getCodeTableRowResultStream(documentCategoryTable)
                .map(BegrepMapper.mapValue(JournalpostTypeResource::new));
    }
}
