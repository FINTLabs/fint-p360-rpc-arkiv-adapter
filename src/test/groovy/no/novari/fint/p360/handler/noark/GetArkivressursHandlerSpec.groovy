package no.novari.fint.p360.handler.noark

import no.novari.fint.model.arkiv.noark.NoarkActions
import no.novari.fint.p360.client.FintClient
import no.novari.fint.p360.data.noark.arkivressurs.ArkivressursFactory
import no.novari.fint.p360.data.noark.arkivressurs.ArkivressursService
import no.novari.fint.p360.data.p360.ContactService
import no.novari.fint.p360.handler.Handler
import no.novari.fint.p360.repository.PersonalressursRepository
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import reactor.core.publisher.Mono
import spock.lang.Specification

class GetArkivressursHandlerSpec extends Specification {

    def "arkivressurs is enabled only when explicitly configured: #properties"() {
        given:
        def fintClient = Mock(FintClient)
        def contactService = Mock(ContactService)
        def runner = new ApplicationContextRunner()
                .withUserConfiguration(GetArkivressursHandler, ArkivressursService,
                        ArkivressursFactory, PersonalressursRepository)
                .withInitializer { context ->
                    context.beanFactory.registerSingleton('fintClient', fintClient)
                    context.beanFactory.registerSingleton('contactService', contactService)
                }
                .withPropertyValues(properties as String[])

        when:
        runner.run { context ->
            assert context.startupFailure == null
            assert context.containsBean('getArkivressursHandler') == enabled
            assert context.containsBean('arkivressursService') == enabled
            assert context.containsBean('arkivressursFactory') == enabled
            assert context.containsBean('personalressursRepository') == enabled
            def actions = context.getBeansOfType(Handler).values().collectMany { it.actions() as List }
            assert actions.contains(NoarkActions.GET_ALL_ARKIVRESSURS.name()) == enabled
        }

        then:
        (enabled ? 1 : 0) * fintClient.getPersonalressursResources() >> Mono.just([])

        where:
        properties                           | enabled
        []                                   | false
        ['fint.arkiv.arkivressurs=false']      | false
        ['fint.arkiv.arkivressurs=true']       | true
    }
}
