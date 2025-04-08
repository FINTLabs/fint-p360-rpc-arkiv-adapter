package no.novari.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.FintMainObject;
import no.novari.p360.FilterSets;
import no.novari.p360.data.exception.InvalidFilterSet;
import no.novari.p360.model.FilterSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FilterSetService {

    private final FilterSet legacyFilterSet;
    private final FilterSets filterSets;

    public FilterSetService(FilterSets filterSets,
                            @Value("${fint.p360.clientid:}") String p360ClientId,
                            @Value("${fint.p360.authkey:}") String p360AuthKey) {
        this.filterSets = filterSets;

        if (filterSets.getIntegration() != null && filterSets.getCasetype() != null) {
            log.info("Configured P360 Filtersets and CaseTypes: {} {}",
                    filterSets.getIntegration().keySet(),
                    filterSets.getCasetype());
            legacyFilterSet = null;
        } else if (StringUtils.isBlank(p360AuthKey) || StringUtils.isBlank(p360ClientId)) {
            log.error("Invalid P360 auth configuration! No FilterSets, and no legacy auth key!");
            throw new IllegalStateException("Invalid P360 Auth Configuration");
        } else {
            legacyFilterSet = new FilterSet(p360AuthKey, p360ClientId);
            log.warn("No configured P360 Filtersets! No worries, we'll continue as in the good old days.");
        }
    }

    private static String resourceName(Class<?> clazz) {
        return StringUtils.removeEnd(StringUtils.lowerCase(clazz.getSimpleName()), "resource");
    }

    public FilterSet getFilterSetForCaseType(Class<? extends FintMainObject> caseType) {
        if (legacyFilterSet != null) {
            return legacyFilterSet;
        }
        return getFilterSetForClass(caseType);
    }

    public FilterSet getDefaultFilterSet() {
        if (legacyFilterSet != null) {
            return legacyFilterSet;
        }
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
}
