package no.fint.p360.data.kulturminne;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.administrasjon.arkiv.MerknadResource;
import no.fint.model.resource.administrasjon.arkiv.PartsinformasjonResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.noark.common.NoarkFactory;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.utilities.*;
import no.fint.p360.service.AdditionalFieldService;
import no.fint.p360.service.TitleService;
import no.p360.model.CaseService.*;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.fint.p360.data.utilities.P360Utils.applyParameterFromLink;

@Slf4j
@Service
public class TilskuddFartoyFactory {

    @Autowired
    private NoarkFactory noarkFactory;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    TitleService titleService;

    @Autowired
    AdditionalFieldService additionalFieldService;

    public TilskuddFartoyResource toFintResource(Case caseResult) throws GetDocumentException, IllegalCaseNumberFormat, NotTilskuddfartoyException {
        if (!isTilskuddFartoy(caseResult)) {
            throw new NotTilskuddfartoyException(caseResult.getCaseNumber());
        }

        TilskuddFartoyResource tilskuddFartoy = new TilskuddFartoyResource();
        String caseNumber = caseResult.getCaseNumber();

        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

        try {
            TitleParser.Title title = TitleParser.parseTitle(caseResult.getTitle());
            tilskuddFartoy.setFartoyNavn(Strings.nullToEmpty(title.getDimension(TitleParser.FARTOY_NAVN)));
            tilskuddFartoy.setKallesignal(Strings.nullToEmpty(title.getDimension(TitleParser.FARTOY_KALLESIGNAL)));
            tilskuddFartoy.setKulturminneId(Strings.nullToEmpty(title.getDimension(TitleParser.KULTURMINNE_ID)));
            tilskuddFartoy.setSoknadsnummer(FintUtils.createIdentifikator(caseResult.getExternalId().getId()));
        } catch (UnableToParseTitle | NoSuchTitleDimension e) {
            log.error("{}", e.getMessage(), e);
        }

        noarkFactory.getSaksmappe(caseResult, tilskuddFartoy);

        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "mappeid", caseYear, sequenceNumber));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "systemid", caseResult.getRecno().toString()));
        tilskuddFartoy.addSelf(Link.with(TilskuddFartoy.class, "soknadsnummer", caseResult.getExternalId().getId()));

        return tilskuddFartoy;
    }


    public CreateCaseArgs convertToCreateCase(TilskuddFartoyResource tilskuddFartoy) {

        CreateCaseArgs createCaseArgs = new CreateCaseArgs();

        createCaseArgs.setTitle(titleService.getTitle(tilskuddFartoy));

        createCaseArgs.setAdditionalFields(
                additionalFieldService.getFieldsForResource(tilskuddFartoy)
                        .peek(System.out::println)
                        .map(it -> {
                            AdditionalField additionalField = new AdditionalField();
                            additionalField.setName(it.getName());
                            additionalField.setValue(it.getValue());
                            return additionalField;
                        }).collect(Collectors.toList())
        );

        createCaseArgs.setExternalId(P360Utils.getExternalIdParameter(tilskuddFartoy.getSoknadsnummer()));

        applyParameterFromLink(
                tilskuddFartoy.getAdministrativEnhet(),
                s -> createCaseArgs.setResponsibleEnterpriseRecno(Integer.valueOf(s))
        );

        applyParameterFromLink(
                tilskuddFartoy.getArkivdel(),
                createCaseArgs::setSubArchive
        );

        applyParameterFromLink(
                tilskuddFartoy.getSaksstatus(),
                createCaseArgs::setStatus
        );

        if (tilskuddFartoy.getSkjerming() != null) {
            applyParameterFromLink(
                    tilskuddFartoy.getSkjerming().getTilgangsrestriksjon(),
                    createCaseArgs::setAccessCode);

            applyParameterFromLink(
                    tilskuddFartoy.getSkjerming().getSkjermingshjemmel(),
                    createCaseArgs::setParagraph);

            // TODO createCaseParameter.setAccessGroup();
        }

        // TODO createCaseParameter.setCategory(objectFactory.createCaseParameterBaseCategory("recno:99999"));
        // TODO Missing parameters
        //createCaseParameter.setRemarks();
        //createCaseParameter.setStartDate();
        //createCaseParameter.setUnofficialTitle();


        List<Contact> contacts = new ArrayList<>();

        tilskuddFartoy
                .getPart()
                .stream()
                .map(this::createCaseContactParameter)
                .forEach(contacts::add);

        createCaseArgs.setContacts(contacts);

        List<Remark> remarks = new ArrayList<>();
        if (tilskuddFartoy.getMerknad() != null) {
            tilskuddFartoy
                    .getMerknad()
                    .stream()
                    .map(this::createCaseRemarkParameter)
                    .forEach(remarks::add);
        }
        createCaseArgs.setRemarks(remarks);

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

    private Remark createCaseRemarkParameter(MerknadResource merknadResource) {
        Remark remark = new Remark();
        remark.setContent(merknadResource.getMerknadstekst());

        merknadResource
                .getMerknadstype()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .findFirst()
                .ifPresent(remark::setRemarkType);

        return remark;
    }


    public Contact createCaseContactParameter(PartsinformasjonResource partsinformasjon) {
        Contact contact = new Contact();

        partsinformasjon
                .getPart()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .findFirst()
                .ifPresent(contact::setReferenceNumber);

        partsinformasjon
                .getPartRolle()
                .stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .findFirst()
                .ifPresent(contact::setRole);

        return contact;
    }

    public CreateDocumentArgs convertToCreateDocument(JournalpostResource journalpostResource, String caseNumber) {
        return journalpostFactory.toP360(journalpostResource, caseNumber);
    }

    // TODO: 2019-05-11 Should we check for both archive classification and external id (is it a digisak)
    // TODO Compare with CaseProperties
    private boolean isTilskuddFartoy(Case caseResult) {

        if (FintUtils.optionalValue(caseResult.getExternalId()).isPresent() && FintUtils.optionalValue(caseResult.getArchiveCodes()).isPresent()) {
            return caseResult.getExternalId().getType().equals(Constants.EXTERNAL_ID_TYPE);
        }

        return false;

    }

}
