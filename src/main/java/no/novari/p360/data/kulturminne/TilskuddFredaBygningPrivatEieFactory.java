package no.novari.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.arkiv.CaseDefaults;
import no.novari.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.data.exception.IllegalCaseNumberFormat;
import no.novari.p360.data.exception.NotTilskuddFredaHusPrivatEieException;
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

@Slf4j
@Service
public class TilskuddFredaBygningPrivatEieFactory {

    @Value("${fint.case.defaults.tilskuddfredabygningprivateie.prosjekt:}")
    private String project;

    private final NoarkFactory noarkFactory;
    private final JournalpostFactory journalpostFactory;
    private final CaseProperties properties;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public TilskuddFredaBygningPrivatEieFactory(NoarkFactory noarkFactory, JournalpostFactory journalpostFactory, CaseDefaults caseDefaults, CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.noarkFactory = noarkFactory;
        this.journalpostFactory = journalpostFactory;

        properties = caseDefaults.getTilskuddfredabygningprivateie();
        this.caseQueryService = caseQueryService;

        filterSet = filterSetService.getFilterSetForCaseType(TilskuddFredaBygningPrivatEieResource.class);
    }

    public TilskuddFredaBygningPrivatEieResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddFredaHusPrivatEieException {
        if (!isTilskuddFredaHusPrivatEie(caseResult)) {
            throw new NotTilskuddFredaHusPrivatEieException(caseResult.getCaseNumber());
        }

        TilskuddFredaBygningPrivatEieResource tilskuddFredaBygningPrivatEie = new TilskuddFredaBygningPrivatEieResource();
        tilskuddFredaBygningPrivatEie.setMatrikkelnummer(new MatrikkelnummerResource());
        tilskuddFredaBygningPrivatEie.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));

        return noarkFactory.getSaksmappe(filterSet, properties, caseResult, tilskuddFredaBygningPrivatEie);
    }

    public CreateCaseArgs convertToCreateCase(TilskuddFredaBygningPrivatEieResource tilskuddFredaBygningPrivatEieResource) {
        final CreateCaseArgs createCaseArgs = noarkFactory.createCaseArgs(properties, tilskuddFredaBygningPrivatEieResource);
        createCaseArgs.setExternalId(P360Utils.getExternalIdParameter(tilskuddFredaBygningPrivatEieResource.getSoknadsnummer()));

        if (StringUtils.isNotBlank(project)) {
            createCaseArgs.setProject(project);
        }

        return createCaseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        TilskuddFredaBygningPrivatEieResource resource =  noarkFactory.getSaksmappe(filterSet, properties, caseQueryService.query(filterSet, "mappeid/" + caseNumber).collect(QueryUtils.toSingleton()), new TilskuddFredaBygningPrivatEieResource());
        return journalpostFactory.toP360(journalpostResource, caseNumber, resource , properties);
    }

    // TODO: 2019-05-11 Should we check for both archive classification and external id (is it a digisak)
    // TODO Compare with CaseProperties
    private boolean isTilskuddFredaHusPrivatEie(Case caseResult) {
        if (FintUtils.optionalValue(caseResult.getExternalId()).isPresent() && FintUtils.optionalValue(caseResult.getArchiveCodes()).isPresent()) {
            return caseResult.getExternalId().getType().equals(Constants.EXTERNAL_ID_TYPE);
        }

        return false;
    }
}
