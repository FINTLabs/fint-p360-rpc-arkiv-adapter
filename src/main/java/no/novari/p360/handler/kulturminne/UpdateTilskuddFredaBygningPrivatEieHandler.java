package no.novari.p360.handler.kulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.CaseProperties;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
import no.novari.p360.data.exception.*;
import no.novari.p360.data.kulturminne.TilskuddFredaBygningPrivatEieFactory;
import no.novari.p360.data.kulturminne.TilskuddFredaBygningPrivatEieService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class UpdateTilskuddFredaBygningPrivatEieHandler implements Handler {
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final TilskuddFredaBygningPrivatEieService tilskuddFredaBygningPrivatEieService;
    private final TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory;
    private final CaseProperties caseProperties;
    private final P360CaseDefaultsService caseDefaultsService;
    private final CaseQueryService caseQueryService;
    private final CaseService caseService;
    private final DocumentService documentService;
    private final FilterSet filterSet;

    public UpdateTilskuddFredaBygningPrivatEieHandler(ObjectMapper objectMapper,
                                                      ValidationService validationService,
                                                      TilskuddFredaBygningPrivatEieService tilskuddFredaBygningPrivatEieService,
                                                      TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory,
                                                      CaseDefaults caseDefaults,
                                                      P360CaseDefaultsService caseDefaultsService,
                                                      CaseQueryService caseQueryService,
                                                      CaseService caseService,
                                                      DocumentService documentService,
                                                      FilterSetService filterSetService) {
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.tilskuddFredaBygningPrivatEieService = tilskuddFredaBygningPrivatEieService;
        this.tilskuddFredaBygningPrivatEieFactory = tilskuddFredaBygningPrivatEieFactory;
        this.caseProperties = caseDefaults.getTilskuddfredabygningprivateie();
        this.caseDefaultsService = caseDefaultsService;
        this.caseQueryService = caseQueryService;
        this.caseService = caseService;
        this.documentService = documentService;
        this.filterSet = filterSetService.getFilterSetForCaseType(TilskuddFredaBygningPrivatEieResource.class);
    }

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
            caseDefaultsService.applyDefaultsForCreation(caseProperties, tilskuddFredaHusPrivatEieResource);
            log.debug("Case: {}", tilskuddFredaHusPrivatEieResource);
            if (!validationService.validate(response, tilskuddFredaHusPrivatEieResource)) {
                return;
            }
            createCase(response, tilskuddFredaHusPrivatEieResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseProperties, tilskuddFredaHusPrivatEieResource);
            log.debug("Case to update: {}", tilskuddFredaHusPrivatEieResource);
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

        try {
            Case theCase = caseQueryService.query(filterSet, query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            createDocumentsForCase(tilskuddFredaHusPrivatEieResource, caseNumber);
            tilskuddFredaBygningPrivatEieService.getTilskuddFredaBygningPrivatEieForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddFredaHusPrivatEieException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, TilskuddFredaBygningPrivatEieResource tilskuddFredaHusPrivatEieResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseProperties,
                                    tilskuddFredaBygningPrivatEieFactory.convertToCreateCase(
                                            tilskuddFredaHusPrivatEieResource));
            String caseNumber = caseService.createCase(filterSet, createCaseArgs);
            createDocumentsForCase(tilskuddFredaHusPrivatEieResource, caseNumber);
            tilskuddFredaBygningPrivatEieService.getTilskuddFredaBygningPrivatEieForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat | NotTilskuddFredaHusPrivatEieException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(TilskuddFredaBygningPrivatEieResource tilskuddFredaHusPrivatEieResource, String caseNumber) {
        if (tilskuddFredaHusPrivatEieResource.getJournalpost() != null) {
            tilskuddFredaHusPrivatEieResource
                    .getJournalpost()
                    .stream()
                    .map(it -> tilskuddFredaBygningPrivatEieFactory.convertToCreateDocument(it, caseNumber))
                    .forEach(it -> documentService.createDocument(filterSet, it));
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.UPDATE_TILSKUDDFREDABYGNINGPRIVATEIE.name());
    }
}
