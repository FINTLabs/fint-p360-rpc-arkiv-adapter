package no.novari.p360.data.noark.sak;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class SakService {

    private final SakFactory sakFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public SakService(SakFactory sakFactory, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.sakFactory = sakFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getFilterSetForCaseType(SakResource.class);
    }

    public void getCasesForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(filterSet, query).map(sakFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
