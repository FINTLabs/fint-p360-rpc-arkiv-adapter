package no.novari.p360.handler.kulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.novari.fint.arkiv.CaseDefaults;
import no.novari.fint.arkiv.CaseProperties;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource;
import no.novari.p360.data.exception.*;
import no.novari.p360.data.kulturminne.TilskuddFartoyFactory;
import no.novari.p360.data.kulturminne.TilskuddFartoyServices;
import no.novari.p360.data.p360.CaseService;
import no.novari.p360.data.p360.DocumentService;
import no.novari.p360.data.utilities.QueryUtils;
import no.novari.p360.handler.Handler;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import no.novari.p360.service.P360CaseDefaultsService;
import no.novari.p360.service.ValidationService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class UpdateTilskuddFartoyHandler implements Handler {
    public UpdateTilskuddFartoyHandler(ObjectMapper objectMapper,
                                       ValidationService validationService,
                                       TilskuddFartoyServices tilskuddfartoyService,
                                       TilskuddFartoyFactory tilskuddFartoyFactory,
                                       CaseDefaults caseDefaults,
                                       P360CaseDefaultsService caseDefaultsService,
                                       CaseQueryService caseQueryService,
                                       CaseService caseService,
                                       DocumentService documentService,
                                       FilterSetService filterSetService) {
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.tilskuddfartoyService = tilskuddfartoyService;
        this.tilskuddFartoyFactory = tilskuddFartoyFactory;
        this.caseProperties = caseDefaults.getTilskuddfartoy();
        this.caseDefaultsService = caseDefaultsService;
        this.caseQueryService = caseQueryService;
        this.caseService = caseService;
        this.documentService = documentService;
        this.filterSet = filterSetService.getFilterSetForCaseType(TilskuddFartoyResource.class);
    }

    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final TilskuddFartoyServices tilskuddfartoyService;
    private final TilskuddFartoyFactory tilskuddFartoyFactory;
    private final CaseProperties caseProperties;
    private final P360CaseDefaultsService caseDefaultsService;
    private final CaseQueryService caseQueryService;
    private final CaseService caseService;
    private final DocumentService documentService;
    private final FilterSet filterSet;

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
            caseDefaultsService.applyDefaultsForCreation(caseProperties, tilskuddFartoyResource);
            log.debug("Case: {}", tilskuddFartoyResource);
            if (!validationService.validate(response, tilskuddFartoyResource)) {
                return;
            }
            createCase(response, tilskuddFartoyResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseProperties, tilskuddFartoyResource);
            if (!validationService.validate(response, tilskuddFartoyResource.getJournalpost())) {
                return;
            }
            updateCase(response, response.getQuery(), tilskuddFartoyResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private void updateCase(Event<FintLinks> response, String query, TilskuddFartoyResource tilskuddFartoyResource) {
        if (!caseQueryService.isValidQuery(query)) {
            response.setStatusCode("BAD_REQUEST");
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            return;
        }
        if (tilskuddFartoyResource.getJournalpost() == null ||
                tilskuddFartoyResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }

        try {
            Case theCase = caseQueryService.query(filterSet, query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            log.info("About to update case with the caseNumber: {}", caseNumber);

            createDocumentsForCase(tilskuddFartoyResource, caseNumber);
            tilskuddfartoyService.getTilskuddFartoyForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, TilskuddFartoyResource tilskuddFartoyResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseProperties,
                                    tilskuddFartoyFactory.convertToCreateCase(
                                            tilskuddFartoyResource));
            String caseNumber = caseService.createCase(filterSet, createCaseArgs);
            createDocumentsForCase(tilskuddFartoyResource, caseNumber);
            tilskuddfartoyService.getTilskuddFartoyForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(TilskuddFartoyResource tilskuddFartoyResource, String caseNumber) {
        if (tilskuddFartoyResource.getJournalpost() != null) {
            tilskuddFartoyResource
                    .getJournalpost()
                    .stream()
                    .map(it -> tilskuddFartoyFactory.convertToCreateDocument(it, caseNumber))
                    .forEach(it -> documentService.createDocument(filterSet, it));
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.UPDATE_TILSKUDDFARTOY.name());
    }
}
