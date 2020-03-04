package no.fint.p360.data.utilities;

import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.exception.IllegalDocumentNumberFormat;

public enum NOARKUtils {
    ;

    public static String getCaseYear(String caseNumber) throws IllegalCaseNumberFormat {
        String[] split = caseNumber.split("/");
        if (split.length != 2) {
            throw new IllegalCaseNumberFormat(String.format("Case number %s is illegal", caseNumber));
        }
        return split[0];
    }

    public static String getCaseSequenceNumber(String caseNumber) throws IllegalCaseNumberFormat {
        String[] split = caseNumber.split("/");
        if (split.length != 2) {
            throw new IllegalCaseNumberFormat(String.format("Case number %s is illegal", caseNumber));
        }
        return split[1];
    }

    public static String getDocumentSequenceNumber(String documentNumber) throws IllegalDocumentNumberFormat {
        String[] split = documentNumber.split("-");
        if (split.length != 2) {
            throw new IllegalDocumentNumberFormat(String.format("Document number %s is illegal", documentNumber));
        }
        return split[1];
    }
}
