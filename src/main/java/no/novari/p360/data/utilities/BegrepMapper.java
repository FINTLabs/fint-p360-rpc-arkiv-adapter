package no.novari.p360.data.utilities;

import no.fint.model.felles.basisklasser.Begrep;
import no.p360.model.SupportService.CodeTableRow;

import java.util.function.Function;
import java.util.function.Supplier;

public class BegrepMapper {
    public static <T extends Begrep> Function<CodeTableRow, T> mapValue(Supplier<T> constructor) {
        return value -> {
            T result = constructor.get();
            result.setSystemId(FintUtils.createIdentifikator(value.getRecno().toString()));
            result.setKode(value.getCode());
            result.setNavn(value.getDescription());
            return result;
        };
    }
}
