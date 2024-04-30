package no.fint.p360.data.utilities;

import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.ODataLexer;
import no.fint.antlr.ODataParser;
import no.fint.p360.data.exception.IllegalODataFilter;
import no.p360.model.CaseService.GetCasesArgs;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ODataFilterUtils {

    private final List<String> supportedODataProperties = List.of("mappeid", "tittel", "systemid", "arkivdel",
            "klassifikasjon", "kontaktid");

    public GetCasesArgs getCasesArgs(String query) {
        GetCasesArgs getCasesArgs = new GetCasesArgs();

        Map<String, String> oDataFilter = parseQuery(query);

        if (oDataFilter.containsKey("mappeid")) {
            getCasesArgs.setCaseNumber(oDataFilter.get("mappeid"));
        }

        if (oDataFilter.containsKey("systemid")) {
            getCasesArgs.setRecno(Integer.valueOf(oDataFilter.get("systemid")));
        }

        if (oDataFilter.containsKey("tittel")) {
            getCasesArgs.setTitle(oDataFilter.get("tittel"));
        }

        if (oDataFilter.containsKey("arkivdel")) {
            getCasesArgs.setSubArchive(oDataFilter.get("arkivdel"));
        }

        if (oDataFilter.containsKey("klassifikasjon")) {
            getCasesArgs.setArchiveCode(oDataFilter.get("klassifikasjon"));
        }

        if (oDataFilter.containsKey("kontaktid")) {
            getCasesArgs.setContactReferenceNumber(oDataFilter.get("kontaktid"));
        }

        return getCasesArgs;
    }

    private Map<String, String> parseQuery(String query) throws IllegalODataFilter {
        ODataLexer lexer = new ODataLexer(CharStreams.fromString(query));
        CommonTokenStream commonTokens = new CommonTokenStream(lexer);
        ODataParser oDataParser = new ODataParser(commonTokens);

        return oDataParser.filter().comparison().stream()
                .map(this::fromODataToP360CaseArgs)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, String> fromODataToP360CaseArgs(ODataParser.ComparisonContext context) throws IllegalODataFilter {
        String oDataProperty = context.property().getText();
        String oDataOperator = context.comparisonOperator().getText();
        String oDataValue = context.value().getText().replaceAll("'", "");

        if (!"eq".equals(oDataOperator)) {
            throw new IllegalODataFilter(String.format("OData operator %s is not supported. Currently only support for 'eq' operator.", oDataOperator));
        }

        if (!supportedODataProperties.contains(oDataProperty)) {
            throw new IllegalODataFilter(String.format("OData property %s is not supported", oDataProperty));
        }

        return Map.entry(oDataProperty, oDataValue);
    }
}
