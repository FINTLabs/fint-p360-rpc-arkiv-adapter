package no.fint.p360.repository;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fint.p360.data.utilities.FintUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Repository
@Slf4j
@ConditionalOnProperty(name = "fint.internal-files.type", havingValue = "BLOB")
public class AzureBlobRepository extends InternalRepository {
    @Value("${fint.internal-files.connection-string}")
    private String connectionString;

    @Value("${fint.internal-files.container-name}")
    private String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient blobContainerClient;

    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
        log.info("Connected to {}", blobServiceClient.getAccountName());
    }

    @Override
    public void putFile(Event<FintLinks> event, DokumentfilResource resource) throws IOException {
        String systemId = getNextSystemId();
        resource.setSystemId(FintUtils.createIdentifikator(systemId));
        BlobClient blobClient = blobContainerClient.getBlobClient(systemId);
        try (BlobOutputStream outputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {
            outputStream.write(Base64.getDecoder().decode(resource.getData()));
        }
        blobClient.setMetadata(ImmutableMap.<String, String>builder()
                .put("format", resource.getFormat())
                .put("filename", URLEncoder.encode(resource.getFilnavn(), StandardCharsets.UTF_8.name()))
                .put("date", LocalDateTime.now().toString())
                .put("orgId", event.getOrgId())
                .put("client", event.getClient())
                .put("corrId", event.getCorrId())
                .build());
    }

    @Override
    public DokumentfilResource getFile(String recNo) throws IOException {
        try {
            return silentGetFile(recNo);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public DokumentfilResource silentGetFile(String recNo) {
        BlobClient blobClient = blobContainerClient.getBlobClient(recNo);
        DokumentfilResource resource = new DokumentfilResource();
        resource.setSystemId(FintUtils.createIdentifikator(recNo));
        Map<String, String> metadata = blobClient.getProperties().getMetadata();
        resource.setFormat(metadata.get("format"));
        try {
            resource.setFilnavn(URLDecoder.decode(metadata.get("filename"), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            resource.setFilnavn(metadata.get("filename"));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        blobClient.download(out);
        resource.setData(Base64.getEncoder().encodeToString(out.toByteArray()));
        return resource;
    }

    @Override
    public boolean health() {
        return blobContainerClient.exists();
    }

    @Override
    public boolean exists(String recNo) {
        return blobContainerClient.getBlobClient(recNo).exists();
    }
}
