package no.fint.p360.data.noark.codes.tilgangsgruppe;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.fint.p360.data.p360.AccessGroupService;
import no.p360.model.AccessGroupService.GetAccessGroupsArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class TilgangsgruppeService {

    @Value("${fint.p360.defaults.tilgangsgruppe:Alle}")
    private String defaultTilgangsgruppe;

    @Autowired
    private AccessGroupService accessGroupService;

    @Autowired
    private TilgangsgruppeFactory tilgangsgruppeFactory;

    public Stream<TilgangsgruppeResource> getTilgangsgruppeResource() {
        log.info("Getting tilgangsgrupper...");
        GetAccessGroupsArgs args = new GetAccessGroupsArgs();
        args.setIncludeMembers(false);
        return accessGroupService.getAccessGroups(args)
            .stream()
            .map(tilgangsgruppeFactory::toFintResource);
    }
}
