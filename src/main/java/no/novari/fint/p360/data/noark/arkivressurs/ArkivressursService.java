package no.novari.fint.p360.data.noark.arkivressurs;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.novari.fint.p360.data.exception.ContactPersonNotFound;
import no.novari.fint.p360.data.p360.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Slf4j
@Service
public class ArkivressursService {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ArkivressursFactory arkivressursFactory;

    public Stream<ArkivressursResource> getArkivressurs() throws ContactPersonNotFound {
        return contactService.getContactPersonsByCategory("recno:1").stream()
                .map(arkivressursFactory::toFintResource);
    }

}
