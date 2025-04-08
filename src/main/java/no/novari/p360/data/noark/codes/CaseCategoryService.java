package no.novari.p360.data.noark.codes;

import no.novari.p360.data.p360.SupportService;
import no.p360.model.SupportService.CodeTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class CaseCategoryService {
    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.document-status:code table: Case category}")
    private String documentStatusTable;

    public Stream<String> getCaseCategoryTable() {
        return supportService
                .getCodeTableRowResultStream(documentStatusTable)
                .map(CodeTableRow::getDescription);
    }
}
