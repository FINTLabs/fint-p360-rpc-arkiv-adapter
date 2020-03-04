package no.fint.p360.handler.kulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.Problem;
import no.fint.event.model.ResponseStatus;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.kulturminne.TilskuddFartoyDefaults;
import no.fint.p360.data.kulturminne.TilskuddfartoyService;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UpdateTilskuddFartoyHandler implements Handler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TilskuddFartoyDefaults tilskuddFartoyDefaults;

    @Autowired
    private TilskuddfartoyService tilskuddfartoyService;

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();

        TilskuddFartoyResource tilskuddFartoyResource = objectMapper.convertValue(response.getData().get(0), TilskuddFartoyResource.class);

        if (operation == Operation.CREATE) {
            createCase(response, tilskuddFartoyResource);
        } else if (operation == Operation.UPDATE) {
            updateCase(response, response.getQuery(), tilskuddFartoyResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }

    }

    private void updateCase(Event<FintLinks> response, String query, TilskuddFartoyResource tilskuddFartoyResource) {
        if (!StringUtils.startsWithIgnoreCase(query, "mappeid/")) {
            throw new IllegalArgumentException("Invalid query: " + query);
        }
        if (tilskuddFartoyResource.getJournalpost() == null ||
                tilskuddFartoyResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }
        tilskuddFartoyDefaults.applyDefaultsForUpdate(tilskuddFartoyResource);
        log.info("Complete document for update: {}", tilskuddFartoyResource);
        List<Problem> problems = validationService.getProblems(tilskuddFartoyResource.getJournalpost());
        if (!problems.isEmpty()) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Payload fails validation!");
            response.setProblems(problems);
            log.info("Validation problems!\n{}\n{}\n", tilskuddFartoyResource, problems);
            return;
        }
        try {
            String caseNumber = StringUtils.removeStartIgnoreCase(query, "mappeid/");
            TilskuddFartoyResource result = tilskuddfartoyService.updateTilskuddFartoyCase(caseNumber, tilskuddFartoyResource);
            response.setData(ImmutableList.of(result));
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (GetTilskuddFartoyNotFoundException | GetTilskuddFartoyException | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, TilskuddFartoyResource tilskuddFartoyResource) {
        try {
            tilskuddFartoyDefaults.applyDefaultsForCreation(tilskuddFartoyResource);
            log.info("Complete document for creation: {}", tilskuddFartoyResource);
            List<Problem> problems = validationService.getProblems(tilskuddFartoyResource);
            if (!problems.isEmpty()) {
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage("Payload fails validation!");
                response.setProblems(problems);
                log.info("Validation problems!\n{}\n{}\n", tilskuddFartoyResource, problems);
                return;
            }
            TilskuddFartoyResource tilskuddFartoy = tilskuddfartoyService.createTilskuddFartoyCase(tilskuddFartoyResource);
            response.setData(ImmutableList.of(tilskuddFartoy));
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CreateCaseException | GetTilskuddFartoyNotFoundException | GetTilskuddFartoyException | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.UPDATE_TILSKUDDFARTOY.name());
    }
}
