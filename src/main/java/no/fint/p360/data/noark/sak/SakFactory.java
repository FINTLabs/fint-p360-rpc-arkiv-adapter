package no.fint.p360.data.noark.sak;

import no.fint.arkiv.CaseProperties;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.p360.model.CaseService.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SakFactory {

    @Autowired
    private NoarkFactory noarkFactory;

    public SakResource toFintResource(Case caseResult) throws IllegalCaseNumberFormat, GetDocumentException {
        // TODO This might not work
        return noarkFactory.getSaksmappe(new CaseProperties(), caseResult, new SakResource());
    }

    public List<SakResource> toFintResourceList(List<Case> caseResults) throws IllegalCaseNumberFormat, GetDocumentException {
        List<SakResource> result = new ArrayList<>(caseResults.size());
        for (Case caseResult : caseResults) {
            result.add(toFintResource(caseResult));
        }
        return result;
    }
    public boolean health() {
        return noarkFactory.health();
    }
}
