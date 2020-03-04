package no.fint.p360.data.utilities;

import com.google.common.base.Strings;
import no.fint.model.felles.kodeverk.Kommune;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource;
import no.fint.p360.data.exception.UnableToParseMatrikkel;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MatrikkelParser {

    public static final int KOMMUNENUMMER = 0;
    public static final int GARDSNUMMER = 1;
    public static final int BRUKSNUMMER = 2;
    public static final int FESTENUMMER = 3;
    public static final int SEKSJONSNUMMER = 4;

    public static MatrikkelnummerResource parse(String s) throws UnableToParseMatrikkel {
        AtomicInteger position = new AtomicInteger(0);
        Map<Integer, String> matrikkelMap = Arrays.stream(s.split("[/\\-]")).collect(Collectors.toMap(i -> position.getAndIncrement(), i -> i));

        if (matrikkelMap.size() < 3 || matrikkelMap.size() > 5 || !s.contains("-") || !s.contains("/")) {
            throw new UnableToParseMatrikkel(s);
        }

        MatrikkelnummerResource matrikkelnummerResource = new MatrikkelnummerResource();
        matrikkelnummerResource.setBruksnummer(matrikkelMap.get(BRUKSNUMMER));
        matrikkelnummerResource.setGardsnummer(matrikkelMap.get(GARDSNUMMER));
        matrikkelnummerResource.addKommunenummer(Link.with(Kommune.class, "systemid", matrikkelMap.get(KOMMUNENUMMER)));


        if (matrikkelMap.size() == 4) {
            matrikkelnummerResource.setFestenummer(matrikkelMap.get(FESTENUMMER));
        }

        if (matrikkelMap.size() == 5) {
            matrikkelnummerResource.setFestenummer(matrikkelMap.get(FESTENUMMER));
            matrikkelnummerResource.setSeksjonsnummer(matrikkelMap.get(SEKSJONSNUMMER));
        }

        return matrikkelnummerResource;

    }

    public static String toString(MatrikkelnummerResource matrikkelnummer) {
        List<String> strings = Arrays.asList(matrikkelnummer.getKommunenummer().get(0).getHref().split("/"));
        String kommunenummer = strings.get(strings.size() - 1);
        return StringUtils.trimTrailingCharacter(String.format("%s-%s/%s/%s/%s",
                kommunenummer,
                matrikkelnummer.getGardsnummer(),
                matrikkelnummer.getBruksnummer(),
                Strings.nullToEmpty(matrikkelnummer.getFestenummer()),
                Strings.nullToEmpty(matrikkelnummer.getSeksjonsnummer())
        ), '/');
    }
}
