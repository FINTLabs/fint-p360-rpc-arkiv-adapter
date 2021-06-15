package no.fint.p360.data.samferdsel;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.p360.service.CaseQueryService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class SoknadDrosjeloyveService {

    private final SoknadDrosjeloyveFactory soknadDrosjeloyveFactory;
    private final CaseQueryService caseQueryService;

    public SoknadDrosjeloyveService(SoknadDrosjeloyveFactory soknadDrosjeloyveFactory, CaseQueryService caseQueryService) {
        this.soknadDrosjeloyveFactory = soknadDrosjeloyveFactory;
        this.caseQueryService = caseQueryService;
    }

    public void getDrosjeloyveForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(soknadDrosjeloyveFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
