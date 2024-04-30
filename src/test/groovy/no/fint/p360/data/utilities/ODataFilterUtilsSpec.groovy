package no.fint.p360.data.utilities

import no.fint.p360.data.exception.IllegalODataFilter
import spock.lang.Specification

class ODataFilterUtilsSpec extends Specification {

    private ODataFilterUtils oDataFilterUtils;

    void setup() {
        oDataFilterUtils = new ODataFilterUtils();
    }

    def "Get mappeid from ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("mappeid eq '24/00027'")

        then:
        "24/00027" == filter.getCaseNumber();
    }

    def "Get systemid from ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("systemid eq '201927'")

        then:
        201927 == filter.getRecno();
    }

    def "Get arkivdel from ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("arkivdel eq '60001'")

        then:
        "60001" == filter.getSubArchive();
    }

    def "Get klassifikasjon from ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("klassifikasjon eq 'C52'")

        then:
        "C52" == filter.getArchiveCode();
    }

    def "Get kontaktid from ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("kontaktid eq '08089312345'")

        then:
        "08089312345" == filter.getContactReferenceNumber();
    }

    def "Get tittel from ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("tittel eq 'Post 74 - Charlie Foxtrot - S/S Den Sorte Dame - 12345'")

        then:
        "Post 74 - Charlie Foxtrot - S/S Den Sorte Dame - 12345" == filter.getTitle();
    }

    def "Get both mappeid and tittel from one magic ODataFiltered query"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("tittel eq 'Charlie Foxtrot - S/S Den Sorte Dame' and mappeid eq '2024/123'")

        then:
        "Charlie Foxtrot - S/S Den Sorte Dame" == filter.getTitle();
        "2024/123" == filter.getCaseNumber();
    }

    def "When unsupported ODataFilter property exception is thrown"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("org eq 'ks dif'")

        then:
        thrown(IllegalODataFilter)
    }

    def "When unsupported ODataFilter operator exception is thrown"() {
        when:
        def filter = oDataFilterUtils.getCasesArgs("fintlabs ne 'ks dif'")

        then:
        thrown(IllegalODataFilter)
    }
}
