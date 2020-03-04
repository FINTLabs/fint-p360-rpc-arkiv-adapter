package no.fint.p360.repository;

import no.fint.event.model.Event;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.administrasjon.arkiv.DokumentfilResource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public abstract class InternalRepository {
    private AtomicLong identifier =
            new AtomicLong(Long
                    .parseLong(DateTimeFormatter
                            .ofPattern("yyyyDDDHHmm'000'")
                            .format(LocalDateTime
                                    .now())));

    public abstract void putFile(Event<FintLinks> event, DokumentfilResource resource) throws IOException;

    protected String getNextSystemId() {
        return String.format("I_%d", identifier.incrementAndGet());
    }

    public abstract DokumentfilResource getFile(String recNo) throws IOException;

    public abstract DokumentfilResource silentGetFile(String recNo);

    public abstract boolean health();

    public abstract boolean exists(String recNo);
}
