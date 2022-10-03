package no.fint.p360.data.samferdsel;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.samferdsel.SoknadDrosjeloyveResource;
import no.fint.p360.model.FilterSet;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class SoknadDrosjeloyveService {

    private final SoknadDrosjeloyveFactory soknadDrosjeloyveFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public SoknadDrosjeloyveService(SoknadDrosjeloyveFactory soknadDrosjeloyveFactory, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.soknadDrosjeloyveFactory = soknadDrosjeloyveFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getFilterSetForCaseType(SoknadDrosjeloyveResource.class);
    }

    public void getDrosjeloyveForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(filterSet, query).map(soknadDrosjeloyveFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
