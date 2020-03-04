package no.fint.p360.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.LoadingCache
import no.fint.p360.AdapterProps
import org.springframework.http.MediaType
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CachedFileServiceSpec extends Specification {

    private CachedFileService cachedFileService
    private LoadingCache<String, Path> cache
    private Path path

    void setup() {
        path = Files.createTempDirectory(Paths.get('build'), 'filerepo')
        cache = Mock()
        cachedFileService = new CachedFileService(files: cache, objectMapper: new ObjectMapper(), props: new AdapterProps(p360User: "RA/TEST", p360Password: "topsecret", cacheDirectory: path))
    }

    def "Get content type from format (file extension)"() {

        when:
        def contentType = cachedFileService.getContentType("PDF")

        then:
        contentType == MediaType.APPLICATION_PDF_VALUE


    }

    def "Scan for files in cache"() {
        given:
        Files.createFile(path.resolve("1.json"))

        when:
        cachedFileService.scan()

        then:
        1 * cache.put('1', _ as Path)
    }
}
