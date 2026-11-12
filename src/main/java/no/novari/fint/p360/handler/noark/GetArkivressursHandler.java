package no.novari.fint.p360.handler.noark;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.novari.fint.model.arkiv.noark.NoarkActions;
import no.novari.fint.model.resource.FintLinks;
import no.novari.fint.p360.data.exception.ContactPersonNotFound;
import no.novari.fint.p360.data.noark.arkivressurs.ArkivressursService;
import no.novari.fint.p360.handler.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "fint.arkiv.arkivressurs", havingValue = "true")
public class GetArkivressursHandler implements Handler {
    @Autowired
    private ArkivressursService arkivressursService;

    @Override
    public void accept(Event<FintLinks> response) {
        try {
            arkivressursService.getArkivressurs().forEach(response::addData);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (ContactPersonNotFound e) {
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(e.getMessage());
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(NoarkActions.GET_ALL_ARKIVRESSURS.name());
    }
}
