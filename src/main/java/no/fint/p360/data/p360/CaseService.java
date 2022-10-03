package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.CaseNotFound;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.model.FilterSet;
import no.p360.model.CaseService.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CaseService extends P360Service {

    public Case getCaseByCaseNumber(FilterSet filterSet, String caseNumber) throws CaseNotFound {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setCaseNumber(caseNumber);

        return getCase(filterSet, getCasesArgs);
    }

    public Case getCaseBySystemId(FilterSet filterSet, String systemId) throws CaseNotFound {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setRecno(Integer.valueOf(systemId));

        return getCase(filterSet, getCasesArgs);
    }

    public Case getCaseByExternalId(FilterSet filterSet, String externalId) throws CaseNotFound {

        ExternalId__1 id = new ExternalId__1();
        id.setId(externalId);
        id.setType(Constants.EXTERNAL_ID_TYPE);

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setExternalId(id);

        return getCase(filterSet, getCasesArgs);
    }

    public List<Case> getCasesByTitle(FilterSet filterSet, String title, String maxReturnedCases) {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setTitle(title);

        if (maxReturnedCases != null)
            getCasesArgs.setMaxReturnedCases(Integer.parseInt(maxReturnedCases));

        return getCases(filterSet, getCasesArgs);
    }

    public Case getCase(FilterSet filterSet, GetCasesArgs getCasesArgs) throws CaseNotFound {

        List<Case> caseResult = getCases(filterSet, getCasesArgs);

        if (caseResult.size() == 1) {
            return caseResult.get(0);
        } else if (caseResult.size() == 0) {
            throw new CaseNotFound("Zero cases found");
        } else {
            throw new CaseNotFound("More than one case found");
        }
    }

    public List<Case> getCases(FilterSet filterSet, GetCasesArgs getCasesArgs) {
        getCasesArgs.setIncludeCaseContacts(true);
        getCasesArgs.setIncludeCustomFields(true);

        GetCasesResponse response = call(filterSet, "CaseService/GetCases", getCasesArgs, GetCasesResponse.class);
        if (!response.getSuccessful()) {
            log.warn("GetCases {}: {}", getCasesArgs, response);
            throw new CaseNotFound(response.getErrorMessage());
        }

        return response.getCases();
    }

    public String createCase(FilterSet filterSet, CreateCaseArgs createCasesArgs) throws CreateCaseException {

        CreateCaseResponse response = call(filterSet, "CaseService/CreateCase", createCasesArgs, CreateCaseResponse.class);

        if (!response.getSuccessful())
            throw new CreateCaseException(response.getErrorMessage());

        return response.getCaseNumber();
    }

}
