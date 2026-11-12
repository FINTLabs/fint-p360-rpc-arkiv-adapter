package no.novari.fint.p360.handler.noark

import no.novari.fint.model.arkiv.noark.NoarkActions
import no.novari.fint.p360.data.noark.arkivressurs.ArkivressursService
import no.novari.fint.p360.handler.Handler
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import spock.lang.Specification

class GetArkivressursHandlerSpec extends Specification {

    def "arkivressurs is enabled only when explicitly configured: #properties"() {
        given:
        def service = Mock(ArkivressursService)
        def runner = new ApplicationContextRunner()
                .withUserConfiguration(GetArkivressursHandler)
                .withInitializer { context -> context.beanFactory.registerSingleton('arkivressursService', service) }
                .withPropertyValues(properties as String[])

        expect:
        runner.run { context ->
            assert context.startupFailure == null
            assert context.containsBean('getArkivressursHandler') == enabled
            def actions = context.getBeansOfType(Handler).values().collectMany { it.actions() as List }
            assert actions.contains(NoarkActions.GET_ALL_ARKIVRESSURS.name()) == enabled
        }

        where:
        properties                           | enabled
        []                                   | false
        ['fint.arkiv.arkivressurs=false']      | false
        ['fint.arkiv.arkivressurs=true']       | true
    }
}
