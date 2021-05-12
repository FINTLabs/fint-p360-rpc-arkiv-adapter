package no.fint.p360.data.samferdsel;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.samferdsel.SoknadDrosjeloyveResource;
import no.fint.p360.model.ContextUser;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.ContextUserService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Slf4j
@Service
public class SoknadDrosjeloyveService {

    private final SoknadDrosjeloyveFactory soknadDrosjeloyveFactory;
    private final CaseQueryService caseQueryService;
    private final ContextUser contextUser;

    public SoknadDrosjeloyveService(SoknadDrosjeloyveFactory soknadDrosjeloyveFactory, CaseQueryService caseQueryService, ContextUserService contextUserService) {
        this.soknadDrosjeloyveFactory = soknadDrosjeloyveFactory;
        this.caseQueryService = caseQueryService;

        contextUser = contextUserService.getContextUserForClass(SoknadDrosjeloyveResource.class);
    }

    public void getDrosjeloyveForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(soknadDrosjeloyveFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
