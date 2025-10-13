package no.fint.p360.data.noark.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseProperties;
import no.fint.arkiv.TitleService;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.arkiv.kodeverk.JournalStatus;
import no.fint.model.arkiv.kodeverk.JournalpostType;
import no.fint.model.arkiv.kodeverk.Merknadstype;
import no.fint.model.arkiv.noark.AdministrativEnhet;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource;
import no.fint.model.resource.arkiv.kodeverk.MerknadstypeResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.MerknadResource;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import no.fint.p360.config.DocumentArgsConfiguration;
import no.fint.p360.data.noark.dokument.DokumentbeskrivelseFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartFactory;
import no.fint.p360.data.noark.korrespondansepart.KorrespondansepartService;
import no.fint.p360.data.noark.skjerming.SkjermingService;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.model.ContextUser;
import no.fint.p360.repository.KodeverkRepository;
import no.fint.p360.service.ContextUserService;
import no.p360.model.DocumentService.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static no.fint.p360.data.utilities.P360Utils.getLinkTargets;

@Slf4j
@Service
public class JournalpostFactory {

    @Value("${fint.p360.documentargs.override-archive:false}")
    private boolean overrideArchive;

    private List<DocumentArgsConfiguration.SakmappetypeMapping> sakmappetypeDocumentarchivesMapping;

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

    @Autowired
    private ContextUserService contextUserService;

    @Autowired
    private TitleService titleService;

    public JournalpostFactory(DocumentArgsConfiguration documentArgsConfiguration) {
        this.sakmappetypeDocumentarchivesMapping = documentArgsConfiguration.getSakmappetypeMapping();
    }

    public JournalpostResource toFintResource(Document__1 documentResult,
                                              CaseProperties caseProperties,
                                              SaksmappeResource saksmappeResource) {
        JournalpostResource journalpost = new JournalpostResource();

        optionalValue(documentResult.getFiles())
                .map(List::size)
                .map(Integer::longValue)
                .ifPresent(journalpost::setAntallVedlegg);

        titleService.parseCaseTitle(caseProperties.getTitle(), saksmappeResource, saksmappeResource.getTittel());

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
        journalpost.setBeskrivelse(String.format("%s - %s - %s",
                documentResult.getType().getDescription(),
                documentResult.getStatusDescription(),
                documentResult.getAccessCodeDescription()));

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

        optionalValue(
                skjermingService.getSkjermingResource(
                        documentResult::getAccessCodeCode,
                        documentResult::getParagraph
                )).ifPresent(journalpost::setSkjerming);

        optionalValue(documentResult.getAccessGroup())
                .flatMap(kode -> kodeverkRepository
                        .getTilgangsgruppe()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(TilgangsgruppeResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(TilgangsgruppeResource.class, "systemid"))
                .ifPresent(journalpost::addTilgangsgruppe);

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

    public CreateDocumentArgs toP360(JournalpostResource journalpostResource,
                                     String caseNumber,
                                     SaksmappeResource saksmappeResource,
                                     CaseProperties caseProperties) {
        CreateDocumentArgs createDocumentArgs = new CreateDocumentArgs();

        String recordTitlePrefix = titleService.getRecordTitlePrefix(caseProperties.getTitle(), saksmappeResource);
        log.info("Record title prefix (brought to you by FINT Arkiv Case Defaults): {}", recordTitlePrefix);

        if (journalpostResource.getOffentligTittel() != null || StringUtils.isNotBlank(journalpostResource.getOffentligTittel())) {
            createDocumentArgs.setTitle(StringUtils.trim(recordTitlePrefix + journalpostResource.getOffentligTittel()));
        } else {
            log.debug("No title found, the prefix ({}) will be the complete title.", recordTitlePrefix);
            createDocumentArgs.setTitle(StringUtils.trim(recordTitlePrefix));
        }

        if (journalpostResource.getTittel() != null || StringUtils.isNotBlank(journalpostResource.getTittel())) {
            createDocumentArgs.setUnofficialTitle(StringUtils.trim(recordTitlePrefix + journalpostResource.getTittel()));
        } else {
            log.info("No title found, the prefix ({}) will be the complete title.", recordTitlePrefix);
            createDocumentArgs.setTitle(StringUtils.trim(recordTitlePrefix));
        }

        createDocumentArgs.setCaseNumber(caseNumber);

        skjermingService.applyAccessCodeAndParagraph(journalpostResource.getSkjerming(),
                createDocumentArgs::setAccessCode,
                createDocumentArgs::setParagraph);

        applyParameterFromLink(
                journalpostResource.getAdministrativEnhet(),
                Integer::parseInt,
                createDocumentArgs::setResponsibleEnterpriseRecno);

        applyParameterFromLink(
                journalpostResource.getArkivdel(),
                createDocumentArgs::setSubArchive);

        ofNullable(journalpostResource.getDokumentetsDato())
                .map(FintUtils::formatIsoDate)
                .ifPresent(createDocumentArgs::setDocumentDate);

        applyParameterFromLink(
                journalpostResource.getJournalposttype(),
                createDocumentArgs::setCategory);

        applyParameterFromLink(
                journalpostResource.getJournalstatus(),
                createDocumentArgs::setStatus);

        if (overrideArchive) {
            log.debug("Let's override the default archive value. We're setting it based on the Saksmappetype: {}",
                    saksmappeResource.getSaksmappetype());

            getLinkTargets(saksmappeResource.getSaksmappetype()).findFirst()
                    .ifPresent(saksmappetype ->
                            sakmappetypeDocumentarchivesMapping.stream()
                                    .filter(item -> item.getSakmappetype().equals(saksmappetype))
                                    .findFirst()
                                    .ifPresent(item ->
                                            createDocumentArgs.setArchive(item.getDocumentarchive())));
        }

        final Pair<List<Contact>, List<UnregisteredContact>> contacts = korrespondansepartService.getContactsFromKorrespondansepart(
                ofNullable(journalpostResource.getKorrespondansepart()).orElse(Collections.emptyList()),
                SkjermingService.hasTilgangsrestriksjon(journalpostResource.getSkjerming()));

        createDocumentArgs.setContacts(contacts.getLeft());
        createDocumentArgs.setUnregisteredContacts(contacts.getRight());

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

        Optional.ofNullable(contextUserService.getContextUserForCaseType(saksmappeResource))
                .map(ContextUser::getUsername)
                .filter(StringUtils::isNotBlank)
                .ifPresent(createDocumentArgs::setADContextUser);

        applyParameterFromLink(
                journalpostResource.getSaksbehandler(),
                Integer::valueOf,
                createDocumentArgs::setResponsiblePersonRecno);

        applyParameterFromLink(
                journalpostResource.getTilgangsgruppe(),
                createDocumentArgs::setAccessGroup);

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