package no.fint.p360.data.noark.codes.tilgangsrestriksjon;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.TilgangsrestriksjonResource;
import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class TilgangsrestriksjonService {

    @Autowired
    private SupportService supportService;
    @Value("${fint.p360.tables.access-code:code table: Access code}")
    private String tableName;

    public Stream<TilgangsrestriksjonResource> getAccessCodeTable() {
        return supportService.getCodeTableRowResultStream(tableName)
                .map(BegrepMapper.mapValue(TilgangsrestriksjonResource::new));
    }
}
