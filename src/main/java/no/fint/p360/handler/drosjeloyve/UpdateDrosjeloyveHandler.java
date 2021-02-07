package no.fint.p360.handler.drosjeloyve;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.samferdsel.SamferdselActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.samferdsel.DrosjeloyveResource;
import no.fint.p360.data.drosjeloyve.DrosjeloyveFactory;
import no.fint.p360.data.drosjeloyve.DrosjeloyveService;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.p360.CaseService;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.utilities.QueryUtils;
import no.fint.p360.handler.Handler;
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.P360CaseDefaultsService;
import no.fint.p360.service.ValidationService;
import no.p360.model.CaseService.ArchiveCode;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class UpdateDrosjeloyveHandler implements Handler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private DrosjeloyveService drosjeloyveService;

    @Autowired
    private DrosjeloyveFactory drosjeloyveFactory;

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

    @Value("${fint.case.defaults.drosjeloyve.primarklassifikasjon}")
    String primarklassifikasjon;

    @Value("${fint.case.defaults.drosjeloyve.kKodeFagklasse}")
    String kKodeFagklasse;

    @Value("${fint.case.defaults.drosjeloyve.kKodeTilleggskode}")
    String kKodeTilleggskode;

    @Override
    public void accept(Event<FintLinks> response) {
        if (response.getData().size() != 1) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid request");
            return;
        }

        Operation operation = response.getOperation();

        DrosjeloyveResource drosjeloyveResource = objectMapper.convertValue(response.getData().get(0), DrosjeloyveResource.class);

        if (operation == Operation.CREATE) {
            caseDefaultsService.applyDefaultsForCreation(caseDefaults.getDrosjeloyve(), drosjeloyveResource);
            log.info("Case: {}", drosjeloyveResource);
            if (!validationService.validate(response, drosjeloyveResource)) {
                return;
            }
            createCase(response, drosjeloyveResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseDefaults.getDrosjeloyve(), drosjeloyveResource);
            if (!validationService.validate(response, drosjeloyveResource.getJournalpost())) {
                return;
            }
            updateCase(response, response.getQuery(), drosjeloyveResource);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    private void updateCase(Event<FintLinks> response, String query, DrosjeloyveResource drosjeloyveResource) {
        if (!caseQueryService.isValidQuery(query)) {
            response.setStatusCode("BAD_REQUEST");
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid query: " + query);
            return;
        }
        if (drosjeloyveResource.getJournalpost() == null ||
                drosjeloyveResource.getJournalpost().isEmpty()) {
            throw new IllegalArgumentException("Update must contain at least one Journalpost");
        }
        log.info("Complete document for update: {}", drosjeloyveResource);
        try {
            Case theCase = caseQueryService.query(query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            createDocumentsForCase(drosjeloyveResource, caseNumber);
            drosjeloyveService.getDrosjeloyveForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, DrosjeloyveResource drosjeloyveResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseDefaults.getDrosjeloyve(),
                                    drosjeloyveFactory.convertToCreateCase(
                                            drosjeloyveResource));

            // TODO: Move to Asgeir
            ArchiveCode primaryArchiveCode = new ArchiveCode();
            primaryArchiveCode.setSort(1);
            primaryArchiveCode.setArchiveType(primarklassifikasjon);
            primaryArchiveCode.setArchiveCode(drosjeloyveResource.getOrganisasjonsnummer());
            primaryArchiveCode.setIsManualText(true);

            ArchiveCode secondaryArchiveCode = new ArchiveCode();
            secondaryArchiveCode.setIsManualText(false);
            secondaryArchiveCode.setSort(2);
            secondaryArchiveCode.setArchiveType("FAGKLASSE PRINSIPP");
            secondaryArchiveCode.setArchiveCode(kKodeFagklasse);

            ArchiveCode additinalCode = new ArchiveCode();
            additinalCode.setIsManualText(false);
            additinalCode.setArchiveType("TILLEGGSKODE PRINSIPP");
            additinalCode.setSort(3);
            additinalCode.setArchiveCode(kKodeTilleggskode);

            createCaseArgs.setArchiveCodes(Arrays.asList(
                    primaryArchiveCode,
                    secondaryArchiveCode,
                    additinalCode
                    )
            );

            String caseNumber = caseService.createCase(createCaseArgs);
            createDocumentsForCase(drosjeloyveResource, caseNumber);
            drosjeloyveService.getDrosjeloyveForQuery("mappeid/" + caseNumber, response);
        } catch (CreateCaseException | CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createDocumentsForCase(DrosjeloyveResource drosjeloyveResource, String caseNumber) {
        if (drosjeloyveResource.getJournalpost() != null) {
            drosjeloyveResource
                    .getJournalpost()
                    .stream()
                    .map(it -> drosjeloyveFactory.convertToCreateDocument(it, caseNumber))
                    .forEach(documentService::createDocument);
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(SamferdselActions.UPDATE_DROSJELOYVE.name());
    }
}
