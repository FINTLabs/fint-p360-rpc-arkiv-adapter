package no.fint.p360.service

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import no.fint.p360.TitleFormats
import spock.lang.Specification

class TitleServiceSpec extends Specification {
    TitleService titleService

    void setup() {
        titleService = new TitleService(new TitleFormats(format: [
                'tilskuddfartoy': '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}'
        ]))
    }

    def "Format title from object"() {
        given:
        def r = new TilskuddFartoyResource(
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                fartoyNavn: 'Hestmann',
                kallesignal: 'XXYYZ',
                kulturminneId: '22334455-1'
        )

        when:
        def t = titleService.getTitle(r)
        println(t)

        then:
        t == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
    }

    def "Set values from title"() {
        given:
        def t = 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseTitle(r, t)
        println(r)

        then:
        r.fartoyNavn == 'Hestmann'
        r.soknadsnummer.identifikatorverdi == '12345'
    }

    def "Invalid title format"() {
        given:
        def t = 'Tilskudd - LDQT - Gamle Lofotferga - 139136-1 - 14812 - 14812 - TEST integrasjon FINT fartøy'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseTitle(r, t)
        println(r)

        then:
        noExceptionThrown()
    }

    def 'Valid title format'() {
        given:
        def t = 'LDQT - Gamle Lofotferga - Tilskudd - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseTitle(r, t)
        println(r)

        then:
        r.fartoyNavn == 'Gamle Lofotferga'
        r.kallesignal == 'LDQT'
        r.soknadsnummer.identifikatorverdi == '14812'
    }
}
