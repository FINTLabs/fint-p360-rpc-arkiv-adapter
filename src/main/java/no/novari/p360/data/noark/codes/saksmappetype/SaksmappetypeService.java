package no.novari.p360.data.noark.codes.saksmappetype;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.novari.p360.data.p360.SupportService;
import no.novari.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class SaksmappetypeService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.case-status:code table: Case type}")
    private String caseTypeTable;

    public Stream<SaksmappetypeResource> getCaseTypeTable() {
        return supportService.getCodeTableRowResultStream(caseTypeTable)
                .map(BegrepMapper.mapValue(SaksmappetypeResource::new));
    }
}
