package no.fint.p360.data.noark.sak;

import no.fint.model.resource.administrasjon.arkiv.SakResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.p360.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SakService {

    @Autowired
    private SakFactory sakFactory;

    @Autowired
    private CaseService caseService;

    public List<SakResource> searchSakByTitle(Map<String, String> query) throws GetDocumentException, IllegalCaseNumberFormat {

        String title = String.format("%%%s%%", query.get("title"));
        String maxReturnedCases = query.getOrDefault("maxResult", "10");

        return sakFactory.toFintResourceList(caseService.getCasesByTitle(title, maxReturnedCases));
    }

    public SakResource getSakByCaseNumber(String caseNumber) throws GetDocumentException, IllegalCaseNumberFormat, GetTilskuddFartoyNotFoundException {
        return sakFactory.toFintResource(caseService.getCaseByCaseNumber(caseNumber));
    }

    public SakResource getSakBySystemId(String systemId) throws GetDocumentException, IllegalCaseNumberFormat, GetTilskuddFartoyNotFoundException {
        return sakFactory.toFintResource(caseService.getCaseBySystemId(systemId));
    }
    public boolean health()  {
        return caseService.ping() && sakFactory.health();
    }
}
