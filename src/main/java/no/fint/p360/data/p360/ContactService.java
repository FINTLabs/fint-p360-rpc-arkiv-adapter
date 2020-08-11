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
        getPrivatePersonsArgs.setRecno(recno);
        getPrivatePersonsArgs.setIncludeCustomFields(true);
        GetPrivatePersonsResponse getPrivatePersonsResponse = call("ContactService/GetPrivatePersons", getPrivatePersonsArgs, GetPrivatePersonsResponse.class);
        log.info("PrivatePersonsResult: {}", getPrivatePersonsResponse);
        if (getPrivatePersonsResponse.getSuccessful() && getPrivatePersonsResponse.getTotalPageCount() == 1) {
            return getPrivatePersonsResponse.getPrivatePersons().get(0);
        }
        return null;
    }

    public PrivatePerson getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound {
        GetPrivatePersonsArgs getPrivatePersonsArgs = new GetPrivatePersonsArgs();
        getPrivatePersonsArgs.setIncludeCustomFields(true);
        getPrivatePersonsArgs.setPersonalIdNumber(personalIdNumber);

        GetPrivatePersonsResponse getPrivatePersonsResponse = call("ContactService/GetPrivatePersons", getPrivatePersonsArgs, GetPrivatePersonsResponse.class);
        log.info("PrivatePersonsResult: {}", getPrivatePersonsResponse);

        if (getPrivatePersonsResponse.getSuccessful() && getPrivatePersonsResponse.getTotalPageCount() == 1) {
            return getPrivatePersonsResponse.getPrivatePersons().get(0);
        }

        throw new PrivatePersonNotFound(getPrivatePersonsResponse.getErrorMessage());

    }

    public ContactPerson getContactPersonByRecno(int recNo) {
        GetContactPersonsArgs getContactPersonsArgs = new GetContactPersonsArgs();
        getContactPersonsArgs.setIncludeCustomFields(true);
        getContactPersonsArgs.setRecno(recNo);

        GetContactPersonsResponse getContactPersonsResponse = call("ContactService/GetContactPersons", getContactPersonsArgs, GetContactPersonsResponse.class);
        log.info("ContactPersonsResult: {}", getContactPersonsResponse);

        if (getContactPersonsResponse.getSuccessful() && getContactPersonsResponse.getTotalPageCount() == 1) {
            return getContactPersonsResponse.getContactPersons().get(0);
        }
        return null;
    }

    public Enterprise getEnterpriseByRecno(int recNo) {

        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        getEnterprisesArgs.setIncludeCustomFields(true);
        getEnterprisesArgs.setRecno(recNo);

        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful() && getEnterprisesResponse.getTotalPageCount() == 1) {
            return getEnterprisesResponse.getEnterprises().get(0);
        }

        return null;
    }

    public Enterprise getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        getEnterprisesArgs.setIncludeCustomFields(true);
        getEnterprisesArgs.setEnterpriseNumber(enterpriseNumber);

        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful() && getEnterprisesResponse.getTotalPageCount() == 1) {
            return getEnterprisesResponse.getEnterprises().get(0);
        }

        throw new EnterpriseNotFound(getEnterprisesResponse.getErrorMessage());
    }

    public Stream<Enterprise> searchEnterprise(Map<String, String> queryParams) {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();

        if (queryParams.containsKey("navn")) {
            getEnterprisesArgs.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("organisasjonsnummer")) {
            getEnterprisesArgs.setEnterpriseNumber(queryParams.get("organisasjonsnummer"));
        }
        if (queryParams.containsKey("maxResults")) {
            getEnterprisesArgs.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }

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
        if (queryParams.containsKey("navn")) {
            getPrivatePersonsArgs.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("maxResults")) {
            getPrivatePersonsArgs.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }
        if (!queryParams.containsKey("navn")) {
            return Stream.empty();
        }

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

        if (queryParams.containsKey("navn")) {
            getContactPersonsArgs.setName(queryParams.get("navn"));
        }
        if (queryParams.containsKey("maxResults")) {
            getContactPersonsArgs.setMaxRows(Integer.valueOf(queryParams.get("maxResults")));
        }
        if (!queryParams.containsKey("navn")) {
            return Stream.empty();
        }

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
