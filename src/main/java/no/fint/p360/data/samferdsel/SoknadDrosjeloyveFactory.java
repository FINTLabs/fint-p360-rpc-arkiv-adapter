package no.fint.p360.data.samferdsel;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.AdditionalFieldService;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.TitleService;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.samferdsel.SoknadDrosjeloyveResource;
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
public class SoknadDrosjeloyveFactory {

    @Value("${fint.case.defaults.drosjeloyve.tilgangsgruppe.journalpost:Drosjeløyver}")
    private String journalpostTilgangsgruppe;

    @Value("${fint.case.defaults.drosjeloyve.tilgangsgruppe.sak:Alle}")
    private String sakTilgangsgruppe;

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

    public SoknadDrosjeloyveResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddFredaHusPrivatEieException {
        SoknadDrosjeloyveResource drosjeloyve = new SoknadDrosjeloyveResource();
        noarkFactory.getSaksmappe(caseDefaults.getSoknaddrosjeloyve(), caseResult, drosjeloyve);
        return drosjeloyve;
    }


    public CreateCaseArgs convertToCreateCase(SoknadDrosjeloyveResource soknadDrosjeloyveResource) {
        final CreateCaseArgs caseArgs = noarkFactory.createCaseArgs(caseDefaults.getSoknaddrosjeloyve(), soknadDrosjeloyveResource);
        if (StringUtils.isNotBlank(sakTilgangsgruppe)) {
            caseArgs.setAccessGroup(sakTilgangsgruppe);
        }
        return caseArgs;
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
