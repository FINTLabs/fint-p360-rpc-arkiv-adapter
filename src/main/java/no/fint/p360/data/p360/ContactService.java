package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.*;
import no.p360.model.ContactService.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class ContactService extends P360Service {

    public PrivatePerson getPrivatePersonByRecno(int recno) {
        GetPrivatePersonsArgs getPrivatePersonsArgs = new GetPrivatePersonsArgs();
        Parameter__2 parameter = new Parameter__2();
        parameter.setRecno(recno);
        parameter.setIncludeCustomFields(true);
        getPrivatePersonsArgs.setParameter(parameter);
        GetPrivatePersonsResponse getPrivatePersonsResponse = call("ContactService/GetPrivatePersons", getPrivatePersonsArgs, GetPrivatePersonsResponse.class);
        log.info("PrivatePersonsResult: {}", getPrivatePersonsResponse);
        if (getPrivatePersonsResponse.getSuccessful() && getPrivatePersonsResponse.getTotalPageCount() == 1) {
            return getPrivatePersonsResponse.getPrivatePersons().get(0);
        }
        return null;
    }

    public PrivatePerson getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound {
        GetPrivatePersonsArgs getPrivatePersonsArgs = new GetPrivatePersonsArgs();
        Parameter__2 parameter = new Parameter__2();
        parameter.setIncludeCustomFields(true);
        parameter.setPersonalIdNumber(personalIdNumber);
        getPrivatePersonsArgs.setParameter(parameter);

        GetPrivatePersonsResponse getPrivatePersonsResponse = call("ContactService/GetPrivatePersons", getPrivatePersonsArgs, GetPrivatePersonsResponse.class);
        log.info("PrivatePersonsResult: {}", getPrivatePersonsResponse);

        if (getPrivatePersonsResponse.getSuccessful() && getPrivatePersonsResponse.getTotalPageCount() == 1) {
            return getPrivatePersonsResponse.getPrivatePersons().get(0);
        }

        throw new PrivatePersonNotFound(getPrivatePersonsResponse.getErrorMessage());

    }

    public ContactPerson getContactPersonByRecno(int recNo) {
        GetContactPersonsArgs getContactPersonsArgs = new GetContactPersonsArgs();
        Parameter parameter = new Parameter();
        parameter.setIncludeCustomFields(true);
        parameter.setRecno(recNo);
        getContactPersonsArgs.setParameter(parameter);

        GetContactPersonsResponse getContactPersonsResponse = call("ContactService/GetContactPersons", getContactPersonsArgs, GetContactPersonsResponse.class);
        log.info("ContactPersonsResult: {}", getContactPersonsResponse);

        if (getContactPersonsResponse.getSuccessful() && getContactPersonsResponse.getTotalPageCount() == 1) {
            return getContactPersonsResponse.getContactPersons().get(0);
        }
        return null;
    }

    public Enterprise getEnterpriseByRecno(int recNo) {

        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        Parameter__1 parameter = new Parameter__1();
        parameter.setIncludeCustomFields(true);
        parameter.setRecno(recNo);
        getEnterprisesArgs.setParameter(parameter);

        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful() && getEnterprisesResponse.getTotalPageCount() == 1) {
            return getEnterprisesResponse.getEnterprises().get(0);
        }

        return null;
    }

    public Enterprise getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        Parameter__1 parameter = new Parameter__1();
        parameter.setIncludeCustomFields(true);
        parameter.setEnterpriseNumber(enterpriseNumber);
        getEnterprisesArgs.setParameter(parameter);

        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful() && getEnterprisesResponse.getTotalPageCount() == 1) {
            return getEnterprisesResponse.getEnterprises().get(0);
        }

        throw new EnterpriseNotFound(getEnterprisesResponse.getErrorMessage());
    }

    public Stream<Enterprise> searchEnterprise(Map<String, String> queryParams) {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        Parameter__1 parameter = new Parameter__1();

        if (queryParams.containsKey("navn")) {
            parameter.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("organisasjonsnummer")) {
            parameter.setEnterpriseNumber(queryParams.get("organisasjonsnummer"));
        }
        if (queryParams.containsKey("maxResults")) {
            parameter.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }
        getEnterprisesArgs.setParameter(parameter);

        log.info("GetEnterprises query: {}", getEnterprisesArgs);
        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);
        log.info("GetEnterprises result: {}", getEnterprisesResponse);

        if (!getEnterprisesResponse.getSuccessful()) {
            return Stream.empty();
        }
        return getEnterprisesResponse.getEnterprises().stream();
    }

    public Stream<PrivatePerson> searchPrivatePerson(Map<String, String> queryParams) {
        GetPrivatePersonsArgs getPrivatePersonsArgs = new GetPrivatePersonsArgs();
        Parameter__2 parameter = new Parameter__2();
        if (queryParams.containsKey("navn")) {
            parameter.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("maxResults")) {
            parameter.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }
        if (!queryParams.containsKey("navn")) {
            return Stream.empty();
        }
        getPrivatePersonsArgs.setParameter(parameter);

        log.info("GetPrivatePersons query: {}", getPrivatePersonsArgs);
        GetPrivatePersonsResponse getPrivatePersonsResponse = call("ContactService/GetPrivatePersons", getPrivatePersonsArgs, GetPrivatePersonsResponse.class);
        log.info("GetPrivatePersons: {}", getPrivatePersonsResponse);

        if (!getPrivatePersonsResponse.getSuccessful()) {
            return Stream.empty();
        }

        return getPrivatePersonsResponse.getPrivatePersons().stream();
    }

    public Stream<ContactPerson> searchContactPerson(Map<String, String> queryParams) {
        GetContactPersonsArgs getContactPersonsArgs = new GetContactPersonsArgs();
        Parameter parameter = new Parameter();

        if (queryParams.containsKey("navn")) {
            parameter.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("maxResults")) {
            parameter.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }
        if (!queryParams.containsKey("navn")) {
            return Stream.empty();
        }

        getContactPersonsArgs.setParameter(parameter);

        log.info("GetContactPersons query: {}", getContactPersonsArgs);
        GetContactPersonsResponse getContactPersonsResponse = call("ContactService/GetContactPersons", getContactPersonsArgs, GetContactPersonsResponse.class);
        log.info("GetContactPersons result: {}", getContactPersonsResponse);

        if (!getContactPersonsResponse.getSuccessful()) {
            return Stream.empty();
        }

        return getContactPersonsResponse.getContactPersons().stream();
    }

    public Integer createPrivatePerson(SynchronizePrivatePersonArgs privatePerson) throws CreateContactException {
        log.info("Create Private Person: {}", privatePerson);
        SynchronizePrivatePersonResponse synchronizePrivatePersonResponse = call("ContactService/SynchronizePrivatePerson", privatePerson, SynchronizePrivatePersonResponse.class);
        log.info("Private Person Result: {}", synchronizePrivatePersonResponse);
        if (synchronizePrivatePersonResponse.getSuccessful()) {
            return synchronizePrivatePersonResponse.getRecno();
        }
        throw new CreateContactException(synchronizePrivatePersonResponse.getErrorMessage());
    }

    public Integer createEnterprise(SynchronizeEnterpriseArgs enterprise) throws CreateEnterpriseException {
        log.info("Create Enterprise: {}", enterprise);
        SynchronizeEnterpriseResponse synchronizeEnterpriseResponse = call("ContactService/SynchronizeEnterprise", enterprise, SynchronizeEnterpriseResponse.class);
        log.info("Enterprise Result: {}", synchronizeEnterpriseResponse);
        if (synchronizeEnterpriseResponse.getSuccessful()) {
            return synchronizeEnterpriseResponse.getRecno();
        }
        throw new CreateEnterpriseException(synchronizeEnterpriseResponse.getErrorMessage());
    }
    public boolean ping()  {
        return getHealth("ContactService/Ping");
    }
}
