package no.fint.p360.data.samferdsel;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.AdditionalFieldService;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.CaseProperties;
import no.fint.arkiv.TitleService;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.samferdsel.SoknadDrosjeloyveResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.NotTilskuddFredaHusPrivatEieException;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.model.ContextUser;
import no.fint.p360.service.ContextUserService;
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

    private final NoarkFactory noarkFactory;
    private final JournalpostFactory journalpostFactory;
    private final CaseProperties properties;
    private final ContextUser contextUser;


    public SoknadDrosjeloyveFactory(NoarkFactory noarkFactory, JournalpostFactory journalpostFactory, CaseDefaults caseDefaults, ContextUserService contextUserService) {
        this.noarkFactory = noarkFactory;
        this.journalpostFactory = journalpostFactory;

        properties = caseDefaults.getSoknaddrosjeloyve();
        contextUser = contextUserService.getContextUserForClass(SoknadDrosjeloyveResource.class);
    }

    public SoknadDrosjeloyveResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat {
        SoknadDrosjeloyveResource drosjeloyve = new SoknadDrosjeloyveResource();
        noarkFactory.getSaksmappe(properties, caseResult, drosjeloyve);
        return drosjeloyve;
    }

    public CreateCaseArgs convertToCreateCase(SoknadDrosjeloyveResource soknadDrosjeloyveResource) {
        final CreateCaseArgs caseArgs = noarkFactory.createCaseArgs(properties, soknadDrosjeloyveResource);

        if (StringUtils.isNotBlank(sakTilgangsgruppe)) {
            caseArgs.setAccessGroup(sakTilgangsgruppe);
        }

        if (contextUser != null && StringUtils.isNotBlank(contextUser.getUsername())) {
            caseArgs.setADContextUser(contextUser.getUsername());
            log.info("CreateCaseArgs with ADContextUser set to {}", contextUser.getUsername());
        }

        return caseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        CreateDocumentArgs createDocumentArgs = journalpostFactory.toP360(journalpostResource, caseNumber);

        if (StringUtils.isNotBlank(journalpostTilgangsgruppe)) {
            createDocumentArgs.setAccessGroup(journalpostTilgangsgruppe);
        } else {
            log.warn("The drosjeloyve.journalpost.tilgangsgruppe is blank for case {}, you're hereby warned!", caseNumber);
        }

        if (contextUser != null && StringUtils.isNotBlank(contextUser.getUsername())) {
            createDocumentArgs.setADContextUser(contextUser.getUsername());
            log.info("CreateDocumentArgs with ADContextUser set to {}", contextUser.getUsername());
        }

        return createDocumentArgs;
    }
}
