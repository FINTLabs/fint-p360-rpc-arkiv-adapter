package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.ContextUsers;
import no.fint.p360.FilterSets;
import no.fint.p360.data.exception.InvalidContextUser;
import no.fint.p360.data.exception.InvalidFilterSet;
import no.fint.p360.model.ContextUser;
import no.fint.p360.model.FilterSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FilterSetService {

    private final FilterSets filterSets;

    public FilterSetService(FilterSets filterSets) {
        this.filterSets = filterSets;

        if (filterSets.getIntegration() == null) {
            log.warn("No configured P360 Filtersets! No worries, we'll continue as in the good old days.");
        } else {
            log.info("Configured P360 Filtersets: {}", filterSets);
        }
    }

    public FilterSet getFilterSetForCaseType(Object caseType) {
        return getFilterSetForClass(caseType.getClass());
    }

    public FilterSet getDefaultFilterSet() {
        return getFilterSet(filterSets.getCasetype().get("default"));
    }

    private FilterSet getFilterSetForClass(Class<?> clazz) {
        if (filterSets.getCasetype() != null) {
            String caseTypeName = resourceName(clazz);
            final String integration = filterSets.getCasetype().get(caseTypeName);

            if (StringUtils.isBlank(integration)) {
                throw new InvalidFilterSet("CaseType " + caseTypeName);
            }

            return getFilterSet(integration);
        }

        return null;
    }

    private FilterSet getFilterSet(String integration) {
        if (filterSets.getIntegration() != null) {
            final FilterSet filterSet = filterSets.getIntegration().get(integration);

            if (filterSet == null) {
                throw new InvalidFilterSet("Integration " + integration);
            }

            return filterSet;
        }

        return null;
    }

    private static String resourceName(Class<?> clazz) {
        return StringUtils.removeEnd(StringUtils.lowerCase(clazz.getSimpleName()), "resource");
    }
}
