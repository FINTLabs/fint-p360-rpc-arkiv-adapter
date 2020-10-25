package no.fint.p360.data.noark.codes.partrolle;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.PartRolleResource;
import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class PartRolleService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.case-contact-role:code table: Contact - Case role}")
    private String contactRoleTable;

    public Stream<PartRolleResource> getPartRolle() {
        return supportService.getCodeTableRowResultStream(contactRoleTable)
                .map(BegrepMapper.mapValue(PartRolleResource::new));
    }
}
