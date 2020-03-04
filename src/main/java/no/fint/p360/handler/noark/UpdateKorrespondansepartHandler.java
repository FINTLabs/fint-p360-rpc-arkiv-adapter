package no.fint.p360.handler.noark;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.Problem;
import no.fint.event.model.ResponseStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartResource;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartService;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UpdateKorrespondansepartHandler implements Handler {
    @Autowired
    private ValidationService validationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KorrespondansepartService korrespondansepartService;

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getOperation() != Operation.CREATE) {
            throw new IllegalArgumentException("Illegal operation: " + response.getOperation());
        }
        if (response.getData() == null || response.getData().size() != 1) {
            throw new IllegalArgumentException("Illegal request data payload.");
        }
        KorrespondansepartResource korrespondansepartResource = objectMapper.convertValue(response.getData().get(0),
                KorrespondansepartResource.class);

        List<Problem> problems = validationService.getProblems(korrespondansepartResource);
        if (!problems.isEmpty()) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Payload fails validation!");
            response.setProblems(problems);
            return;
        }

        try {
            KorrespondansepartResource result = korrespondansepartService.createKorrespondansepart(korrespondansepartResource);
            response.setData(Collections.singletonList(result));
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CreateContactException | CreateEnterpriseException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }

    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(ArkivActions.UPDATE_KORRESPONDANSEPART.name());
    }
}
