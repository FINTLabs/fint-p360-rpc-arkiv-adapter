package no.novari.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.noark.NoarkActions;
import no.fint.model.resource.FintLinks;
import no.novari.p360.data.exception.EnterpriseNotFound;
import no.novari.p360.data.noark.administrativenhet.AdministrativEnhetService;
import no.novari.p360.handler.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
public class GetAdministrativEnhetHandler implements Handler {
    @Autowired
    private AdministrativEnhetService administrativEnhetService;

    @Override
    public void accept(Event<FintLinks> response) {
        try {
            administrativEnhetService.getAdministrativEnhet().forEach(response::addData);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (EnterpriseNotFound e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(e.getMessage());
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(NoarkActions.GET_ALL_ADMINISTRATIVENHET.name());
    }

}
