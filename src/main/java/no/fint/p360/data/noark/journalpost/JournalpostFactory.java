package no.fint.p360.data.noark.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.arkiv.kodeverk.JournalStatus;
import no.fint.model.arkiv.kodeverk.JournalpostType;
import no.fint.model.arkiv.kodeverk.Merknadstype;
import no.fint.model.arkiv.noark.AdministrativEnhet;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource;
import no.fint.model.resource.arkiv.kodeverk.MerknadstypeResource;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.MerknadResource;
import no.fint.p360.data.noark.dokument.DokumentbeskrivelseFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartService;
import no.fint.p360.data.noark.skjerming.SkjermingService;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.repository.KodeverkRepository;
import no.p360.model.DocumentService.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static no.fint.p360.data.utilities.FintUtils.optionalValue;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@Slf4j
@Service
public class JournalpostFactory {

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private DokumentbeskrivelseFactory dokumentbeskrivelseFactory;

    @Autowired
    private SkjermingService skjermingService;

    @Autowired
    private KorrespondansepartService korrespondansepartService;

    @Autowired
    private KorrespondansepartFactory korrespondansepartFactory;

    public JournalpostResource toFintResource(Document__1 documentResult) {
        JournalpostResource journalpost = new JournalpostResource();


        optionalValue(documentResult.getFiles())
                .map(List::size)
                .map(Integer::longValue)
                .ifPresent(journalpost::setAntallVedlegg);
        optionalValue(documentResult.getTitle()).ifPresent(journalpost::setTittel);
        optionalValue(documentResult.getOfficialTitle()).ifPresent(journalpost::setOffentligTittel);
        optionalValue(documentResult.getDocumentDate())
                .map(FintUtils::parseIsoDate)
                .ifPresent(journalpost::setDokumentetsDato);
        optionalValue(documentResult.getJournalDate())
                .map(FintUtils::parseIsoDate)
                .ifPresent(journalpost::setJournalDato);
        optionalValue(documentResult.getCreatedDate())
                .map(FintUtils::parseDate)
                .ifPresent(journalpost::setOpprettetDato);

        // FIXME: 2019-05-08 check for empty
        journalpost.setDokumentbeskrivelse(Collections.emptyList());
        // FIXME: 2019-05-08 check for empty
        journalpost.setForfatter(Collections.emptyList());
        // FIXME: 2019-05-08 check for empty keywords
        journalpost.setNokkelord(Collections.emptyList());
        // FIXME: 2019-05-08 check for empty
        journalpost.setReferanseArkivDel(Collections.emptyList());

        // FIXME: 2019-05-08 Figure out which is already rep and if some of them should be code lists (noark) + skjerming
        journalpost.setBeskrivelse(String.format("%s - %s - %s", documentResult.getType().getDescription(), documentResult.getStatusDescription(), documentResult.getAccessCodeDescription()));

        // TODO: 2019-05-08 Check noark if this is correct
        journalpost.setForfatter(Collections.singletonList(documentResult.getResponsiblePersonName()));

        journalpost.setKorrespondansepart(
                optionalValue(documentResult.getContacts())
                        .map(Collection::stream)
                        .orElse(Stream.empty())
                        .map(korrespondansepartFactory::toFintResource)
                        .collect(Collectors.toList()));


        String[] split = optionalValue(documentResult.getDocumentNumber()).orElse("").split("-");
        if (split.length == 2 && StringUtils.isNumeric(split[1])) {
            journalpost.setJournalPostnummer(Long.parseLong(split[1]));
        }

        optionalValue(documentResult.getResponsiblePerson())
                .map(ResponsiblePerson::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Personalressurs.class, "ansattnummer"))
                .ifPresent(journalpost::addSaksbehandler);
        optionalValue(documentResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getRecno)
                .map(String::valueOf)
                .map(Link.apply(AdministrativEnhet.class, "systemid"))
                .ifPresent(journalpost::addAdministrativEnhet);
        optionalValue(documentResult.getCategory())
                .map(Category::getRecno)
                .map(String::valueOf)
                .map(Link.apply(JournalpostType.class, "systemid"))
                .ifPresent(journalpost::addJournalposttype);
        optionalValue(documentResult.getStatusCode())
                .flatMap(code -> kodeverkRepository
                        .getJournalStatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(code, it.getKode()))
                        .findAny())
                .map(JournalStatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(JournalStatus.class, "systemid"))
                .ifPresent(journalpost::addJournalstatus);

        journalpost.setMerknad(
                optionalValue(documentResult.getRemarks())
                        .map(List::stream)
                        .orElse(Stream.empty())
                        .map(this::createMerknad)
                        .collect(Collectors.toList()));

        List<File__1> documentFileResult = documentResult.getFiles();

        journalpost.setDokumentbeskrivelse(documentFileResult
                .stream()
                .map(dokumentbeskrivelseFactory::toFintResource)
                .collect(Collectors.toList()));

        return journalpost;
    }


    private MerknadResource createMerknad(Remark__1 remarkInfo) {
        MerknadResource merknad = new MerknadResource();

        optionalValue(remarkInfo.getTypeCode())
                .flatMap(type ->
                        kodeverkRepository
                                .getMerknadstype()
                                .stream()
                                .filter(v -> StringUtils.equalsIgnoreCase(type, v.getKode()))
                                .findAny())
                .map(MerknadstypeResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Merknadstype.class, "systemid"))
                .ifPresent(merknad::addMerknadstype);

        merknad.setMerknadstekst(
                Stream.of(optionalValue(remarkInfo.getTitle()), optionalValue(remarkInfo.getContent()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.joining(" - ")));

        optionalValue(remarkInfo.getModifiedDate())
                .map(FintUtils::parseDate)
                .ifPresent(merknad::setMerknadsdato);

        // TODO merknad.addMerknadRegistrertAv();

        return merknad;
    }

    public CreateDocumentArgs toP360(JournalpostResource journalpostResource, String caseNumber) {

        CreateDocumentArgs createDocumentArgs = new CreateDocumentArgs();
//        createDocumentParameter.setADContextUser(objectFactory.createDocumentParameterBaseADContextUser(adapterProps.getP360User()));

        createDocumentArgs.setTitle(journalpostResource.getOffentligTittel());
        createDocumentArgs.setUnofficialTitle(journalpostResource.getTittel());
        createDocumentArgs.setCaseNumber(caseNumber);

        skjermingService.applyAccessCodeAndParagraph(journalpostResource.getSkjerming(), createDocumentArgs::setAccessCode, createDocumentArgs::setParagraph);


        applyParameterFromLink(
                journalpostResource.getAdministrativEnhet(),
                Integer::parseInt,
                createDocumentArgs::setResponsibleEnterpriseRecno);

        // TODO Set from incoming fields
        //createDocumentParameter.setDocumentDate();

        applyParameterFromLink(
                journalpostResource.getJournalposttype(),
                createDocumentArgs::setCategory);

        applyParameterFromLink(
                journalpostResource.getJournalstatus(),
                createDocumentArgs::setStatus);

        final Pair<List<Contact>, List<UnregisteredContact>> contacts = korrespondansepartService.getContactsFromKorrespondansepart(journalpostResource.getKorrespondansepart());

        createDocumentArgs.setContacts(contacts.getLeft());
        createDocumentArgs.setUnregisteredContacts(contacts.getRight());

        /* createDocumentArgs.setUnregisteredContacts(
                ofNullable(journalpostResource.getKorrespondansepart())
                        .map(List::stream)
                        .orElseGet(Stream::empty)
                        .map(this::createDocumentContact)
                        .collect(Collectors.toList()));

         */


        createDocumentArgs.setFiles(
                ofNullable(journalpostResource.getDokumentbeskrivelse())
                        .map(List::stream)
                        .orElseGet(Stream::empty)
                        .peek(r -> log.info("Handling Dokumentbeskrivelse: {}", r))
                        .flatMap(this::createFiles)
                        .collect(Collectors.toList()));

        createDocumentArgs.setRemarks(
                ofNullable(journalpostResource.getMerknad())
                        .map(List::stream)
                        .orElseGet(Stream::empty)
                        .map(this::createDocumentRemarkParameter)
                        .collect(Collectors.toList()));

        return createDocumentArgs;
    }

    private Remark createDocumentRemarkParameter(MerknadResource merknadResource) {
        Remark remark = new Remark();
        remark.setContent(merknadResource.getMerknadstekst());

        merknadResource
                .getMerknadstype()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .findFirst()
                .ifPresent(remark::setRemarkType);
        return remark;
    }


    private Stream<File> createFiles(DokumentbeskrivelseResource dokumentbeskrivelse) {
        return dokumentbeskrivelse
                .getDokumentobjekt()
                .stream()
                .map(dokumentobjekt -> dokumentbeskrivelseFactory.toP360(dokumentbeskrivelse, dokumentobjekt));
    }

}
