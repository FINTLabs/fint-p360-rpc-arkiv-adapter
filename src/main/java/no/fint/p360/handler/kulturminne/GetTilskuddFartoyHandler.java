package no.fint.p360.handler.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.kulturminne.TilskuddfartoyService;
import no.fint.p360.handler.Handler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

import static no.fint.p360.data.utilities.QueryUtils.getQueryParams;

@Service
@Slf4j
public class GetTilskuddFartoyHandler implements Handler {
    @Autowired
    private TilskuddfartoyService tilskuddfartoyService;

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();
        try {
            response.getData().clear();
            if (StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
                response.addData(tilskuddfartoyService.getTilskuddFartoyCaseByCaseNumber(StringUtils.removeStartIgnoreCase(query, "mappeid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(tilskuddfartoyService.getTilskuddFartoyCaseBySystemId(StringUtils.removeStartIgnoreCase(query, "systemid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "soknadsnummer/")) {
                response.addData(tilskuddfartoyService.getTilskuddFartoyCaseByExternalId(StringUtils.removeStartIgnoreCase(query, "soknadsnummer/")));
            } else if (StringUtils.startsWith(query, "?")) {
                tilskuddfartoyService.searchTilskuddFartoyCaseByTitle(getQueryParams(query)).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_A_TILSKUDDFARTOY_SAK");
            response.setMessage(e.getMessage());
        } catch (GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }

    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.GET_TILSKUDDFARTOY.name());
    }

}
