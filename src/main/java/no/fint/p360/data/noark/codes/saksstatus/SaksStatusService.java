package no.fint.p360.data.noark.codes.saksstatus;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.SaksstatusResource;
import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class SaksStatusService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.case-status:code table: Case status}")
    private String caseStatusTable;

    public Stream<SaksstatusResource> getCaseStatusTable() {
        return supportService.getCodeTableRowResultStream(caseStatusTable)
                .map(BegrepMapper.mapValue(SaksstatusResource::new));
    }
}
