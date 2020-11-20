package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;
import no.p360.model.ContactService.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class ContactService extends P360Service {

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

    public List<Enterprise> getEnterprisesByCategory(String... categories) throws EnterpriseNotFound {
        return getEnterprises(a -> a.setCategories(Arrays.asList(categories)));
    }

    public List<Enterprise> getEnterprisesByName(String name) throws EnterpriseNotFound {
        return getEnterprises(a -> a.setName(name));
    }

    public Enterprise getEnterpriseByEnterpriseNumber(String enterpriseNumber) throws EnterpriseNotFound {
        return getEnterprises(a -> a.setEnterpriseNumber(enterpriseNumber)).get(0);
    }

    private List<Enterprise> getEnterprises(Consumer<GetEnterprisesArgs> argsConsumer) throws EnterpriseNotFound {
        GetEnterprisesArgs getEnterprisesArgs = new GetEnterprisesArgs();
        getEnterprisesArgs.setIncludeCustomFields(true);
        argsConsumer.accept(getEnterprisesArgs);

        GetEnterprisesResponse getEnterprisesResponse = call("ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);

        log.info("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful() && getEnterprisesResponse.getTotalPageCount() == 1) {
            return getEnterprisesResponse.getEnterprises();
        }

        throw new EnterpriseNotFound(getEnterprisesResponse.getErrorMessage());
    }

    public Integer synchronizePrivatePerson(SynchronizePrivatePersonArgs privatePerson) throws CreateContactException {
        log.info("Create Private Person: {}", privatePerson);
        SynchronizePrivatePersonResponse synchronizePrivatePersonResponse = call("ContactService/SynchronizePrivatePerson", privatePerson, SynchronizePrivatePersonResponse.class);
        log.info("Private Person Result: {}", synchronizePrivatePersonResponse);
        if (synchronizePrivatePersonResponse.getSuccessful()) {
            return synchronizePrivatePersonResponse.getRecno();
        }
        throw new CreateContactException(synchronizePrivatePersonResponse.getErrorMessage());
    }

    public Integer synchronizeEnterprise(SynchronizeEnterpriseArgs enterprise) throws CreateEnterpriseException {
        log.info("Create Enterprise: {}", enterprise);
        SynchronizeEnterpriseResponse synchronizeEnterpriseResponse = call("ContactService/SynchronizeEnterprise", enterprise, SynchronizeEnterpriseResponse.class);
        log.info("Enterprise Result: {}", synchronizeEnterpriseResponse);
        if (synchronizeEnterpriseResponse.getSuccessful()) {
            return synchronizeEnterpriseResponse.getRecno();
        }
        throw new CreateEnterpriseException(synchronizeEnterpriseResponse.getErrorMessage());
    }
}
