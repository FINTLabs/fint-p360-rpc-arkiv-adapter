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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ODataFilterUtils {

    private final List<String> supportedODataProperties = List.of("mappeid", "tittel", "systemid", "arkivdel",
            "klassifikasjon", "kontaktid");

    public GetCasesArgs getCasesArgs(String query) {
        GetCasesArgs getCasesArgs = new GetCasesArgs();

        Map<String, String> oDataFilter = parseQuery(query);

        Optional.ofNullable(oDataFilter.get("mappeid"))
                .ifPresent(getCasesArgs::setCaseNumber);

        Optional.ofNullable(oDataFilter.get("systemid"))
                .map(Integer::valueOf)
                .ifPresent(getCasesArgs::setRecno);

        Optional.ofNullable(oDataFilter.get("tittel"))
                .ifPresent(getCasesArgs::setTitle);

        Optional.ofNullable(oDataFilter.get("arkivdel"))
                .ifPresent(getCasesArgs::setSubArchive);

        Optional.ofNullable(oDataFilter.get("klassifikasjon"))
                .ifPresent(getCasesArgs::setArchiveCode);

        Optional.ofNullable(oDataFilter.get("kontaktid"))
                .ifPresent(getCasesArgs::setContactReferenceNumber);

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
