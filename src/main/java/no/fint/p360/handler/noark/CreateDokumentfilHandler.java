package no.fint.p360.handler.noark;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.Problem;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.noark.NoarkActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fint.p360.handler.Handler;
import no.fint.p360.repository.InternalRepository;
import no.fint.p360.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CreateDokumentfilHandler implements Handler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private InternalRepository internalRepository;

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getOperation() != Operation.CREATE || StringUtils.isNoneBlank(response.getQuery()) || response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("ILLEGAL_REQUEST");
            response.setMessage("Illegal request");
            return;
        }
        DokumentfilResource dokumentfilResource = objectMapper.convertValue(response.getData().get(0), DokumentfilResource.class);

        List<Problem> problems = validationService.getProblems(dokumentfilResource);
        if (!problems.isEmpty()) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Payload fails validation!");
            response.setProblems(problems);
            return;
        }

        log.info("Format: {}, data: {}...", dokumentfilResource.getFormat(), StringUtils.substring(dokumentfilResource.getData(), 0, 25));

        response.getData().clear();
        try {
            internalRepository.putFile(response, dokumentfilResource);
            response.addData(dokumentfilResource);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (IOException e) {
            response.setMessage(e.getMessage());
            response.setResponseStatus(ResponseStatus.ERROR);
        }

    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(NoarkActions.UPDATE_DOKUMENTFIL.name());
    }

    @Override
    public boolean health() {
        return internalRepository.health();
    }
}
