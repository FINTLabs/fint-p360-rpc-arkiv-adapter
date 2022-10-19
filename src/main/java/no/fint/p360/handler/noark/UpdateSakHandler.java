package no.fint.p360.handler.noark;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseProperties;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.noark.NoarkActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.noark.sak.SakFactory;
import no.fint.p360.data.noark.sak.SakService;
import no.fint.p360.data.p360.CaseService;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.utilities.QueryUtils;
import no.fint.p360.handler.Handler;
import no.fint.p360.model.FilterSet;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.FilterSetService;
import no.fint.p360.service.P360CaseDefaultsService;
import no.fint.p360.service.ValidationService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class UpdateSakHandler implements Handler {
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final SakService sakService;
    private final SakFactory sakFactory;
    private final CaseProperties caseProperties;
    private final P360CaseDefaultsService caseDefaultsService;
    private final CaseQueryService caseQueryService;
    private final CaseService caseService;
    private final DocumentService documentService;
    private final FilterSet filterSet;
    public UpdateSakHandler(ObjectMapper objectMapper,
                            ValidationService validationService,
                            SakService sakService,
                            SakFactory sakFactory,
                            P360CaseDefaultsService caseDefaultsService,
                            CaseQueryService caseQueryService,
                            CaseService caseService,
                            DocumentService documentService,
                            FilterSetService filterSetService) {
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.sakService = sakService;
        this.sakFactory = sakFactory;
        this.caseProperties = new CaseProperties();
        this.caseDefaultsService = caseDefaultsService;
        this.caseQueryService = caseQueryService;
        this.caseService = caseService;
        this.documentService = documentService;
        this.filterSet = filterSetService.getFilterSetForCaseType(SakResource.class);
    }

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();
        SakResource sakResource = objectMapper.convertValue(response.getData().get(0), SakResource.class);

        if (operation == Operation.CREATE) {
            caseDefaultsService.applyDefaultsForCreation(caseProperties, sakResource);
            log.info("Case: {}", sakResource);
            if (!validationService.validate(response, sakResource)) {
                return;
            }
            createCase(response, sakResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseProperties, sakResource);
            if (!validationService.validate(response, sakResource.getJournalpost())) {
                return;
            }
            updateCase(response, response.getQuery(), sakResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private void updateCase(Event<FintLinks> response, String query, SakResource sakResource) {
        if (!caseQueryService.isValidQuery(query)) {
            response.setStatusCode("BAD_REQUEST");
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            return;
        }
        if (sakResource.getJournalpost() == null ||
                sakResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }

        try {
            Case theCase = caseQueryService.query(filterSet, query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            log.info("About to update case with the caseNumber: {}", caseNumber);

            createDocumentsForCase(sakResource, caseNumber);
            sakService.getCasesForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat |
                 NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, SakResource sakResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseProperties,
                                    sakFactory.convertToCreateCase(
                                            sakResource));
            String caseNumber = caseService.createCase(filterSet, createCaseArgs);
            createDocumentsForCase(sakResource, caseNumber);
            sakService.getCasesForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException |
                 IllegalCaseNumberFormat | NotTilskuddfartoyException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(SakResource sakResource, String caseNumber) {
        if (sakResource.getJournalpost() != null) {
            sakResource
                    .getJournalpost()
                    .stream()
                    .map(it -> sakFactory.convertToCreateDocument(it, caseNumber))
                    .forEach(it -> documentService.createDocument(filterSet, it));
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(NoarkActions.UPDATE_SAK.name());
    }
}
