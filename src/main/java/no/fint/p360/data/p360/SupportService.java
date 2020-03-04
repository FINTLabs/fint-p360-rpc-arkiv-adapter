package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.data.exception.CodeTableNotFound;
import no.fint.p360.data.utilities.FintUtils;
import no.p360.model.SupportService.CodeTableRow;
import no.p360.model.SupportService.GetCodeTableRowsArgs;
import no.p360.model.SupportService.GetCodeTableRowsResponse;
import no.p360.model.SupportService.Parameter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class SupportService extends P360Service {

    public GetCodeTableRowsResponse getCodeTable(String table) throws CodeTableNotFound {
        GetCodeTableRowsArgs getCodeTableRowsArgs = new GetCodeTableRowsArgs();
        Parameter parameter = new Parameter();
        parameter.setCodeTableName(table);
        getCodeTableRowsArgs.setParameter(parameter);
        GetCodeTableRowsResponse getCodeTableRowsResponse = call("SupportService/GetCodeTableRows", getCodeTableRowsArgs, GetCodeTableRowsResponse.class);
        if (getCodeTableRowsResponse.getSuccessful()) {
            return getCodeTableRowsResponse;
        }
        throw new CodeTableNotFound(String.format("Could not find %s", table));
    }

    public Stream<CodeTableRow> getCodeTableRowResultStream(String table) {
        GetCodeTableRowsResponse codeTable = getCodeTable(table);
        return FintUtils.optionalValue(codeTable.getCodeTableRows())
                .map(List::stream)
                .orElseThrow(() -> new CodeTableNotFound(table));
    }

    public boolean ping()  {
        return getHealth("SupportService/Ping");
    }
}
