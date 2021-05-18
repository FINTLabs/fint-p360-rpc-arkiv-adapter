package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.model.ContextUser;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.ContextUserService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Slf4j
@Service
public class TilskuddFartoyServices {

    private final TilskuddFartoyFactory tilskuddFartoyFactory;
    private final CaseQueryService caseQueryService;
    private final ContextUser contextUser;

    public TilskuddFartoyServices(TilskuddFartoyFactory tilskuddFartoyFactory, CaseQueryService caseQueryService, ContextUserService contextUserService) {
        this.tilskuddFartoyFactory = tilskuddFartoyFactory;
        this.caseQueryService = caseQueryService;

        contextUser = contextUserService.getContextUserForClass(TilskuddFartoyResource.class);
    }

    public void getTilskuddFartoyForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(tilskuddFartoyFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
