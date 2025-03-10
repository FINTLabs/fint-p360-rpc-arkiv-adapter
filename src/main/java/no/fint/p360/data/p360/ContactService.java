package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.CreateContactException;
import no.fint.p360.data.exception.CreateEnterpriseException;
import no.fint.p360.data.exception.EnterpriseNotFound;
import no.fint.p360.data.exception.PrivatePersonNotFound;
import no.fint.p360.service.FilterSetService;
import no.p360.model.ContactService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class ContactService extends P360Service {

    @Autowired
    private FilterSetService filterSetService;

    public PrivatePerson getPrivatePersonByPersonalIdNumber(String personalIdNumber) throws PrivatePersonNotFound {
        GetPrivatePersonsArgs getPrivatePersonsArgs = new GetPrivatePersonsArgs();
        getPrivatePersonsArgs.setIncludeCustomFields(true);
        getPrivatePersonsArgs.setPersonalIdNumber(personalIdNumber);

        GetPrivatePersonsResponse getPrivatePersonsResponse = call(filterSetService.getDefaultFilterSet(),
                "ContactService/GetPrivatePersons", getPrivatePersonsArgs, GetPrivatePersonsResponse.class);
        log.debug("PrivatePersonsResult: {}", getPrivatePersonsResponse);

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

        GetEnterprisesResponse getEnterprisesResponse = call(filterSetService.getDefaultFilterSet(),
                "ContactService/GetEnterprises", getEnterprisesArgs, GetEnterprisesResponse.class);
        log.debug("EnterpriseResult: {}", getEnterprisesResponse);

        if (getEnterprisesResponse.getSuccessful() && getEnterprisesResponse.getTotalPageCount() == 1) {
            return getEnterprisesResponse.getEnterprises();
        }

        throw new EnterpriseNotFound(getEnterprisesResponse.getErrorMessage());
    }

    public Integer synchronizePrivatePerson(SynchronizePrivatePersonArgs privatePerson) throws CreateContactException {
        log.debug("Create Private Person: {}", privatePerson);
        SynchronizePrivatePersonResponse synchronizePrivatePersonResponse = call(filterSetService.getDefaultFilterSet(),
                "ContactService/SynchronizePrivatePerson", privatePerson, SynchronizePrivatePersonResponse.class);
        log.debug("Private Person Result: {}", synchronizePrivatePersonResponse);

        if (synchronizePrivatePersonResponse.getSuccessful()) {
            return synchronizePrivatePersonResponse.getRecno();
        }

        throw new CreateContactException(synchronizePrivatePersonResponse.getErrorMessage());
    }

    public Integer synchronizeEnterprise(SynchronizeEnterpriseArgs enterprise) throws CreateEnterpriseException {
        log.debug("Create Enterprise: {}", enterprise);
        SynchronizeEnterpriseResponse synchronizeEnterpriseResponse = call(filterSetService.getDefaultFilterSet(),
                "ContactService/SynchronizeEnterprise", enterprise, SynchronizeEnterpriseResponse.class);
        log.debug("Enterprise Result: {}", synchronizeEnterpriseResponse);

        if (synchronizeEnterpriseResponse.getSuccessful()) {
            return synchronizeEnterpriseResponse.getRecno();
        }

        throw new CreateEnterpriseException(synchronizeEnterpriseResponse.getErrorMessage());
    }
}
