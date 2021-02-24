package no.fint.p360.handler.samferdsel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
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
import no.fint.p360.service.CaseQueryService;
import no.fint.p360.service.P360CaseDefaultsService;
import no.fint.p360.service.ValidationService;
import no.p360.model.CaseService.ArchiveCode;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
@ConditionalOnProperty("fint.case.handlers.soknaddrosjeloyve")
public class UpdateSoknadDrosjeloyveHandler implements Handler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private SoknadDrosjeloyveService soknadDrosjeloyveService;

    @Autowired
    private SoknadDrosjeloyveFactory soknadDrosjeloyveFactory;

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

        SoknadDrosjeloyveResource SoknadDrosjeloyveResource = objectMapper.convertValue(response.getData().get(0), SoknadDrosjeloyveResource.class);

        if (operation == Operation.CREATE) {
            caseDefaultsService.applyDefaultsForCreation(caseDefaults.getSoknaddrosjeloyve(), SoknadDrosjeloyveResource);
            log.info("Case: {}", SoknadDrosjeloyveResource);
            if (!validationService.validate(response, SoknadDrosjeloyveResource)) {
                return;
            }
            createCase(response, SoknadDrosjeloyveResource);
        } else if (operation == Operation.UPDATE) {
            caseDefaultsService.applyDefaultsForUpdate(caseDefaults.getSoknaddrosjeloyve(), SoknadDrosjeloyveResource);
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
            Case theCase = caseQueryService.query(query).collect(QueryUtils.toSingleton());
            String caseNumber = theCase.getCaseNumber();
            createDocumentsForCase(SoknadDrosjeloyveResource, caseNumber);
            soknadDrosjeloyveService.getDrosjeloyveForQuery(query, response);
        } catch (CaseNotFound | CreateDocumentException | GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode(HttpStatus.BAD_REQUEST.name());
            response.setMessage(e.getMessage());
        }
    }

    private void createCase(Event<FintLinks> response, SoknadDrosjeloyveResource SoknadDrosjeloyveResource) {
        try {
            final CreateCaseArgs createCaseArgs =
                    caseDefaultsService
                            .applyDefaultsToCreateCaseParameter(
                                    caseDefaults.getSoknaddrosjeloyve(),
                                    soknadDrosjeloyveFactory.convertToCreateCase(
                                            SoknadDrosjeloyveResource));

            // TODO: Move to case-defaults 3.0.0
            ArchiveCode primaryArchiveCode = new ArchiveCode();
            primaryArchiveCode.setSort(1);
            primaryArchiveCode.setArchiveType(primarklassifikasjon);
            primaryArchiveCode.setArchiveCode(SoknadDrosjeloyveResource.getOrganisasjonsnummer());
            primaryArchiveCode.setIsManualText(true);

            ArchiveCode secondaryArchiveCode = new ArchiveCode();
            secondaryArchiveCode.setIsManualText(false);
            secondaryArchiveCode.setSort(2);
            secondaryArchiveCode.setArchiveType("FAGKLASSE PRINSIPP");
            secondaryArchiveCode.setArchiveCode(kKodeFagklasse);

            ArchiveCode additionalCode = new ArchiveCode();
            additionalCode.setIsManualText(false);
            additionalCode.setArchiveType("TILLEGGSKODE PRINSIPP");
            additionalCode.setSort(3);
            additionalCode.setArchiveCode(kKodeTilleggskode);

            createCaseArgs.setArchiveCodes(Arrays.asList(
                    primaryArchiveCode,
                    secondaryArchiveCode,
                    additionalCode
                    )
            );

            String caseNumber = caseService.createCase(createCaseArgs);
            createDocumentsForCase(SoknadDrosjeloyveResource, caseNumber);
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
                    .forEach(documentService::createDocument);
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(SamferdselActions.UPDATE_SOKNADDROSJELOYVE.name());
    }
}
