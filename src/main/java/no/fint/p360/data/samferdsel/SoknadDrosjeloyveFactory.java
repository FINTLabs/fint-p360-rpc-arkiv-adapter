package no.fint.p360.data.samferdsel;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaults;
import no.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.arkiv.samferdsel.SoknadDrosjeloyveResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.model.FilterSet;
import no.fint.p360.service.FilterSetService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SoknadDrosjeloyveFactory {

    @Value("${fint.case.defaults.drosjeloyve.tilgangsgruppe.journalpost:Drosjeløyver}")
    private String journalpostTilgangsgruppe;

    @Value("${fint.case.defaults.drosjeloyve.tilgangsgruppe.sak:Alle}")
    private String sakTilgangsgruppe;

    @Value("${fint.case.defaults.drosjeloyve.part:false}")
    private boolean createPart;

    private final NoarkFactory noarkFactory;
    private final JournalpostFactory journalpostFactory;
    private final CaseProperties properties;
    private final FilterSet filterSet;

    public SoknadDrosjeloyveFactory(NoarkFactory noarkFactory, JournalpostFactory journalpostFactory, CaseDefaults caseDefaults, FilterSetService filterSetService) {
        this.noarkFactory = noarkFactory;
        this.journalpostFactory = journalpostFactory;

        properties = caseDefaults.getSoknaddrosjeloyve();
        filterSet = filterSetService.getFilterSetForCaseType(SoknadDrosjeloyveResource.class);
    }

    public SoknadDrosjeloyveResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat {
        SoknadDrosjeloyveResource drosjeloyve = new SoknadDrosjeloyveResource();
        noarkFactory.getSaksmappe(filterSet, properties, caseResult, drosjeloyve);
        return drosjeloyve;
    }

    public CreateCaseArgs convertToCreateCase(SoknadDrosjeloyveResource soknadDrosjeloyveResource) {
        if (createPart) {
            addPart(soknadDrosjeloyveResource);
        }

        final CreateCaseArgs caseArgs = noarkFactory.createCaseArgs(properties, soknadDrosjeloyveResource);

        if (StringUtils.isNotBlank(sakTilgangsgruppe)) {
            caseArgs.setAccessGroup(sakTilgangsgruppe);

            log.info("Using tilgangsgruppe {} on case.", sakTilgangsgruppe);
        }

        return caseArgs;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        CreateDocumentArgs createDocumentArgs = journalpostFactory.toP360(journalpostResource, caseNumber, new SoknadDrosjeloyveResource(), properties);

        if (StringUtils.isNotBlank(journalpostTilgangsgruppe)) {
            createDocumentArgs.setAccessGroup(journalpostTilgangsgruppe);

            log.info("Using tilgangsgruppe {} on document (journalpost).", journalpostTilgangsgruppe);
        }

        return createDocumentArgs;
    }

    private void addPart(SoknadDrosjeloyveResource soknadDrosjeloyveResource) {
        PartResource part = new PartResource();
        part.setPartNavn(soknadDrosjeloyveResource.getOrganisasjonsnavn());
        part.setOrganisasjonsnummer(soknadDrosjeloyveResource.getOrganisasjonsnummer());

        if (soknadDrosjeloyveResource.getPart() != null) {
            soknadDrosjeloyveResource.getPart().add(part);
        } else {
            soknadDrosjeloyveResource.setPart(List.of(part));
        }

        log.info("Created and added a PartResource ({}, {}) to this SoknadDrosjeloyveResource.",
                part.getPartNavn(), part.getOrganisasjonsnummer());
    }
}
