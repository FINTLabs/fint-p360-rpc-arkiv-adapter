package no.fint.p360.data.noark.skjerming;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.NoarkMetadataService;
import no.fint.model.arkiv.kodeverk.Skjermingshjemmel;
import no.fint.model.arkiv.kodeverk.Tilgangsrestriksjon;
import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fint.p360.repository.KodeverkRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;
import static no.fint.p360.data.utilities.P360Utils.getLinkTargets;


@Service
@Slf4j
public class SkjermingService {
    private final KodeverkRepository kodeverkRepository;
    private final NoarkMetadataService noarkMetadataService;

    public SkjermingService(KodeverkRepository kodeverkRepository, NoarkMetadataService noarkMetadataService) {
        this.kodeverkRepository = kodeverkRepository;
        this.noarkMetadataService = noarkMetadataService;
    }


    public SkjermingResource getSkjermingResource(Supplier<String> accessCodeSupplier, Supplier<String> pursuantSupplier) {
        SkjermingResource skjerming = new SkjermingResource();
        optionalValue(accessCodeSupplier.get())
                .map(Link.apply(Tilgangsrestriksjon.class, "systemid"))
                .ifPresent(skjerming::addTilgangsrestriksjon);
        optionalValue(pursuantSupplier.get())
                .flatMap(pursuant ->
                        kodeverkRepository.getSkjermingshjemmel()
                                .stream()
                                .filter(it -> it.getNavn().equals(pursuant))
                                .map(SkjermingshjemmelResource::getSystemId)
                                .map(Identifikator::getIdentifikatorverdi)
                                .filter(it -> StringUtils.startsWith(it, accessCodeSupplier.get()))
                                .map(Link.apply(Skjermingshjemmel.class, "systemid"))
                                .findAny())
                .ifPresent(skjerming::addSkjermingshjemmel);
        if (!skjerming.equals(new SkjermingResource())) {
            return skjerming;
        }
        return null;
    }

    public void applyAccessCodeAndPursuant(SkjermingResource skjerming, Consumer<String> accessCodeConsumer, Consumer<String> pursuantConsumer) {
        optionalValue(skjerming)
                .ifPresent(s -> {
                    applyParameterFromLink(s.getTilgangsrestriksjon(), accessCodeConsumer);
                    getLinkTargets(s.getSkjermingshjemmel())
                            .flatMap(id ->
                                    Stream.concat(
                                            Stream.concat(
                                                    // First, try finding pursuant from P360 codes
                                                    kodeverkRepository.getSkjermingshjemmel().stream(),
                                                    // Second, try finding in Noark Skjermingshjemmel
                                                    noarkMetadataService.getSkjermingshjemmel())
                                                    .filter(it -> it.getSystemId().getIdentifikatorverdi().equals(id)),
                                            getLinkTargets(s.getTilgangsrestriksjon())
                                                    // Third, try finding in Noark Tilgangsrestriksjon
                                                    .flatMap(acc -> noarkMetadataService.getTilgangsrestriksjon().filter(it -> it.getKode().equals(acc)))
                                    ))
                            .map(Begrep::getKode)
                            .filter(StringUtils::isNotBlank)
                            .findFirst()
                            .ifPresent(pursuantConsumer);
                });
    }

}
