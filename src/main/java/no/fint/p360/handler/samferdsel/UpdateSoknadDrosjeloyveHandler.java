package no.fint.p360.handler.samferdsel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.CaseProperties;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.samferdsel.SamferdselActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.samferdsel.SoknadDrosjeloyveResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.p360.CaseService;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.samferdsel.SoknadDrosjeloyveFactory;
import no.fint.p360.data.samferdsel.SoknadDrosjeloyveService;
import no.fint.p360.data.utilities.QueryUtils;
import no.fint.p360.handler.Handler;
import no.fint.p360.model.FilterSet;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.FilterSetService;
import no.fint.p360.service.P360CaseDefaultsService;
import no.fint.p360.service.ValidationService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class UpdateSoknadDrosjeloyveHandler implements Handler {
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;
    private final SoknadDrosjeloyveService soknadDrosjeloyveService;
    private final SoknadDrosjeloyveFactory soknadDrosjeloyveFactory;
    private final CaseProperties caseProperties;
    private final P360CaseDefaultsService caseDefaultsService;
    private final CaseQueryService caseQueryService;
    private final CaseService caseService;
    private final DocumentService documentService;
    private final FilterSet filterSet;

    public UpdateSoknadDrosjeloyveHandler(ObjectMapper objectMapper,
                                          ValidationService validationService,
                                          SoknadDrosjeloyveService soknadDrosjeloyveService,
                                          SoknadDrosjeloyveFactory soknadDrosjeloyveFactory,
                                          CaseDefaults caseDefaults,
                                          P360CaseDefaultsService caseDefaultsService,
                                          CaseQueryService caseQueryService,
                                          CaseService caseService,
                                          DocumentService documentService,
                                          FilterSetService filterSetService) {
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.soknadDrosjeloyveService = soknadDrosjeloyveService;
        this.soknadDrosjeloyveFactory = soknadDrosjeloyveFactory;
        this.caseProperties = caseDefaults.getSoknaddrosjeloyve();
        this.caseDefaultsService = caseDefaultsService;
        this.caseQueryService = caseQueryService;
        this.caseService = caseService;
        this.documentService = documentService;
        this.filterSet = filterSetService.getFilterSetForCaseType(SoknadDrosjeloyveResource.class);
    }

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();

        SoknadDrosjeloyveResource SoknadDrosjeloyveResource = objectMapper.convertValue(response.getData().get(0), SoknadDrosjeloyveResource.class);

        if (operation == Operation.CREATE) {
            caseDefaultsService.applyDefaultsForCreation(caseProperties, SoknadDrosjeloyveResource);
            log.info("Case: {}", SoknadDrosjeloyveResource);
            if (!validationService.validate(response, SoknadDrosjeloyveResource)) {
                return;
            }
            createCase(response, SoknadDrosjeloyveResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseProperties, SoknadDrosjeloyveResource);
            if (!validationService.validate(response, SoknadDrosjeloyveResource.getJournalpost())) {
                return;
            }
            updateCase(response, response.getQuery(), SoknadDrosjeloyveResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private void updateCase(Event<FintLinks> response, String query, SoknadDrosjeloyveResource SoknadDrosjeloyveResource) {
        if (!caseQueryService.isValidQuery(query)) {
            response.setStatusCode("BAD_REQUEST");
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            return;
        }
        if (SoknadDrosjeloyveResource.getJournalpost() == null ||
                SoknadDrosjeloyveResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }
        log.info("Complete document for update: {}", SoknadDrosjeloyveResource);
        try {
            Case theCase = caseQueryService.query(filterSet, query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            createDocumentsForCase(SoknadDrosjeloyveResource, caseNumber);
            soknadDrosjeloyveService.getDrosjeloyveForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, SoknadDrosjeloyveResource soknadDrosjeloyveResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseProperties,
                                    soknadDrosjeloyveFactory.convertToCreateCase(
                                            soknadDrosjeloyveResource));

            String caseNumber = caseService.createCase(filterSet, createCaseArgs);
            createDocumentsForCase(soknadDrosjeloyveResource, caseNumber);
            soknadDrosjeloyveService.getDrosjeloyveForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(SoknadDrosjeloyveResource SoknadDrosjeloyveResource, String caseNumber) {
        if (SoknadDrosjeloyveResource.getJournalpost() != null) {
            SoknadDrosjeloyveResource
                    .getJournalpost()
                    .stream()
                    .map(it -> soknadDrosjeloyveFactory.convertToCreateDocument(it, caseNumber))
                    .forEach(it -> documentService.createDocument(filterSet, it));
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(SamferdselActions.UPDATE_SOKNADDROSJELOYVE.name());
    }
}
