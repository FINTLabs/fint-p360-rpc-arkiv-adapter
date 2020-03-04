package no.fint.p360.data

import no.fint.p360.data.exception.IllegalCaseNumberFormat
import no.fint.p360.data.exception.IllegalDocumentNumberFormat
import no.fint.p360.data.utilities.NOARKUtils
import spock.lang.Specification

class NOARKUtilsSpec extends Specification {

    def "Get case year from case number"() {
        when:
        def year = NOARKUtils.getCaseYear("17/12345")

        then:
        year == "17"

    }

    def "Get case year from case number - invalid format"() {
        when:
        NOARKUtils.getCaseYear("16-12345")

        then:
        thrown(IllegalCaseNumberFormat)

    }

    def "Get case sequencenumber from case number"() {
        when:
        def sequenceNumber = NOARKUtils.getCaseSequenceNumber("17/12345")

        then:
        sequenceNumber == "12345"

    }

    def "Get case sequencenumber from case number - invalid format"() {
        when:
        NOARKUtils.getCaseYear("16-12345")

        then:
        thrown(IllegalCaseNumberFormat)

    }

    def "Get document sequence number from document number"() {
        when:
        def documentSequencNumber = NOARKUtils.getDocumentSequenceNumber("17/12345-2")

        then:
        documentSequencNumber == "2"

    }

    def "Get document sequence number from document number - invalid format"() {
        when:
        NOARKUtils.getDocumentSequenceNumber("16/12345:5")

        then:
        thrown(IllegalDocumentNumberFormat)

    }
}
