package no.fint.p360.data.noark.common;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.AdditionalFieldService;
import no.fint.arkiv.CaseProperties;
import no.fint.arkiv.TitleService;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.arkiv.kodeverk.Saksstatus;
import no.fint.model.arkiv.noark.AdministrativEnhet;
import no.fint.model.arkiv.noark.Arkivdel;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.SaksmappetypeResource;
import no.fint.model.resource.arkiv.kodeverk.SaksstatusResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.fint.model.resource.arkiv.noark.JournalpostResource;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.MerknadResource;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.codes.klasse.KlasseFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.noark.part.PartFactory;
import no.fint.p360.data.noark.part.PartService;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.NOARKUtils;
import no.fint.p360.model.ContextUser;
import no.fint.p360.model.FilterSet;
import no.fint.p360.repository.KodeverkRepository;
import no.fint.p360.service.ContextUserService;
import no.p360.model.CaseService.*;
import no.p360.model.DocumentService.Document__1;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;
import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@Service
@Slf4j
public class NoarkFactory {

    @Value("${fint.arkiv.part:false}")
    private boolean usePart;

    @Value("${fint.arkiv.casenumber.format:}")
    private String caseNumberFormat;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    private TitleService titleService;

    @Autowired
    private AdditionalFieldService additionalFieldService;

    @Autowired
    private PartFactory partFactory;

    @Autowired
    private PartService partService;

    @Autowired
    private KlasseFactory klasseFactory;

    @Autowired
    private ContextUserService contextUserService;


    public <T extends SaksmappeResource> T getSaksmappe(FilterSet filterSet, CaseProperties caseProperties, Case caseResult, T saksmappeResource) throws GetDocumentException, IllegalCaseNumberFormat {
        String caseNumber = caseResult.getCaseNumber();
        log.debug("Case number as returned from P360: {}", caseNumber);
        String caseYear = getCaseYear(caseNumber, caseResult);
        log.debug("Case year (our calculation): {}", caseYear);
        String sequenceNumber = getCaseSequenceNumber(caseNumber);
        log.debug("Case sequence number (our calculation): {}", sequenceNumber);

        optionalValue(caseResult.getNotes())
                .filter(StringUtils::isNotBlank)
                .ifPresent(saksmappeResource::setBeskrivelse);
        saksmappeResource.setMappeId(FintUtils.createIdentifikator(caseNumber));
        saksmappeResource.setSystemId(FintUtils.createIdentifikator(caseResult.getRecno().toString()));
        saksmappeResource.setSakssekvensnummer(sequenceNumber);
        saksmappeResource.setSaksaar(caseYear);
        saksmappeResource.setSaksdato(FintUtils.parseIsoDate(caseResult.getDate()));
        saksmappeResource.setOpprettetDato(FintUtils.parseIsoDate(caseResult.getCreatedDate()));
        saksmappeResource.setTittel(caseResult.getUnofficialTitle());
        saksmappeResource.setOffentligTittel(caseResult.getTitle());
        saksmappeResource.setNoekkelord(caseResult
                .getArchiveCodes()
                .stream()
                .flatMap(it -> Stream.of(it.getArchiveType(), it.getArchiveCode()))
                .collect(Collectors.toList()));

        saksmappeResource.setPart(
                optionalValue(caseResult.getContacts()).stream()
                        .flatMap(Collection::stream)
                        .map(partFactory::getPartsinformasjon)
                        .collect(Collectors.toList()));

        List<String> journalpostIds = optionalValue(caseResult.getDocuments()).stream()
                .flatMap(Collection::stream)
                .map(Document::getRecno)
                .map(String::valueOf)
                .toList();
        List<JournalpostResource> journalpostList = new ArrayList<>(journalpostIds.size());
        for (String journalpostRecord : journalpostIds) {
            Document__1 documentResult = documentService.getDocumentBySystemId(filterSet, journalpostRecord);
            JournalpostResource journalpostResource = journalpostFactory.toFintResource(documentResult, caseProperties, saksmappeResource);
            journalpostList.add(journalpostResource);
        }
        saksmappeResource.setJournalpost(journalpostList);

        optionalValue(caseResult.getStatus())
                .flatMap(kode -> kodeverkRepository
                        .getSaksstatus()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(SaksstatusResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(Saksstatus.class, "systemid"))
                .ifPresent(saksmappeResource::addSaksstatus);

        optionalValue(caseResult.getSubArchive())
                .map(Link.apply(Arkivdel.class, "systemid"))
                .ifPresent(saksmappeResource::addArkivdel);

        optionalValue(caseResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getRecno)
                .map(String::valueOf)
                .map(Link.apply(AdministrativEnhet.class, "systemid"))
                .ifPresent(saksmappeResource::addAdministrativEnhet);

        optionalValue(caseResult.getResponsiblePerson())
                .map(ResponsiblePerson::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Personalressurs.class, "ansattnummer"))
                .ifPresent(saksmappeResource::addSaksansvarlig);

        optionalValue(caseResult.getAccessGroup())
                .flatMap(kode -> kodeverkRepository
                        .getTilgangsgruppe()
                        .stream()
                        .filter(it -> StringUtils.equalsIgnoreCase(kode, it.getNavn()))
                        .findAny())
                .map(TilgangsgruppeResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(TilgangsgruppeResource.class, "systemid"))
                .ifPresent(saksmappeResource::addTilgangsgruppe);

        saksmappeResource.setKlasse(
                caseResult
                        .getArchiveCodes()
                        .stream()
                        .map(klasseFactory::toFintResource)
                        .collect(Collectors.toList()));

        titleService.parseCaseTitle(caseProperties.getTitle(), saksmappeResource, saksmappeResource.getTittel());

        additionalFieldService.setFieldsForResource(caseProperties.getField(), saksmappeResource,
                caseResult.getCustomFields()
                        .stream()
                        .map(f -> new AdditionalFieldService.Field(f.getName(), StringUtils.trimToEmpty(f.getValue())))
                        .collect(Collectors.toList()));

        optionalValue(caseResult.getCaseTypeCode())
                .flatMap(casetype -> kodeverkRepository.getSaksmappetype()
                        .stream()
                        .filter(saksmappetype ->
                                StringUtils.equalsIgnoreCase(casetype, saksmappetype.getKode()))
                        .findAny())
                .map(SaksmappetypeResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(SaksmappetypeResource.class, "systemid"))
                .ifPresent(saksmappeResource::addSaksmappetype);

        return saksmappeResource;
    }


    public CreateCaseArgs createCaseArgs(CaseProperties caseProperties, SaksmappeResource saksmappeResource) {
        CreateCaseArgs createCaseArgs = new CreateCaseArgs();

        String tittel = titleService.getCaseTitle(caseProperties.getTitle(), saksmappeResource);
        String offentligTittel = saksmappeResource.getOffentligTittel();

        log.debug("Case title: {}, officalTitle: {}", tittel, offentligTittel);

        if (StringUtils.isNotBlank(offentligTittel)) {
            createCaseArgs.setTitle(offentligTittel);
            createCaseArgs.setUnofficialTitle(tittel);
        } else {
            createCaseArgs.setTitle(tittel); // When not set, UnofficialTitle will get the same value as Title in P360
        }

        Optional.ofNullable(contextUserService.getContextUserForCaseType(saksmappeResource))
                .map(ContextUser::getUsername)
                .filter(StringUtils::isNotBlank)
                .ifPresent(createCaseArgs::setADContextUser);

        Optional.ofNullable(caseProperties.getSaksansvarlig())
                .filter(StringUtils::isNotBlank)
                .map(Integer::valueOf)
                .ifPresent(createCaseArgs::setResponsiblePersonRecno);

        if (StringUtils.isBlank(caseProperties.getSaksansvarlig())) {
            log.debug("No saksansvarlig from Case Defaults, we'll fetch it from the SaksmappeResource.");
            applyParameterFromLink(
                    saksmappeResource.getSaksansvarlig(),
                    Integer::valueOf,
                    createCaseArgs::setResponsiblePersonRecno);
        }

        createCaseArgs.setAdditionalFields(
                additionalFieldService.getFieldsForResource(caseProperties.getField(), saksmappeResource)
                        .map(it -> {
                            AdditionalField additionalField = new AdditionalField();
                            additionalField.setName(it.getName());
                            additionalField.setValue(it.getValue());
                            return additionalField;
                        }).collect(Collectors.toList())
        );

        applyParameterFromLink(
                saksmappeResource.getAdministrativEnhet(),
                Integer::valueOf,
                createCaseArgs::setResponsibleEnterpriseRecno
        );

        applyParameterFromLink(
                saksmappeResource.getArkivdel(),
                createCaseArgs::setSubArchive
        );

        applyParameterFromLink(
                saksmappeResource.getSaksmappetype(),
                createCaseArgs::setCaseType
        );

        applyParameterFromLink(
                saksmappeResource.getSaksstatus(),
                createCaseArgs::setStatus
        );

        if (saksmappeResource.getSkjerming() != null) {
            applyParameterFromLink(
                    saksmappeResource.getSkjerming().getTilgangsrestriksjon(),
                    createCaseArgs::setAccessCode);

            applyParameterFromLink(
                    saksmappeResource.getSkjerming().getSkjermingshjemmel(),
                    createCaseArgs::setParagraph);
        }

        // TODO createCaseParameter.setCategory(objectFactory.createCaseParameterBaseCategory("recno:99999"));
        // TODO Missing parameters
        //createCaseParameter.setRemarks();
        //createCaseParameter.setStartDate();

        if (usePart && saksmappeResource.getPart() != null) {
            final Pair<List<Contact>, List<UnregisteredContact>> contacts = partService
                    .getContactsFromSakspart(saksmappeResource.getPart());
            createCaseArgs.setContacts(contacts.getLeft());
            createCaseArgs.setUnregisteredContacts(contacts.getRight());
        }

        if (saksmappeResource.getMerknad() != null) {
            createCaseArgs.setRemarks(
                    saksmappeResource
                            .getMerknad()
                            .stream()
                            .map(this::createCaseRemarkParameter)
                            .collect(Collectors.toList()));
        }

        if (saksmappeResource.getKlasse() != null) {
            createCaseArgs.setArchiveCodes(
                    saksmappeResource
                            .getKlasse()
                            .stream()
                            .map(this::createCaseArchiveCode)
                            .collect(Collectors.toList()));
        }

        applyParameterFromLink(
                saksmappeResource.getTilgangsgruppe(),
                createCaseArgs::setAccessGroup
        );

        return createCaseArgs;
    }

    private ArchiveCode createCaseArchiveCode(KlasseResource klasseResource) {
        ArchiveCode archiveCode = new ArchiveCode();
        applyParameterFromLink(klasseResource.getKlassifikasjonssystem(), archiveCode::setArchiveType);
        archiveCode.setArchiveCode(klasseResource.getKlasseId());
        archiveCode.setSort(klasseResource.getRekkefolge());

        // ArchiveCode is assumed to be manual text (i.e. dynamic) if the code does not exist in the code list.
        final boolean codeValue =
                StringUtils.startsWith(klasseResource.getKlasseId(), "recno:")
                        || kodeverkRepository
                        .getKlasse()
                        .stream()
                        .anyMatch(it -> StringUtils.equals(it.getKlasseId(), klasseResource.getKlasseId()));
        archiveCode.setIsManualText(!codeValue);
        return archiveCode;
    }

    private Remark createCaseRemarkParameter(MerknadResource merknadResource) {
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

    private String getCaseYear(String caseNumber, Case caseResult) {
        if (StringUtils.isEmpty(caseNumberFormat)) {
            return NOARKUtils.getCaseYear(caseNumber);
        } else if (caseNumberFormat.equalsIgnoreCase("yyyy")) {
            return caseNumber.substring(0, 4);
        } else if (caseNumberFormat.equalsIgnoreCase("yy")) {
            return caseNumber.substring(0, 2);
        }

        String caseYear = caseResult.getCreatedDate().substring(0, 4);
        log.warn("We're not able to determine case year from the case number ({}). Therefore we'll use the case created date's year ({}).",
                caseNumber, caseYear);
        return caseYear;
    }

    private String getCaseSequenceNumber(String caseNumber) {
        if (StringUtils.isEmpty(caseNumberFormat)) {
            return NOARKUtils.getCaseSequenceNumber(caseNumber);
        } else if (caseNumberFormat.equalsIgnoreCase("yyyy")) {
            return caseNumber.substring(4);
        } else if (caseNumberFormat.equalsIgnoreCase("yy")) {
            return caseNumber.substring(2);
        }

        log.warn("We'll return the complete case number ({}) as case sequence number due to a way to funky case number format ({}).",
                caseNumber, caseNumberFormat);
        return caseNumber;
    }
}
