package no.fint.p360.data.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.p360.service.CaseQueryService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class TilskuddFredaBygningPrivatEieService {

    private final TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory;
    private final CaseQueryService caseQueryService;

    public TilskuddFredaBygningPrivatEieService(TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory, CaseQueryService caseQueryService) {
        this.tilskuddFredaBygningPrivatEieFactory = tilskuddFredaBygningPrivatEieFactory;
        this.caseQueryService = caseQueryService;
    }

    public void getTilskuddFredaBygningPrivatEieForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(tilskuddFredaBygningPrivatEieFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
