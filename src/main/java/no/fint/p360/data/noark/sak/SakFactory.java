package no.fint.p360.data.noark.sak;

import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.utilities.NOARKUtils;
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

        SakResource sakResource = new SakResource();
        String caseNumber = caseResult.getCaseNumber();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        noarkFactory.getSaksmappe(caseResult, sakResource);

        sakResource.addSaksstatus(Link.with(Saksstatus.class, "systemid", caseResult.getStatus()));
        sakResource.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        sakResource.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));

        return sakResource;
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
