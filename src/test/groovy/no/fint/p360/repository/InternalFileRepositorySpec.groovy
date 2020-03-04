package no.fint.p360.repository

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.event.model.Event
import no.fint.model.resource.FintLinks
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource
import no.fint.p360.data.utilities.FintUtils
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class InternalFileRepositorySpec extends Specification {

    private InternalFileRepository fileRepository
    private DokumentfilResource dokumentfilResource
    private Path path

    def setup() {
        path = Files.createTempDirectory(Paths.get('build'), 'internal-files')
        dokumentfilResource = new DokumentfilResource()
        dokumentfilResource.setData("Dette er en test")
        dokumentfilResource.setFormat("text/plain")
        dokumentfilResource.setSystemId(FintUtils.createIdentifikator("1"))

        fileRepository = new InternalFileRepository(rootDirectory: path, objectMapper: new ObjectMapper())
    }
    def "Save file to local file system"() {

        when:
        fileRepository.putFile(new Event<FintLinks>(), dokumentfilResource)

        then:
        Files.list(path).anyMatch(Files.&isRegularFile)

    }

    def "Read file from local file system"() {
        given:
        fileRepository.putFile(new Event<FintLinks>(), dokumentfilResource)
        def id = dokumentfilResource.systemId.identifikatorverdi

        when:
        def file = fileRepository.getFile(id)

        then:
        file.data == "Dette er en test"
        file.systemId.identifikatorverdi == id
    }

    def 'Base64 decoding of empty string'() {
        given:
        def input = ''

        when:
        def result = Base64.encoder.encode(input.getBytes())
        println(result)

        then:
        result.length == 0

        when:
        def res2 = Base64.decoder.decode(input)
        println(res2)

        then:
        res2.length == 0
    }

}
