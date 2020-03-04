package no.fint.p360.data.noark.codes.skjermingshjemmel;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.SkjermingshjemmelResource;
import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class SkjermingshjemmelService {

    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.law:code table: Law}")
    private String tableName;

    public Stream<SkjermingshjemmelResource> getLawTable() {
        return supportService.getCodeTableRowResultStream(tableName)
                .map(BegrepMapper.mapValue(SkjermingshjemmelResource::new));
    }
}
