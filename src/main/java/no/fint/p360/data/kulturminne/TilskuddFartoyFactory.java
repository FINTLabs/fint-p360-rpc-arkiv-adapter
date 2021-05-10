package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.AdditionalFieldService;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.TitleService;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.NotTilskuddfartoyException;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.P360Utils;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TilskuddFartoyFactory {

    @Value("${fint.case.defaults.tilskuddfartoy.prosjekt:}")
    private String project;

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    TitleService titleService;

    @Autowired
    AdditionalFieldService additionalFieldService;

    @Autowired
    CaseDefaults caseDefaults;

    public TilskuddFartoyResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddfartoyException {
        if (!isTilskuddFartoy(caseResult)) {
            throw new NotTilskuddfartoyException(caseResult.getCaseNumber());
        }

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));
        return noarkFactory.getSaksmappe(caseDefaults.getTilskuddfartoy(), caseResult, tilskuddFartoy);

        /*
        String caseNumber = caseResult.getCaseNumber();
        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "soknadsnummer", caseResult.getExternalId().getId()));
         */

    }


    public CreateCaseArgs convertToCreateCase(TilskuddFartoyResource tilskuddFartoy) {
        CreateCaseArgs createCaseArgs = noarkFactory.createCaseArgs(caseDefaults.getTilskuddfartoy(), tilskuddFartoy);
        createCaseArgs.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer()));
        if (StringUtils.isNotBlank(project)) {
            createCaseArgs.setProject(project);
        }
        return createCaseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        return journalpostFactory.toP360(journalpostResource, caseNumber);
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
