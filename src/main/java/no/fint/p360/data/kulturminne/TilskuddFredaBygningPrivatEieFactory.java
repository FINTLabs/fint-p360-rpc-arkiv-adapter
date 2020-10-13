package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.AdditionalFieldService;
import no.fint.arkiv.TitleService;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TilskuddFredaBygningPrivatEieFactory {

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    TitleService titleService;

    @Autowired
    AdditionalFieldService additionalFieldService;

    public TilskuddFredaBygningPrivatEieResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddFredaHusPrivatEieException {
        if (!isTilskuddFredaHusPrivatEie(caseResult)) {
            throw new NotTilskuddFredaHusPrivatEieException(caseResult.getCaseNumber());
        }

        TilskuddFredaBygningPrivatEieResource tilskuddFredaBygningPrivatEie = new TilskuddFredaBygningPrivatEieResource();
        tilskuddFredaBygningPrivatEie.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));
        noarkFactory.getSaksmappe(caseResult, tilskuddFredaBygningPrivatEie);

        /*
        String caseNumber = caseResult.getCaseNumber();
        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);
        tilskuddFredaBygningPrivatEie.addSelf(Link.with(TilskuddFredaHusPrivatEie.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFredaBygningPrivatEie.addSelf(Link.with(TilskuddFredaHusPrivatEie.class, "systemid", caseResult.getRecno().toString()));
        tilskuddFredaBygningPrivatEie.addSelf(Link.with(TilskuddFredaHusPrivatEie.class, "soknadsnummer", caseResult.getExternalId().getId()));
         */

        return tilskuddFredaBygningPrivatEie;
    }


    public CreateCaseArgs convertToCreateCase(TilskuddFredaBygningPrivatEieResource tilskuddFredaBygningPrivatEieResource) {
        CreateCaseArgs createCaseArgs = noarkFactory.createCaseArgs(tilskuddFredaBygningPrivatEieResource);
        createCaseArgs.setExternalId(P360Utils.getExternalIdParameter(tilskuddFredaBygningPrivatEieResource.getSoknadsnummer()));
        return createCaseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        return journalpostFactory.toP360(journalpostResource, caseNumber);
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
