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
import no.fint.model.resource.arkiv.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.novari.p360.data.exception.*;
import no.novari.p360.data.kulturminne.DispensasjonAutomatiskFredaKulturminneFactory;
import no.novari.p360.data.kulturminne.DispensasjonAutomatiskFredaKulturminneService;
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
public class UpdateDispensasjonAutomatiskFredaKulturminneHandler implements Handler {
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final DispensasjonAutomatiskFredaKulturminneService dispensasjonAutomatiskFredaKulturminneService;
    private final DispensasjonAutomatiskFredaKulturminneFactory dispensasjonAutomatiskFredaKulturminneFactory;
    private final CaseProperties caseProperties;
    private final P360CaseDefaultsService caseDefaultsService;
    private final CaseQueryService caseQueryService;
    private final CaseService caseService;
    private final DocumentService documentService;
    private final FilterSet filterSet;

    public UpdateDispensasjonAutomatiskFredaKulturminneHandler(ObjectMapper objectMapper,
                                                               ValidationService validationService,
                                                               DispensasjonAutomatiskFredaKulturminneService dispensasjonAutomatiskFredaKulturminneService,
                                                               DispensasjonAutomatiskFredaKulturminneFactory dispensasjonAutomatiskFredaKulturminneFactory,
                                                               CaseDefaults caseDefaults,
                                                               P360CaseDefaultsService caseDefaultsService,
                                                               CaseQueryService caseQueryService,
                                                               CaseService caseService,
                                                               DocumentService documentService,
                                                               FilterSetService filterSetService) {
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.dispensasjonAutomatiskFredaKulturminneService = dispensasjonAutomatiskFredaKulturminneService;
        this.dispensasjonAutomatiskFredaKulturminneFactory = dispensasjonAutomatiskFredaKulturminneFactory;
        this.caseProperties = caseDefaults.getDispensasjonautomatiskfredakulturminne();
        this.caseDefaultsService = caseDefaultsService;
        this.caseQueryService = caseQueryService;
        this.caseService = caseService;
        this.documentService = documentService;
        this.filterSet = filterSetService.getFilterSetForCaseType(DispensasjonAutomatiskFredaKulturminneResource.class);
    }

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();

        DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne =
                objectMapper.convertValue(response.getData().get(0), DispensasjonAutomatiskFredaKulturminneResource.class);

        if (operation == Operation.CREATE) {
            caseDefaultsService.applyDefaultsForCreation(caseProperties, dispensasjonAutomatiskFredaKulturminne);
            log.debug("Case: {}", dispensasjonAutomatiskFredaKulturminne);
            if (!validationService.validate(response, dispensasjonAutomatiskFredaKulturminne)) {
                return;
            }
            createCase(response, dispensasjonAutomatiskFredaKulturminne);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseProperties, dispensasjonAutomatiskFredaKulturminne);
            log.debug("Case to update: {}", dispensasjonAutomatiskFredaKulturminne);
            if (!validationService.validate(response, dispensasjonAutomatiskFredaKulturminne.getJournalpost())) {
                return;
            }

            updateCase(response, response.getQuery(), dispensasjonAutomatiskFredaKulturminne);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private void updateCase(Event<FintLinks> response, String query,
                            DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne) {

        if (!caseQueryService.isValidQuery(query)) {
            response.setStatusCode("BAD_REQUEST");
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            return;
        }
        if (dispensasjonAutomatiskFredaKulturminne.getJournalpost() == null ||
                dispensasjonAutomatiskFredaKulturminne.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }

        try {
            Case theCase = caseQueryService.query(filterSet, query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            createDocumentsForCase(dispensasjonAutomatiskFredaKulturminne, caseNumber);
            dispensasjonAutomatiskFredaKulturminneService.getDispensasjonAutomatiskFredaKulturminneForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat |
                 NotDispensasjonAutomatiskFredaKulturminneException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseProperties,
                                    dispensasjonAutomatiskFredaKulturminneFactory.convertToCreateCase(
                                            dispensasjonAutomatiskFredaKulturminne));
            String caseNumber = caseService.createCase(filterSet, createCaseArgs);
            createDocumentsForCase(dispensasjonAutomatiskFredaKulturminne, caseNumber);
            dispensasjonAutomatiskFredaKulturminneService.getDispensasjonAutomatiskFredaKulturminneForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException |
                 IllegalCaseNumberFormat | NotDispensasjonAutomatiskFredaKulturminneException e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne, String caseNumber) {
        if (dispensasjonAutomatiskFredaKulturminne.getJournalpost() != null) {
            dispensasjonAutomatiskFredaKulturminne
                    .getJournalpost()
                    .stream()
                    .map(it -> dispensasjonAutomatiskFredaKulturminneFactory.convertToCreateDocument(it, caseNumber))
                    .forEach(it -> documentService.createDocument(filterSet, it));
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE.name());
    }
}
