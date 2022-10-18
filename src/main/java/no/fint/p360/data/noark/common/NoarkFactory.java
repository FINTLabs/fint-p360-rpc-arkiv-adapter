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
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.kodeverk.SaksstatusResource;
import no.fint.model.resource.arkiv.noark.*;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.codes.klasse.KlasseFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.noark.part.PartFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
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
                optionalValue(caseResult.getContacts())
                        .map(List::stream)
                        .orElseGet(Stream::empty)
                        .map(partFactory::getPartsinformasjon)
                        .collect(Collectors.toList()));

        List<String> journalpostIds = optionalValue(caseResult.getDocuments())
                .map(List::stream)
                .orElse(Stream.empty())
                .map(Document::getRecno)
                .map(String::valueOf)
                .collect(Collectors.toList());
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

        return saksmappeResource;
    }


    public CreateCaseArgs createCaseArgs(CaseProperties caseProperties, SaksmappeResource saksmappeResource) {
        CreateCaseArgs createCaseArgs = new CreateCaseArgs();

        createCaseArgs.setTitle(titleService.getCaseTitle(caseProperties.getTitle(), saksmappeResource));

        Optional.ofNullable(contextUserService.getContextUserForCaseType(saksmappeResource))
                .map(ContextUser::getUsername)
                .filter(StringUtils::isNotBlank)
                .ifPresent(createCaseArgs::setADContextUser);

        Optional.ofNullable(caseProperties.getSaksansvarlig())
            .filter(StringUtils::isNotBlank)
            .map(Integer::valueOf)
            .ifPresent(createCaseArgs::setResponsiblePersonRecno);

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

            // TODO createCaseParameter.setAccessGroup();
        }

        // TODO createCaseParameter.setCategory(objectFactory.createCaseParameterBaseCategory("recno:99999"));
        // TODO Missing parameters
        //createCaseParameter.setRemarks();
        //createCaseParameter.setStartDate();
        //createCaseParameter.setUnofficialTitle();

        if (usePart && saksmappeResource.getPart() != null) {
            createCaseArgs.setUnregisteredContacts(
                    saksmappeResource
                            .getPart()
                            .stream()
                            .map(this::createCaseContactParameter)
                            .collect(Collectors.toList())
            );
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


        // TODO Responsible person
        /*
        createCaseParameter.setResponsiblePersonIdNumber(
                objectFactory.createCaseParameterBaseResponsiblePersonIdNumber(
                        tilskuddFartoy.getSaksansvarlig().get(0).getHref()
                )
        );
        */

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


    public UnregisteredContact createCaseContactParameter(PartResource part) {
        UnregisteredContact contact = new UnregisteredContact();

        ofNullable(part.getAdresse())
                .map(AdresseResource::getAdresselinje)
                .map(l -> String.join("\n", l))
                .filter(StringUtils::isNotBlank)
                .ifPresent(contact::setAddress);

        ofNullable(part.getAdresse())
                .map(AdresseResource::getPostnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(contact::setZipCode);

        ofNullable(part.getAdresse())
                .map(AdresseResource::getPoststed)
                .filter(StringUtils::isNotBlank)
                .ifPresent(contact::setZipPlace);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getEpostadresse)
                .filter(StringUtils::isNotBlank)
                .ifPresent(contact::setEmail);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getMobiltelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(contact::setMobilePhone);

        ofNullable(part.getKontaktinformasjon())
                .map(Kontaktinformasjon::getTelefonnummer)
                .filter(StringUtils::isNotBlank)
                .ifPresent(contact::setPhone);


        contact.setContactName(part.getKontaktperson());
        contact.setContactCompanyName(part.getPartNavn());

        part
                .getPartRolle()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .map(s -> StringUtils.prependIfMissing(s, "recno:"))
                .findFirst()
                .ifPresent(contact::setRole);

        return contact;
    }

    private String getCaseYear(String caseNumber, Case caseResult) {
        if (StringUtils.isEmpty(caseNumberFormat)) {
            return NOARKUtils.getCaseYear(caseNumber);
        } else if (caseNumberFormat.equalsIgnoreCase("yyyy")) {
            return caseNumber.substring(0,4);
        } else if (caseNumberFormat.equalsIgnoreCase("yy")) {
            return caseNumber.substring(0,2);
        }

        String caseYear = caseResult.getCreatedDate().substring(0,4);
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
