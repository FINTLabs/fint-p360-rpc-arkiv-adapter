package no.novari.p360.data.noark.codes.tilgangsgruppe;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.novari.p360.data.p360.AccessGroupService;
import no.p360.model.AccessGroupService.GetAccessGroupsArgs;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class TilgangsgruppeService {

    private final AccessGroupService accessGroupService;
    private final TilgangsgruppeFactory tilgangsgruppeFactory;

    public TilgangsgruppeService(AccessGroupService accessGroupService, TilgangsgruppeFactory tilgangsgruppeFactory) {
        this.accessGroupService = accessGroupService;
        this.tilgangsgruppeFactory = tilgangsgruppeFactory;
    }

    public Stream<TilgangsgruppeResource> getTilgangsgruppeResource() {

        GetAccessGroupsArgs args = new GetAccessGroupsArgs();
        args.setIncludeMembers(false);
        args.setMaxRows(0);

        return Optional.ofNullable(accessGroupService.getAccessGroups(args))
                .orElseGet((() -> {
                    log.warn("Not able to get access groups! Do you have the right privileges?");
                    return List.of();
                }))
                .stream()
                .map(tilgangsgruppeFactory::toFintResource);
    }
}
