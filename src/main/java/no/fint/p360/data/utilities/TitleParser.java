package no.fint.p360.data.utilities;

import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.exception.NoSuchTitleDimension;
import no.fint.p360.data.exception.UnableToParseTitle;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TitleParser {

    public static final int FARTOY_KALLESIGNAL = 1;
    public static final int FARTOY_NAVN = 2;
    public static final int MATRIKKELNUMMER = 1;
    public static final int KULTURMINNE_ID = 3;
    public static final int DIGISAKSNUMMER = 4;
    public static final int USER_DEFINED_TITLE = 4;


    public static class Title {
        Map<Integer, String> titleMap;

        public Title(Map<Integer, String> map) {
            titleMap = map;
        }

        public String getDimension(Integer dimension) throws NoSuchTitleDimension {
            if (titleMap.containsKey(dimension)) {
                return titleMap.get(dimension);
            }
            throw new NoSuchTitleDimension(dimension);
        }
    }

    public static Title parseTitle(String caseTitle) throws UnableToParseTitle {
        AtomicInteger position = new AtomicInteger(0);
        Map<Integer, String> titleMap = Arrays.stream(caseTitle.split(" - "))
                .collect(Collectors.toMap(i -> position.getAndIncrement(), String::trim));

        if (titleMap.size() <= 1) {
            throw new UnableToParseTitle(String.format("Unable to parse title: %s", caseTitle));
        }

        return new Title(titleMap);
    }

    // TODO: 2019-05-09 Vi må finne ut hvordan denne skal være
    public static String getTitleString(TilskuddFartoyResource tilskuddFartoy) {
        return String.format("Tilskudd - %s - %s - %s - %s - %s",
                tilskuddFartoy.getKallesignal(),
                tilskuddFartoy.getFartoyNavn(),
                tilskuddFartoy.getKulturminneId(),
                tilskuddFartoy.getSoknadsnummer().getIdentifikatorverdi(),
                tilskuddFartoy.getTittel());
    }

    public static String getTitleString(DispensasjonAutomatiskFredaKulturminneResource dispensasjonAutomatiskFredaKulturminne) {
        return String.format("Dispensasjon - %s - %s - %s - %s ",
                MatrikkelParser.toString(dispensasjonAutomatiskFredaKulturminne.getMatrikkelnummer()),
                dispensasjonAutomatiskFredaKulturminne.getKulturminneId(),
                dispensasjonAutomatiskFredaKulturminne.getSoknadsnummer().getIdentifikatorverdi(),
                dispensasjonAutomatiskFredaKulturminne.getTittel());
    }


}
