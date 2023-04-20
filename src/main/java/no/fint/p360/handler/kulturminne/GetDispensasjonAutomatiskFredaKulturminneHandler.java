package no.fint.p360.handler.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.kulturminne.DispensasjonAutomatiskFredaKulturminneService;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.CaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class GetDispensasjonAutomatiskFredaKulturminneHandler implements Handler {

    @Autowired
    private DispensasjonAutomatiskFredaKulturminneService dispensasjonAutomatiskFredaKulturminneService;

    @Autowired
    private CaseQueryService caseQueryService;

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();

        if (!caseQueryService.isValidQuery(query)) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("BAD_REQUEST");
            response.setMessage("Invalid query: " + query);
            return;
        }

        try {
            dispensasjonAutomatiskFredaKulturminneService.getDispensasjonAutomatiskFredaKulturminneForQuery(query, response);
        } catch (CaseNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (NotDispensasjonAutomatiskFredaKulturminneException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_A_DISPENSASJON_AUTOMATISK_FREDA_KULTURMINNE_SAK");
            response.setMessage(e.getMessage());
        } catch (GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE.name());
    }
}
