package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.NotTilskuddFredaHusPrivatEieException;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.P360Utils;
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


    public TilskuddFredaBygningPrivatEieFactory(NoarkFactory noarkFactory, JournalpostFactory journalpostFactory, CaseDefaults caseDefaults) {
        this.noarkFactory = noarkFactory;
        this.journalpostFactory = journalpostFactory;

        properties = caseDefaults.getTilskuddfredabygningprivateie();
    }

    public TilskuddFredaBygningPrivatEieResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddFredaHusPrivatEieException {
        if (!isTilskuddFredaHusPrivatEie(caseResult)) {
            throw new NotTilskuddFredaHusPrivatEieException(caseResult.getCaseNumber());
        }

        TilskuddFredaBygningPrivatEieResource tilskuddFredaBygningPrivatEie = new TilskuddFredaBygningPrivatEieResource();
        tilskuddFredaBygningPrivatEie.setMatrikkelnummer(new MatrikkelnummerResource());
        tilskuddFredaBygningPrivatEie.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));

        return noarkFactory.getSaksmappe(properties, caseResult, tilskuddFredaBygningPrivatEie);
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
        return journalpostFactory.toP360(journalpostResource, caseNumber, new TilskuddFredaBygningPrivatEieResource());
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
