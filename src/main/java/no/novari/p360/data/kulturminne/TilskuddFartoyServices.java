package no.novari.p360.data.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class TilskuddFartoyServices {

    private final TilskuddFartoyFactory tilskuddFartoyFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public TilskuddFartoyServices(TilskuddFartoyFactory tilskuddFartoyFactory, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.tilskuddFartoyFactory = tilskuddFartoyFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getFilterSetForCaseType(TilskuddFartoyResource.class);
    }

    public void getTilskuddFartoyForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(filterSet, query).map(tilskuddFartoyFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
