package no.fint.p360.handler.kulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.kulturminne.TilskuddFredaBygningPrivatEieFactory;
import no.fint.p360.data.kulturminne.TilskuddFredaHusPrivatEieService;
import no.fint.p360.data.p360.CaseService;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.utilities.QueryUtils;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.P360CaseDefaultsService;
import no.fint.p360.service.ValidationService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class UpdateTilskuddFredaHusPrivatEieHandler implements Handler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TilskuddFredaHusPrivatEieService tilskuddFredaHusPrivatEieService;

    @Autowired
    private TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory;

    @Autowired
    private CaseDefaults caseDefaults;

    @Autowired
    private P360CaseDefaultsService caseDefaultsService;

    @Autowired
    private CaseQueryService caseQueryService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private DocumentService documentService;

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();

        TilskuddFredaBygningPrivatEieResource tilskuddFredaHusPrivatEieResource = objectMapper.convertValue(response.getData().get(0), TilskuddFredaBygningPrivatEieResource.class);

        if (operation == Operation.CREATE) {
            caseDefaultsService.applyDefaultsForCreation(caseDefaults.getTilskuddfredahusprivateie(), tilskuddFredaHusPrivatEieResource);
            log.info("Case: {}", tilskuddFredaHusPrivatEieResource);
            if (!validationService.validate(response, tilskuddFredaHusPrivatEieResource)) {
                return;
            }
            createCase(response, tilskuddFredaHusPrivatEieResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseDefaults.getTilskuddfredahusprivateie(), tilskuddFredaHusPrivatEieResource);
            if (!validationService.validate(response, tilskuddFredaHusPrivatEieResource.getJournalpost())) {
                return;
            }
            updateCase(response, response.getQuery(), tilskuddFredaHusPrivatEieResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private void updateCase(Event<FintLinks> response, String query, TilskuddFredaBygningPrivatEieResource tilskuddFredaHusPrivatEieResource) {
        if (!caseQueryService.isValidQuery(query)) {
            response.setStatusCode("BAD_REQUEST");
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            return;
        }
        if (tilskuddFredaHusPrivatEieResource.getJournalpost() == null ||
                tilskuddFredaHusPrivatEieResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }
        log.info("Complete document for update: {}", tilskuddFredaHusPrivatEieResource);
        try {
            Case theCase = caseQueryService.query(query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            createDocumentsForCase(tilskuddFredaHusPrivatEieResource, caseNumber);
            tilskuddFredaHusPrivatEieService.getTilskuddFredaHusPrivatEieForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddFredaHusPrivatEieException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, TilskuddFredaBygningPrivatEieResource tilskuddFredaHusPrivatEieResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseDefaults.getTilskuddfredahusprivateie(),
                                    tilskuddFredaBygningPrivatEieFactory.convertToCreateCase(
                                            tilskuddFredaHusPrivatEieResource));
            String caseNumber = caseService.createCase(createCaseArgs);
            createDocumentsForCase(tilskuddFredaHusPrivatEieResource, caseNumber);
            tilskuddFredaHusPrivatEieService.getTilskuddFredaHusPrivatEieForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddFredaHusPrivatEieException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(TilskuddFredaBygningPrivatEieResource tilskuddFredaHusPrivatEieResource, String caseNumber) {
        tilskuddFredaHusPrivatEieResource
                .getJournalpost()
                .stream()
                .map(it -> tilskuddFredaBygningPrivatEieFactory.convertToCreateDocument(it, caseNumber))
                .forEach(documentService::createDocument);
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.UPDATE_TILSKUDDFREDABYGNINGPRIVATEIE.name());
    }
}
