package no.fint.p360.data.utilities;

import lombok.extern.slf4j.Slf4j;
import no.fint.antlr.ODataLexer;
import no.fint.antlr.ODataParser;
import no.fint.p360.data.exception.IllegalODataFilter;
import no.p360.model.CaseService.AdditionalField__1;
import no.p360.model.CaseService.GetCasesArgs;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ODataFilterUtils {

    private final List<String> supportedODataProperties = List.of("mappeid", "tittel", "systemid", "arkivdel",
            "klassifikasjon/primar/verdi", "klassifikasjon/primar/ordning", "kontaktid", "saksstatus", "saksmappetype");

    public GetCasesArgs getCasesArgs(String query, String caseStatusFilter) {
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

        Optional.ofNullable(oDataFilter.get("klassifikasjon/primar/verdi"))
                .ifPresent(getCasesArgs::setArchiveCode);

        Optional.ofNullable(oDataFilter.get("kontaktid"))
                .ifPresent(getCasesArgs::setContactReferenceNumber);

        Optional.ofNullable(oDataFilter.get("saksmappetype"))
                .map(value -> StringUtils.isNumeric(value) ? "recno:" + value : value)
                .ifPresent(getCasesArgs::setCaseType);

        Optional.ofNullable(oDataFilter.get("saksstatus"))
                .or(() -> Optional.ofNullable(caseStatusFilter).filter(StringUtils::isNotBlank))
                .ifPresent(saksstatus -> {
                    AdditionalField__1 additionalField = new AdditionalField__1();
                    additionalField.setName("ToCaseStatus");
                    additionalField.setValue(saksstatus);
                    getCasesArgs.setAdditionalFields(List.of(additionalField));

                    log.debug("We've just used the ToCaseStatus filter feature. Setting value to {}", saksstatus);
                });

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

        if ("arkivdel".equalsIgnoreCase(oDataProperty) && StringUtils.isNumeric(oDataValue)) {
            log.info("Custom P360 h4ck to prefix our OData filter with 'recno:.'");
            oDataValue = "recno:".concat(oDataValue);

            log.debug("The new modified ODatafitler value: {}", oDataValue);
        }

        if (!"eq".equals(oDataOperator)) {
            throw new IllegalODataFilter(String.format("OData operator %s is not supported. Currently only support for 'eq' operator.", oDataOperator));
        }

        if (!supportedODataProperties.contains(oDataProperty)) {
            throw new IllegalODataFilter(String.format("OData property %s is not supported", oDataProperty));
        }

        return Map.entry(oDataProperty, oDataValue);
    }
}
