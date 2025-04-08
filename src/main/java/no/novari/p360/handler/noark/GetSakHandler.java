package no.novari.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.noark.NoarkActions;
import no.fint.model.resource.FintLinks;
import no.novari.p360.data.exception.CaseNotFound;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.data.exception.IllegalCaseNumberFormat;
import no.novari.p360.data.noark.sak.SakFactory;
import no.novari.p360.handler.Handler;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

@Slf4j
@Service
public class GetSakHandler implements Handler {
    private final SakFactory sakFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public GetSakHandler(SakFactory sakFactory, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.sakFactory = sakFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getDefaultFilterSet();
    }

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();
        if (!caseQueryService.isValidQuery(query)) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("BAD_REQUEST");
            response.setMessage("Invalid query: " + query);
            return;
        }
        response.setData(new LinkedList<>());
        try {
            caseQueryService.query(filterSet, query).map(sakFactory::toFintResource).forEach(response::addData);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CaseNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }

    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(NoarkActions.GET_SAK.name());
    }

}

