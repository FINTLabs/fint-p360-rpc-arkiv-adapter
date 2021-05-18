package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
import no.fint.p360.model.ContextUser;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.ContextUserService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Slf4j
@Service
public class TilskuddFredaBygningPrivatEieService {

    private final TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory;
    private final CaseQueryService caseQueryService;
    private final ContextUser contextUser;

    public TilskuddFredaBygningPrivatEieService(TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory, CaseQueryService caseQueryService, ContextUserService contextUserService) {
        this.tilskuddFredaBygningPrivatEieFactory = tilskuddFredaBygningPrivatEieFactory;
        this.caseQueryService = caseQueryService;

        contextUser = contextUserService.getContextUserForClass(TilskuddFredaBygningPrivatEieResource.class);
    }

    public void getTilskuddFredaBygningPrivatEieForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(tilskuddFredaBygningPrivatEieFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
