package no.novari.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.arkiv.CaseDefaults;
import no.novari.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.data.exception.IllegalCaseNumberFormat;
import no.novari.p360.data.exception.NotTilskuddfartoyException;
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
public class TilskuddFartoyFactory {

    @Value("${fint.case.defaults.tilskuddfartoy.prosjekt:}")
    private String project;

    private final NoarkFactory noarkFactory;
    private final JournalpostFactory journalpostFactory;
    private final CaseProperties properties;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public TilskuddFartoyFactory(NoarkFactory noarkFactory, JournalpostFactory journalpostFactory, CaseDefaults caseDefaults, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.noarkFactory = noarkFactory;
        this.journalpostFactory = journalpostFactory;

        properties = caseDefaults.getTilskuddfartoy();
        this.caseQueryService = caseQueryService;

        filterSet = filterSetService.getFilterSetForCaseType(TilskuddFartoyResource.class);
    }

    public TilskuddFartoyResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddfartoyException {
        if (!isTilskuddFartoy(caseResult)) {
            throw new NotTilskuddfartoyException(caseResult.getCaseNumber());
        }

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));

        return noarkFactory.getSaksmappe(filterSet, properties, caseResult, tilskuddFartoy);
    }

    public CreateCaseArgs convertToCreateCase(TilskuddFartoyResource tilskuddFartoy) {
        CreateCaseArgs createCaseArgs = noarkFactory.createCaseArgs(properties, tilskuddFartoy);
        createCaseArgs.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer()));
        if (StringUtils.isNotBlank(project)) {
            createCaseArgs.setProject(project);
        }

        return createCaseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        Case theCase = caseQueryService.query(filterSet, "mappeid/" + caseNumber).collect(QueryUtils.toSingleton());
        TilskuddFartoyResource resource = noarkFactory.getSaksmappe(filterSet, properties, theCase, new TilskuddFartoyResource());
        Optional.of(FintUtils.createIdentifikator(theCase.getExternalId().getId())).ifPresent(resource::setSoknadsnummer);
        log.debug("Currently working (aka creating documents) with søknadsnummer: ", resource.getSoknadsnummer());

        return journalpostFactory.toP360(journalpostResource, caseNumber, resource, properties);
    }

    // TODO: 2019-05-11 Should we check for both archive classification and external id (is it a digisak)
    // TODO Compare with CaseProperties
    private boolean isTilskuddFartoy(Case caseResult) {

        if (FintUtils.optionalValue(caseResult.getExternalId()).isPresent() && FintUtils.optionalValue(caseResult.getArchiveCodes()).isPresent()) {
            return caseResult.getExternalId().getType().equals(Constants.EXTERNAL_ID_TYPE);
        }

        return false;
    }
}
