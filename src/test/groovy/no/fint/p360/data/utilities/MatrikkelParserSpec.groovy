package no.fint.p360.data.utilities

import no.fint.model.felles.kodeverk.Kommune
import no.fint.model.resource.Link
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource
import no.fint.p360.data.exception.UnableToParseMatrikkel
import spock.lang.Specification


class MatrikkelParserSpec extends Specification {

    def "Matrikkelresurce with everything set to string"() {
        given:
        def resource = new MatrikkelnummerResource()
        resource.setBruksnummer("12")
        resource.setGardsnummer("123")
        resource.setFestenummer("1")
        resource.setSeksjonsnummer("1")
        resource.addKommunenummer(Link.with(Kommune.class, "systemid", "1108"))


        when:
        def string = MatrikkelParser.toString(resource)

        then:
        string == "1108-123/12/1/1"
    }

    def "Matrikkelresurce with minimum set to string"() {
        given:
        def resource = new MatrikkelnummerResource()
        resource.setBruksnummer("12")
        resource.setGardsnummer("123")
        resource.addKommunenummer(Link.with(Kommune.class, "systemid", "1108"))


        when:
        def string = MatrikkelParser.toString(resource)

        then:
        string == "1108-123/12"
    }

    def "Parse matrikkel string"() {
        when:
        def matrikkel = MatrikkelParser.parse("1108-123/12")

        then:
        matrikkel.getBruksnummer() == "12"
        noExceptionThrown()
    }

    def "Unable to parse matrikkel string"() {
        when:
        MatrikkelParser.parse("1108/123/12/1/1")

        then:
        thrown UnableToParseMatrikkel
    }
}
