package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.CaseNotFound;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.utilities.Constants;
import no.p360.model.CaseService.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CaseService extends P360Service {

    public Case getCaseByCaseNumber(String caseNumber) throws CaseNotFound {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setCaseNumber(caseNumber);

        return getCase(getCasesArgs);
    }

    public Case getCaseBySystemId(String systemId) throws CaseNotFound {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setRecno(Integer.valueOf(systemId));

        return getCase(getCasesArgs);
    }

    public Case getCaseByExternalId(String externalId) throws CaseNotFound {

        ExternalId__1 id = new ExternalId__1();
        id.setId(externalId);
        id.setType(Constants.EXTERNAL_ID_TYPE);

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setExternalId(id);

        return getCase(getCasesArgs);
    }

    public List<Case> getCasesByTitle(String title, String maxReturnedCases) {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setTitle(title);

        if (maxReturnedCases != null)
            getCasesArgs.setMaxReturnedCases(Integer.parseInt(maxReturnedCases));

        return getCases(getCasesArgs);
    }

    public Case getCase(GetCasesArgs getCasesArgs) throws CaseNotFound {

        List<Case> caseResult = getCases(getCasesArgs);

        if (caseResult.size() == 1) {
            return caseResult.get(0);
        } else if (caseResult.size() == 0) {
            throw new CaseNotFound("Zero cases found");
        } else {
            throw new CaseNotFound("More than one case found");
        }
    }

    public List<Case> getCases(GetCasesArgs getCasesArgs) {
        getCasesArgs.setIncludeCaseContacts(true);
        getCasesArgs.setIncludeCustomFields(true);

        GetCasesResponse response = call("CaseService/GetCases", getCasesArgs, GetCasesResponse.class);
        if (!response.getSuccessful()) {
            log.warn("GetCases {}: {}", getCasesArgs, response);
            throw new CaseNotFound(response.getErrorMessage());
        }

        return response.getCases();
    }

    public String createCase(CreateCaseArgs createCasesArgs) throws CreateCaseException {

        CreateCaseResponse response = call("CaseService/CreateCase", createCasesArgs, CreateCaseResponse.class);

        if (!response.getSuccessful())
            throw new CreateCaseException(response.getErrorMessage());

        return response.getCaseNumber();
    }

    public boolean ping() {
        return getHealth("CaseService/Ping");
    }
}
