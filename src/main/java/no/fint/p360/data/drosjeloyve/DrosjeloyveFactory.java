package no.fint.p360.data.drosjeloyve;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.AdditionalFieldService;
import no.fint.arkiv.TitleService;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.samferdsel.DrosjeloyveResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.NotTilskuddFredaHusPrivatEieException;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DrosjeloyveFactory {

    @Value("${fint.case.defaults.drosjeloyve.journalpost.tilgangsgruppe:Drosjeløyver}")
    private String journalpostTilgangsgruppe;

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    TitleService titleService;

    @Autowired
    AdditionalFieldService additionalFieldService;

    public DrosjeloyveResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddFredaHusPrivatEieException {
        DrosjeloyveResource drosjeloyve = new DrosjeloyveResource();
        noarkFactory.getSaksmappe(caseResult, drosjeloyve);
        return drosjeloyve;
    }


    public CreateCaseArgs convertToCreateCase(DrosjeloyveResource drosjeloyveResource) {
        return noarkFactory.createCaseArgs(drosjeloyveResource);
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        CreateDocumentArgs createDocumentArgs = journalpostFactory.toP360(journalpostResource, caseNumber);

        if(StringUtils.isNotBlank(journalpostTilgangsgruppe)) {
            createDocumentArgs.setAccessGroup(journalpostTilgangsgruppe);
        } else {
            log.warn("The drosjeloyve.journalpost.tilgangsgruppe is blank for case {}, you're hereby warned!", caseNumber);
        }

        return createDocumentArgs;
    }

}
