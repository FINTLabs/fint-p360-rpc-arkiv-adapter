package no.fint.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.data.exception.KorrespondansepartNotFound;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartService;
import no.fint.p360.handler.Handler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

import static no.fint.p360.data.utilities.QueryUtils.getQueryParams;

@Slf4j
@Service
public class GetKorrespondansepartHandler implements Handler {
    @Autowired
    private KorrespondansepartService korrespondansepartService;

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();
        try {
            if (StringUtils.startsWithIgnoreCase(query, "systemid/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartBySystemId(
                                Integer.parseInt(
                                        StringUtils.removeStartIgnoreCase(query, "systemid/"))));
            } else if (StringUtils.startsWith(query, "organisasjonsnummer/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartByOrganisasjonsnummer(
                                StringUtils.removeStart(query, "organisasjonsnummer/")));
            } else if (StringUtils.startsWith(query, "fodselsnummer/")) {
                response.addData(
                        korrespondansepartService.getKorrespondansepartByFodselsnummer(
                                StringUtils.removeStart(query, "fodselsnummer/")
                        )
                );
            } else if (StringUtils.startsWith(query, "?")) {
                korrespondansepartService.search(getQueryParams(query)).forEach(response::addData);
            } else {
                throw new IllegalArgumentException("Invalid query: " + query);
            }
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (KorrespondansepartNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        }

    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(ArkivActions.GET_KORRESPONDANSEPART.name());
    }

    @Override
    public boolean health() {
        return korrespondansepartService.health();
    }
}
