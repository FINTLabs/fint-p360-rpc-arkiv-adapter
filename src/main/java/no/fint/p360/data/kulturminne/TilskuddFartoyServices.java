package no.fint.p360.data.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.p360.service.CaseQueryService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class TilskuddFartoyServices {

    private final TilskuddFartoyFactory tilskuddFartoyFactory;
    private final CaseQueryService caseQueryService;

    public TilskuddFartoyServices(TilskuddFartoyFactory tilskuddFartoyFactory, CaseQueryService caseQueryService) {
        this.tilskuddFartoyFactory = tilskuddFartoyFactory;
        this.caseQueryService = caseQueryService;
    }

    public void getTilskuddFartoyForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(tilskuddFartoyFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
