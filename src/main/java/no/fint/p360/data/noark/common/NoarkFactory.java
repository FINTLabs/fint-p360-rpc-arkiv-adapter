package no.fint.p360.data.noark.common;

import no.fint.model.administrasjon.arkiv.Saksstatus;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.administrasjon.arkiv.KlasseResource;
import no.fint.model.resource.administrasjon.arkiv.SaksmappeResource;
import no.fint.model.resource.administrasjon.arkiv.SaksstatusResource;
import no.fint.p360.data.exception.GetDocumentException;
import no.fint.p360.data.exception.IllegalCaseNumberFormat;
import no.fint.p360.data.noark.journalpost.JournalpostFactory;
import no.fint.p360.data.noark.part.PartFactory;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.utilities.FintUtils;
import no.fint.p360.data.utilities.NOARKUtils;
import no.fint.p360.repository.KodeverkRepository;
import no.fint.p360.service.TitleService;
import no.p360.model.CaseService.*;
import no.p360.model.DocumentService.Document__1;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fint.p360.data.utilities.FintUtils.optionalValue;

@Service
public class NoarkFactory {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private KodeverkRepository kodeverkRepository;

    @Autowired
    private JournalpostFactory journalpostFactory;

    @Autowired
    private TitleService titleService;

    @Autowired
    private PartFactory partFactory;

    public void getSaksmappe(Case caseResult, SaksmappeResource saksmappeResource) throws GetDocumentException, IllegalCaseNumberFormat {
        String caseNumber = caseResult.getCaseNumber();
        String caseYear = NOARKUtils.getCaseYear(caseNumber);
        String sequenceNumber = NOARKUtils.getCaseSequenceNumber(caseNumber);

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
            Document__1 documentResult = documentService.getDocumentBySystemId(journalpostRecord);
            JournalpostResource journalpostResource = journalpostFactory.toFintResource(documentResult);
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

        optionalValue(caseResult.getResponsibleEnterprise())
                .map(ResponsibleEnterprise::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Organisasjonselement.class, "organisasjonsid"))
                .ifPresent(saksmappeResource::addAdministrativEnhet);

        optionalValue(caseResult.getResponsiblePerson())
                .map(ResponsiblePerson::getRecno)
                .map(String::valueOf)
                .map(Link.apply(Personalressurs.class, "ansattnummer"))
                .ifPresent(saksmappeResource::addSaksansvarlig);

        caseResult
                .getArchiveCodes()
                .stream()
                .map(ArchiveCode__1::getArchiveCode)
                .flatMap(code -> kodeverkRepository
                        .getKlasse()
                        .stream()
                        .filter(it -> StringUtils.equals(code, it.getTittel())))
                .map(KlasseResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(Link.apply(KlasseResource.class, "systemid"))
                .forEach(saksmappeResource::addKlasse);
        titleService.parseTitle(saksmappeResource, saksmappeResource.getTittel());
    }
    public boolean health()  {
        return documentService.ping();
    }
}
