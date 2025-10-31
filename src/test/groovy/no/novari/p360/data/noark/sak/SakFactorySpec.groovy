package no.novari.p360.data.noark.sak

import no.novari.fint.arkiv.AdditionalFieldService
import no.novari.fint.arkiv.SubstitutorService
import no.novari.fint.arkiv.TitleService
import no.fint.model.resource.arkiv.noark.SakResource
import no.novari.p360.data.noark.common.NoarkFactory
import no.novari.p360.data.noark.journalpost.JournalpostFactory
import no.novari.p360.service.CaseQueryService
import no.novari.p360.service.ContextUserService
import no.novari.p360.service.FilterSetService
import spock.lang.Specification

import java.util.stream.Stream

class SakFactorySpec extends Specification
{

    def additionalFieldService = Mock(AdditionalFieldService)
    def sakFactory = new SakFactory(
            new NoarkFactory(
                    titleService: new TitleService(Mock(SubstitutorService)),
                    additionalFieldService: additionalFieldService,
                    contextUserService: Mock(ContextUserService),
            ),
            Mock(FilterSetService),
            Mock(JournalpostFactory),
            Mock(CaseQueryService)
    )

    void setup() {
        additionalFieldService.getFieldsForResource(_, _) >> Stream.empty()
    }

    def "createCaseArgs should set title and publicTitle correctly when offentligTittel is set"(){
        given:
        def sakResource = new SakResource(
                tittel: "Elevmappe - Kari Nordmann",
                offentligTittel: "Elevmappe",
        )

        when:
        def result = sakFactory.convertToCreateCase(sakResource)

        then:
        result.getTitle() == "Elevmappe"
        result.getUnofficialTitle() == "Elevmappe - Kari Nordmann"
    }

    def "createCaseArgs should set title and publicTitle correctly when only tittel is set"(){
        given:
        def sakResource = new SakResource(
                tittel: "Elevmappe - Kari Nordmann",
        )

        when:
        def result = sakFactory.convertToCreateCase(sakResource)

        then:
        result.getTitle() == "Elevmappe - Kari Nordmann"
        result.getUnofficialTitle() == null // after case is created in p360, unofficalTitle will be the same as title
    }

    // this should not be possible?
    def "createCaseArgs should set title and publicTitle correctly when only unofficialTitle is set"(){
        given:
        def sakResource = new SakResource(
                offentligTittel: "Elevmappe",
        )

        when:
        def result = sakFactory.convertToCreateCase(sakResource)

        then:
        result.getTitle() == "Elevmappe"
        result.getUnofficialTitle() == null // after case is created in p360, unofficalTitle will be the same as title
    }
}
