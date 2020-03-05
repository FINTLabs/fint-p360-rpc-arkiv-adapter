package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.CreateCaseException;
import no.fint.p360.data.exception.GetTilskuddFartoyNotFoundException;
import no.fint.p360.data.utilities.Constants;
import no.p360.model.CaseService.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CaseService extends P360Service {

    public Case getCaseByCaseNumber(String caseNumber) throws GetTilskuddFartoyNotFoundException {

        GetCasesArgs getCasesArgs = CreateGetCaseArgsWithParameter();
        getCasesArgs.getParameter().setCaseNumber(caseNumber);

        return getCase(getCasesArgs);
    }

    public Case getCaseBySystemId(String systemId) throws GetTilskuddFartoyNotFoundException {

        GetCasesArgs getCasesArgs = CreateGetCaseArgsWithParameter();
        getCasesArgs.getParameter().setRecno(Integer.valueOf(systemId));

        return getCase(getCasesArgs);
    }

    public Case getCaseByExternalId(String externalId) throws GetTilskuddFartoyNotFoundException {

        ExternalId__1 id = new ExternalId__1();
        id.setId(externalId);
        id.setType(Constants.EXTERNAL_ID_TYPE);

        GetCasesArgs getCasesArgs = CreateGetCaseArgsWithParameter();
        getCasesArgs.getParameter().setExternalId(id);

        return getCase(getCasesArgs);
    }

    public List<Case> getCasesByTitle(String title, String maxReturnedCases) {

        GetCasesArgs getCasesArgs = CreateGetCaseArgsWithParameter();
        getCasesArgs.getParameter().setTitle(title);

        if (maxReturnedCases != null)
            getCasesArgs.getParameter().setMaxReturnedCases(Integer.parseInt(maxReturnedCases));

        return getCases(getCasesArgs);
    }

    public Case getCase(GetCasesArgs getCasesArgs) throws GetTilskuddFartoyNotFoundException {

        List<Case> caseResult = getCases(getCasesArgs);

        if (caseResult.size() == 1) {
            return caseResult.get(0);
        } else if (caseResult.size() == 0){
            throw new GetTilskuddFartoyNotFoundException("Zero cases found");
        }else {
            throw new GetTilskuddFartoyNotFoundException("More than one case found");
        }
    }

    public List<Case> getCases(GetCasesArgs getCasesArgs) {

        GetCasesResponse response = call("CaseService/GetCases", getCasesArgs, GetCasesResponse.class);

        return response.getCases();
    }

    public String createCase(CreateCaseArgs createCasesArgs) throws CreateCaseException {

        CreateCaseResponse response = call("CaseService/CreateCase", createCasesArgs, CreateCaseResponse.class);

        if (!response.getSuccessful())
            throw new CreateCaseException(response.getErrorDetails());

        return response.getCaseNumber();
    }

    private static GetCasesArgs CreateGetCaseArgsWithParameter() {

        GetCasesArgs getCasesArgs = new GetCasesArgs();
        getCasesArgs.setParameter(new Parameter__1());

        return getCasesArgs;
    }

    public boolean ping()  {
        return getHealth("CaseService/Ping");
    }
}
