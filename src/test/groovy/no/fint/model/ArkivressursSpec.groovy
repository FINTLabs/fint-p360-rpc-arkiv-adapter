package no.fint.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import no.novari.fint.model.administrasjon.personal.Personalressurs
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource
import spock.lang.Specification

class ArkivressursSpec extends Specification {

    def 'Serialize Arkivressurs'() {
        given:
        def objectMapper = new ObjectMapper()
        def arkivressurs = new ArkivressursResource(
                kildesystemId: new Identifikator(identifikatorverdi: 'USER-mbank-4331')
        )
        arkivressurs.addPersonalressurs(Link.with(Personalressurs, 'ansattnummer', '7284'))

        when:
        def result = objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES).writeValueAsString(arkivressurs)
        println(result)

        then:
        result
    }
}
