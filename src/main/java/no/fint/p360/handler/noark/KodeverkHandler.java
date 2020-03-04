package no.fint.p360.handler.noark;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.resource.FintLinks;
import no.fint.p360.handler.Handler;
import no.fint.p360.repository.KodeverkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static no.fint.model.administrasjon.arkiv.ArkivActions.*;

@Service
@Slf4j
public class KodeverkHandler implements Handler {

    private final EnumMap<ArkivActions, Supplier<List<? extends FintLinks>>> suppliers = new EnumMap<>(ArkivActions.class);

    @PostConstruct
    public void init() {
        suppliers.put(GET_ALL_DOKUMENTSTATUS, kodeverkRepository::getDokumentStatus);
        suppliers.put(GET_ALL_DOKUMENTTYPE, kodeverkRepository::getDokumentType);
        suppliers.put(GET_ALL_JOURNALPOSTTYPE, kodeverkRepository::getJournalpostType);
        suppliers.put(GET_ALL_JOURNALSTATUS, kodeverkRepository::getJournalStatus);
        suppliers.put(GET_ALL_KORRESPONDANSEPARTTYPE, kodeverkRepository::getKorrespondansepartType);
        suppliers.put(GET_ALL_MERKNADSTYPE, kodeverkRepository::getMerknadstype);
        suppliers.put(GET_ALL_PARTROLLE, kodeverkRepository::getPartRolle);
        suppliers.put(GET_ALL_SAKSSTATUS, kodeverkRepository::getSaksstatus);
        suppliers.put(GET_ALL_SKJERMINGSHJEMMEL, kodeverkRepository::getSkjermingshjemmel);
        suppliers.put(GET_ALL_TILGANGSRESTRIKSJON, kodeverkRepository::getTilgangsrestriksjon);
        suppliers.put(GET_ALL_TILKNYTTETREGISTRERINGSOM, kodeverkRepository::getTilknyttetRegistreringSom);
        suppliers.put(GET_ALL_VARIANTFORMAT, kodeverkRepository::getVariantformat);
        suppliers.put(GET_ALL_KLASSIFIKASJONSSYSTEM, kodeverkRepository::getKlassifikasjonssystem);
        suppliers.put(GET_ALL_KLASSE, kodeverkRepository::getKlasse);
    }

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Override
    public void accept(Event<FintLinks> response) {
        if (!health()) {
            response.setStatus(Status.ADAPTER_REJECTED);
            response.setMessage("Health test failed");
            return;
        }
        response.setResponseStatus(ResponseStatus.ACCEPTED);
        suppliers.getOrDefault(ArkivActions.valueOf(response.getAction()), Collections::emptyList)
                .get()
                .forEach(response::addData);
    }

    @Override
    public Set<String> actions() {
        return suppliers.keySet().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public boolean health() {
        return kodeverkRepository.health();
    }

}
