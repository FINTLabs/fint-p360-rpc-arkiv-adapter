package no.fint.p360.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import spock.lang.Specification

class ModelDeserializationSpec extends Specification {

    def "Tilskudd fartøy"() {

        given:
        def om = new ObjectMapper()
        def json = "{\n" +
                "\t\"_links\": {\n" +
                "    \"saksansvarlig\": [\n" +
                "      {\n" +
                "        \"href\": \"string\"\n" +
                "      }\n" +
                "    ],\n" +
                "\t\t\"saksstatus\": [\n" +
                "      {\n" +
                "        \"href\": \"string\"\n" +
                "      }\n" +
                "    ],\n" +
                "\t\t\"administrativEnhet\": [\n" +
                "      {\n" +
                "        \"href\": \"string\"\n" +
                "      }\n" +
                "    ],\n" +
                "\t\t\"opprettetAv\": [\n" +
                "      {\n" +
                "        \"href\": \"string\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "    \"avsluttetDato\": \"2019-03-11T19:58:27.841Z\",\n" +
                "  \"beskrivelse\": \"string\",\n" +
                "  \"kallesignal\": \"string\",\n" +
                "  \"kulturminneId\": \"string\",\n" +
                "  \"mappeId\": {\n" +
                "    \"gyldighetsperiode\": {\n" +
                "      \"beskrivelse\": \"string\",\n" +
                "      \"slutt\": \"2019-03-11T19:58:27.841Z\",\n" +
                "      \"start\": \"2019-03-11T19:58:27.841Z\"\n" +
                "    },\n" +
                "    \"identifikatorverdi\": \"string\"\n" +
                "  },\n" +
                "  \"noekkelord\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"offentligTittel\": \"string\",\n" +
                "  \"opprettetDato\": \"2019-03-11T19:58:27.841Z\",\n" +
                "  \"saksaar\": \"string\",\n" +
                "  \"saksdato\": \"2019-03-11T19:58:27.841Z\",\n" +
                "  \"sakssekvensnummer\": \"string\",\n" +
                "  \"soknadsnummer\": {\n" +
                "    \"gyldighetsperiode\": {\n" +
                "      \"beskrivelse\": \"string\",\n" +
                "      \"slutt\": \"2019-03-11T19:58:27.841Z\",\n" +
                "      \"start\": \"2019-03-11T19:58:27.841Z\"\n" +
                "    },\n" +
                "    \"identifikatorverdi\": \"string\"\n" +
                "  },\n" +
                "  \"systemId\": {\n" +
                "    \"gyldighetsperiode\": {\n" +
                "      \"beskrivelse\": \"string\",\n" +
                "      \"slutt\": \"2019-03-11T19:58:27.841Z\",\n" +
                "      \"start\": \"2019-03-11T19:58:27.841Z\"\n" +
                "    },\n" +
                "    \"identifikatorverdi\": \"string\"\n" +
                "  },\n" +
                "  \"tittel\": \"Test igjen FINT 11\",\n" +
                "  \"utlaantDato\": \"2019-03-11T19:58:27.841Z\"\n" +
                "}"

        when:
        def result = om.readValue(json, TilskuddFartoyResource)

        then:
        result
    }
}
