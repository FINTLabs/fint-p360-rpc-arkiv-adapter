package no.fint.p360.data.exception;

public class NotTilskuddFredaHusPrivatEieException extends RuntimeException {
    public NotTilskuddFredaHusPrivatEieException(String caseNumber) {
        super(caseNumber);
    }
}
