package no.novari.p360.data.utilities

import no.novari.p360.data.exception.IllegalODataFilter
import spock.lang.Specification

class ODataFilterUtilsSpec extends Specification {

    private ODataFilterUtils oDataFilterUtils = new ODataFilterUtils()

    def "Get property from ODataFiltered query"() {
        setup:
        def filter = oDataFilterUtils.getCasesArgs(query, null)

        expect:
        expected == propertyGetter.call(filter)

        where:
        query                                                                             | expected                               | propertyGetter
        "mappeid eq '24/00027'"                                                           | "24/00027"                             | { it.getCaseNumber() }
        "systemid eq '201927'"                                                            | 201927                                 | { it.getRecno() }
        "arkivdel eq '60001'"                                                             | "recno:60001"                          | { it.getSubArchive() }
        "arkivdel eq 'Opplæring'"                                                         | "Opplæring"                            | { it.getSubArchive() }
        "klassifikasjon/primar/verdi eq 'C52'"                                            | "C52"                                  | { it.getArchiveCode() }
        "klassifikasjon/primar/ordning eq 'ORG' and klassifikasjon/primar/verdi eq '123'" | "123"                                  | { it.getArchiveCode() }
        "kontaktid eq '08089312345'"                                                      | "08089312345"                          | { it.getContactReferenceNumber() }
        "tittel eq 'Post 74 - S/S Den Sorte Dame - 12345'"                                | "Post 74 - S/S Den Sorte Dame - 12345" | { it.getTitle() }
        "tittel eq 'Post 74, Post 44 - Fiskeskøyte'"                                                        | "Post 74, Post 44 - Fiskeskøyte"                           | { it.getTitle() }
        "saksstatus eq '5'"                                                               | "5"                                    | { it.getAdditionalFields().first().getValue() }
    }

    def "Get both mappeid and tittel from one magic ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("tittel eq 'Charlie Foxtrot - S/S Den Sorte Dame' and mappeid eq '2024/123'", null)

        then:
        "Charlie Foxtrot - S/S Den Sorte Dame" == filter.getTitle()
        "2024/123" == filter.getCaseNumber()
    }

    def "When unsupported ODataFilter property exception is thrown"() {
        when:
        oDataFilterUtils.getCasesArgs("org eq 'ks dif'", null)

        then:
        thrown(IllegalODataFilter)
    }

    def "When unsupported ODataFilter operator exception is thrown"() {
        when:
        oDataFilterUtils.getCasesArgs("fintlabs ne 'ks dif'", null)

        then:
        thrown(IllegalODataFilter)
    }
}
