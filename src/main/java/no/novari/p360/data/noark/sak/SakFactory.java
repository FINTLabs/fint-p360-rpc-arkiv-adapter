package no.novari.p360.data.noark.sak;

import no.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.data.exception.IllegalCaseNumberFormat;
import no.novari.p360.data.noark.common.NoarkFactory;
import no.novari.p360.data.noark.journalpost.JournalpostFactory;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.FilterSetService;
import no.p360.model.CaseService.Case;
import no.p360.model.CaseService.CreateCaseArgs;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SakFactory {

    private final NoarkFactory noarkFactory;
    private final FilterSet filterSet;
    private final JournalpostFactory journalpostFactory;

    public SakFactory(NoarkFactory noarkFactory, FilterSetService filterSetService,
                      JournalpostFactory journalpostFactory) {
        this.noarkFactory = noarkFactory;
        filterSet = filterSetService.getDefaultFilterSet();
        this.journalpostFactory = journalpostFactory;
    }

    public SakResource toFintResource(Case caseResult) throws IllegalCaseNumberFormat, GetDocumentException {
        // TODO This might not work
        return noarkFactory.getSaksmappe(filterSet, new CaseProperties(), caseResult, new SakResource());
    }

    public List<SakResource> toFintResourceList(List<Case> caseResults) throws IllegalCaseNumberFormat, GetDocumentException {
        List<SakResource> result = new ArrayList<>(caseResults.size());
        for (Case caseResult : caseResults) {
            result.add(toFintResource(caseResult));
        }
        return result;
    }

    public CreateCaseArgs convertToCreateCase(SakResource sakResource) {
        return noarkFactory.createCaseArgs(new CaseProperties(), sakResource);
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        return journalpostFactory.toP360(journalpostResource, caseNumber, new SakResource(), new CaseProperties());
    }

}
