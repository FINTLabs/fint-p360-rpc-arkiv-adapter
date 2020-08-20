package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.p360.model.ContactService.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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

    public List<Enterprise> getEnterprisesByCategory(String category) {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        getEnterprisesArgs.setCategories(Collections.singletonList(category));
        getEnterprisesArgs.setIncludeCustomFields(true);
        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful()) {
            return getEnterprisesResponse.getEnterprises();
        } else {
            log.warn(getEnterprisesResponse.getErrorMessage());
        }

        return null;
    }

    public List<Enterprise> getEnterprisesByName(String name) {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        getEnterprisesArgs.setName(name);
        getEnterprisesArgs.setIncludeCustomFields(true);
        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful()) {
            return getEnterprisesResponse.getEnterprises();
        } else {
            log.warn(getEnterprisesResponse.getErrorMessage());
        }

        return null;
    }

}
