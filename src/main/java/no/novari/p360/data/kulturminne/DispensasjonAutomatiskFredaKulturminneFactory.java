package no.novari.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.arkiv.CaseDefaults;
import no.novari.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.data.exception.IllegalCaseNumberFormat;
import no.novari.p360.data.exception.NotDispensasjonAutomatiskFredaKulturminneException;
import no.novari.p360.data.noark.common.NoarkFactory;
import no.novari.p360.data.noark.journalpost.JournalpostFactory;
import no.novari.p360.data.utilities.Constants;
import no.novari.p360.data.utilities.FintUtils;
import no.novari.p360.data.utilities.P360Utils;
import no.novari.p360.data.utilities.QueryUtils;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class DispensasjonAutomatiskFredaKulturminneFactory {

    @Value("${fint.case.defaults.dispensasjonautomatiskfredakulturminne.prosjekt:}")
    private String project;

    private final NoarkFactory noarkFactory;
    private final JournalpostFactory journalpostFactory;
    private final CaseProperties properties;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public DispensasjonAutomatiskFredaKulturminneFactory(NoarkFactory noarkFactory,
                                                         JournalpostFactory journalpostFactory,
                                                         CaseDefaults caseDefaults,
                                                         CaseQueryService caseQueryService,
                                                         FilterSetService filterSetService) {

        this.noarkFactory = noarkFactory;
        this.journalpostFactory = journalpostFactory;

        properties = caseDefaults.getDispensasjonautomatiskfredakulturminne();
        this.caseQueryService = caseQueryService;

        filterSet = filterSetService.getFilterSetForCaseType(DispensasjonAutomatiskFredaKulturminneResource.class);
    }

    public DispensasjonAutomatiskFredaKulturminneResource toFintResource(Case caseResult)
            throws GetDocumentException, IllegalCaseNumberFormat, NotDispensasjonAutomatiskFredaKulturminneException {

        if (!isDispensasjonAutomatiskFredaKulturminne(caseResult)) {
            throw new NotDispensasjonAutomatiskFredaKulturminneException(caseResult.getCaseNumber());
        }
        DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne =
                new DispensasjonAutomatiskFredaKulturminneResource();
        dispensasjonAutomatiskFredaKulturminne.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));

        return noarkFactory.getSaksmappe(filterSet, properties, caseResult, dispensasjonAutomatiskFredaKulturminne);
    }

    public CreateCaseArgs convertToCreateCase(DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne) {
        CreateCaseArgs createCaseArgs = noarkFactory.createCaseArgs(properties, dispensasjonAutomatiskFredaKulturminne);
        createCaseArgs.setExternalId(P360Utils.getExternalIdParameter(dispensasjonAutomatiskFredaKulturminne.getSoknadsnummer()));
        if (StringUtils.isNotBlank(project)) {
            createCaseArgs.setProject(project);
        }

        return createCaseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        Case theCase = caseQueryService.query(filterSet, "mappeid/" + caseNumber).collect(QueryUtils.toSingleton());
        DispensasjonAutomatiskFredaKulturminneResource resource = noarkFactory.getSaksmappe(filterSet, properties, theCase,
                new DispensasjonAutomatiskFredaKulturminneResource());
        Optional.of(FintUtils.createIdentifikator(theCase.getExternalId().getId())).ifPresent(resource::setSoknadsnummer);
        log.debug("Currently working (aka creating documents) with søknadsnummer: {}", resource.getSoknadsnummer());

        return journalpostFactory.toP360(journalpostResource, caseNumber, resource, properties);
    }

    // TODO: 2019-05-11 Should we check for both archive classification and external id (is it a digisak)
    // TODO Compare with CaseProperties
    private boolean isDispensasjonAutomatiskFredaKulturminne(Case caseResult) {

        if (FintUtils.optionalValue(caseResult.getExternalId()).isPresent()
                && FintUtils.optionalValue(caseResult.getArchiveCodes()).isPresent()) {
            return caseResult.getExternalId().getType().equals(Constants.EXTERNAL_ID_TYPE);
        }

        return false;
    }
}
