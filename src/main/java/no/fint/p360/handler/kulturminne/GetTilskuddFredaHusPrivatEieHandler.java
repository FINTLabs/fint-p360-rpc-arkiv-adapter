package no.fint.p360.handler.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.exception.CaseNotFound;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.NotTilskuddFredaHusPrivatEieException;
import no.fint.p360.data.kulturminne.TilskuddFredaBygningPrivatEieService;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.CaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.Set;

@Service
public class GetTilskuddFredaHusPrivatEieHandler implements Handler {

    @Autowired
    private TilskuddFredaBygningPrivatEieService tilskuddFredaBygningPrivatEieService;

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
            tilskuddFredaBygningPrivatEieService.getTilskuddFredaBygningPrivatEieForQuery(query, response);
        } catch (CaseNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (NotTilskuddFredaHusPrivatEieException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_A_TILSKUDDFREDAHUSPRIVATEIE_SAK");
            response.setMessage(e.getMessage());
        } catch (GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        } catch (WebClientResponseException e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setStatusCode(String.valueOf(e.getStatusCode()));
            response.setMessage(e.getResponseBodyAsString());
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.GET_TILSKUDDFREDABYGNINGPRIVATEIE.name());
    }
}
