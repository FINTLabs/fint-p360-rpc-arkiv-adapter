package no.fint.p360.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.p360.CaseService;
import no.fint.p360.data.utilities.QueryUtils;
import no.p360.model.CaseService.Case;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@Slf4j
public class CaseQueryService {

    private final ImmutableMap<String, Function<String, Stream<Case>>> queryMap;
    private final String[] validQueries;

    @Autowired
    private CaseService caseService;

    public CaseQueryService() {
        queryMap = new ImmutableMap.Builder<String, Function<String, Stream<Case>>>()
                .put("soknadsnummer/", this::finnSaksmapperGittEksternNokkel)
                .put("mappeid/", this::finnSaksmapperGittSaksnummer)
                .put("systemid/", this::finnSaksmapperGittSystemId)
                .put("?", this::finnSaksmapperGittTittel)
                .build();
        validQueries = queryMap.keySet().toArray(new String[0]);
    }

    public boolean isValidQuery(String query) {
        return StringUtils.startsWithAny(StringUtils.lowerCase(query), validQueries);
    }

    public Stream<Case> query(String query) {
        for (String prefix : validQueries) {
            if (StringUtils.startsWithIgnoreCase(query, prefix)) {
                return queryMap.get(prefix).apply(StringUtils.removeStartIgnoreCase(query, prefix));
            }
        }
        throw new IllegalArgumentException("Invalid query: " + query);
    }

    public Stream<Case> finnSaksmapperGittSystemId(String id) {
        return Stream.of(caseService.getCaseBySystemId(id));
    }

    public Stream<Case> finnSaksmapperGittSaksnummer(String saksnummer) {
        return Stream.of(caseService.getCaseByCaseNumber(saksnummer));
    }

    public Stream<Case> finnSaksmapperGittTittel(String query) {
        final Map<String, String> params = QueryUtils.getQueryParams("?" + query);
        String title = params.get("title");
        String maxResult = params.getOrDefault("maxResult", "10");
        return caseService.getCasesByTitle(title, maxResult).stream();
    }

    public Stream<Case> finnSaksmapperGittEksternNokkel(String noekkel) {
        return Stream.of(caseService.getCaseByExternalId(noekkel));
    }
}
