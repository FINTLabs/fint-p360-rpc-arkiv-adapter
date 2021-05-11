package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.ContextUsers;
import no.fint.p360.data.exception.InvalidContextUser;
import no.fint.p360.model.ContextUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContextUserService {

    private final ContextUsers contextUsers;

    public ContextUserService(ContextUsers contextUsers) {
        this.contextUsers = contextUsers;

        if (contextUsers.getAccount() == null) {
            log.warn("No configured P360 ADContextUsers!");
        } else {
            log.info("Configured P360 ADContextUsers: {}", contextUsers);
        }
    }

    public ContextUser getContextUserForClass(Class<?> clazz) {
        String caseTypeName = resourceName(clazz);
        final String account = contextUsers.getCasetype().get(caseTypeName);

        if (StringUtils.isBlank(account)) {
            throw new InvalidContextUser("CaseType " + caseTypeName);
        }

        return getContextUser(account);
    }

    public ContextUser getContextUserForCaseType(Object caseType) {
        return getContextUserForClass(caseType.getClass());
    }

    public ContextUser getDefaultContextUser() {
        return getContextUser(contextUsers.getCasetype().get("default"));
    }

    private ContextUser getContextUser(String account) {
        final ContextUser contextUser = contextUsers.getAccount().get(account);

        if (contextUser == null) {
            throw new InvalidContextUser("Account " + account);
        }

        return contextUser;
    }

    private static String resourceName(Class<?> clazz) {
        return StringUtils.removeEnd(StringUtils.lowerCase(clazz.getSimpleName()), "resource");
    }
}
