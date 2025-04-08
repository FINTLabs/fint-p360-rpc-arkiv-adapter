package no.novari.p360.data.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class TilskuddFredaBygningPrivatEieService {

    private final TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public TilskuddFredaBygningPrivatEieService(TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.tilskuddFredaBygningPrivatEieFactory = tilskuddFredaBygningPrivatEieFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getFilterSetForCaseType(TilskuddFredaBygningPrivatEieResource.class);
    }

    public void getTilskuddFredaBygningPrivatEieForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(filterSet, query).map(tilskuddFredaBygningPrivatEieFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
