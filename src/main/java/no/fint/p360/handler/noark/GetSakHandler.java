package no.fint.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.noark.sak.SakService;
import no.fint.p360.handler.Handler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

import static no.fint.p360.data.utilities.QueryUtils.getQueryParams;

@Slf4j
@Service
public class GetSakHandler implements Handler {
    @Autowired
    private SakService sakService;

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();
        try {
            response.getData().clear();
            if (StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
                response.addData(sakService.getSakByCaseNumber(StringUtils.removeStartIgnoreCase(query, "mappeid/")));
            } else if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(sakService.getSakBySystemId(StringUtils.removeStartIgnoreCase(query, "systemid/")));
            } else if (StringUtils.startsWith(query, "?")) {
                sakService.searchSakByTitle(getQueryParams(query)).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException e) {
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
        return Collections.singleton(ArkivActions.GET_SAK.name());
    }

    @Override
    public boolean health() {
        return sakService.health();
    }
}

