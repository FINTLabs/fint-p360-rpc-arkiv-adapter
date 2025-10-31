package no.novari.p360.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.novari.p360.data.p360.CaseService;
import no.novari.p360.data.utilities.QueryUtils;
import no.novari.p360.model.FilterSet;
import no.p360.model.CaseService.Case;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Service
@Slf4j
public class CaseQueryService {

    private final ImmutableMap<String, BiFunction<FilterSet, String, Stream<Case>>> queryMap;
    private final String[] validQueries;

    @Autowired
    private CaseService caseService;

    public CaseQueryService() {
        queryMap = new ImmutableMap.Builder<String, BiFunction<FilterSet, String, Stream<Case>>>()
                .put("soknadsnummer/", this::finnSaksmapperGittEksternNokkel)
                .put("mappeid/", this::finnSaksmapperGittSaksnummer)
                .put("systemid/", this::finnSaksmapperGittSystemId)
                .put("$filter=", this::finnSaksmapperGittODataFilter)
                .put("?", this::finnSaksmapperGittTittel)
                .build();
        validQueries = queryMap.keySet().toArray(new String[0]);
    }

    public boolean isValidQuery(String query) {
        return StringUtils.startsWithAny(StringUtils.lowerCase(query), validQueries);
    }

    public Stream<Case> query(FilterSet filterSet, String query) {
        for (String prefix : validQueries) {
            if (StringUtils.startsWithIgnoreCase(query, prefix)) {
                return queryMap.get(prefix).apply(filterSet, StringUtils.removeStartIgnoreCase(query, prefix));
            }
        }
        throw new IllegalArgumentException("Invalid query: " + query);
    }

    public Stream<Case> finnSaksmapperGittSystemId(FilterSet filterSet, String id) {
        return Stream.of(caseService.getCaseBySystemId(filterSet, id));
    }

    public Stream<Case> finnSaksmapperGittSaksnummer(FilterSet filterSet, String saksnummer) {
        return Stream.of(caseService.getCaseByCaseNumber(filterSet, saksnummer));
    }

    @Deprecated
    public Stream<Case> finnSaksmapperGittTittel(FilterSet filterSet, String query) {
        log.warn("..so you want to use this old deprecated stuff ({})?! We recommend the new fancy OData way.", query);

        final Map<String, String> params = QueryUtils.getQueryParams("?" + query);
        String title = params.get("title");
        String maxResult = params.getOrDefault("maxResult", "10");
        return caseService.getCasesByTitle(filterSet, title, maxResult).stream();
    }

    public Stream<Case> finnSaksmapperGittEksternNokkel(FilterSet filterSet, String noekkel) {
        return Stream.of(caseService.getCaseByExternalId(filterSet, noekkel));
    }

    public Stream<Case> finnSaksmapperGittODataFilter(FilterSet filterSet, String query) {
        log.debug("The Odata filtered case query, proudly present to you by Paperboiz: " + query);

        return caseService.getCaseByODataFilter(filterSet, query).stream();
    }
}
