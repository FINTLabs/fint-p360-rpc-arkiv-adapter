package no.novari.p360.data.noark.dokument;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.novari.p360.repository.InternalRepository;
import no.novari.p360.service.CachedFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DokumentfilService {

    @Autowired
    private CachedFileService cachedFileService;

    @Autowired
    private InternalRepository internalRepository;

    public DokumentfilResource getDokumentfil(String systemId) throws IOException {
        if (StringUtils.startsWith(systemId, "I_")) {
            return internalRepository.getFile(systemId);
        } else {
            return cachedFileService.getFile(systemId);
        }
    }
    public boolean health()  {
        return cachedFileService.health() && internalRepository.health();
    }
}
