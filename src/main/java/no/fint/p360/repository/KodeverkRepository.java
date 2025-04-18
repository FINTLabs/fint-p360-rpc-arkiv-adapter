package no.fint.p360.repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.kodeverk.*;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.KlassifikasjonssystemResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsgruppeResource;
import no.fint.p360.data.noark.codes.CaseCategoryService;
import no.fint.p360.data.noark.codes.ResponseCodeService;
import no.fint.p360.data.noark.codes.dokumentstatus.DokumentstatusService;
import no.fint.p360.data.noark.codes.dokumenttype.DokumenttypeService;
import no.fint.p360.data.noark.codes.filformat.FilformatResource;
import no.fint.p360.data.noark.codes.filformat.FilformatService;
import no.fint.p360.data.noark.codes.journalposttype.JournalpostTypeService;
import no.fint.p360.data.noark.codes.journalstatus.JournalStatusService;
import no.fint.p360.data.noark.codes.klasse.KlasseService;
import no.fint.p360.data.noark.codes.klassifikasjonssystem.KlassifikasjonssystemService;
import no.fint.p360.data.noark.codes.korrespondanseparttype.KorrespondansepartTypeService;
import no.fint.p360.data.noark.codes.merknadstype.MerknadstypeService;
import no.fint.p360.data.noark.codes.partrolle.PartRolleService;
import no.fint.p360.data.noark.codes.saksmappetype.SaksmappetypeService;
import no.fint.p360.data.noark.codes.saksstatus.SaksStatusService;
import no.fint.p360.data.noark.codes.skjermingshjemmel.SkjermingshjemmelService;
import no.fint.p360.data.noark.codes.tilgangsgruppe.TilgangsgruppeService;
import no.fint.p360.data.noark.codes.tilgangsrestriksjon.TilgangsrestriksjonService;
import no.fint.p360.data.noark.codes.tilknyttetregistreringsom.TilknyttetRegistreringSomService;
import no.fint.p360.data.noark.codes.variantformat.VariantformatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KodeverkRepository {

    @Autowired
    private SaksmappetypeService saksmappetypeService;

    @Autowired
    private SaksStatusService saksStatusService;

    @Autowired
    private DokumentstatusService dokumentstatusService;

    @Autowired
    private DokumenttypeService dokumenttypeService;

    @Autowired
    private JournalpostTypeService journalpostTypeService;

    @Autowired
    private KorrespondansepartTypeService korrespondansepartTypeService;

    @Autowired
    private PartRolleService partRolleService;

    @Autowired
    private JournalStatusService journalStatusService;

    @Autowired
    private TilknyttetRegistreringSomService tilknyttetRegistreringSomService;

    @Autowired
    private TilgangsrestriksjonService tilgangsrestriksjonService;

    @Autowired
    private SkjermingshjemmelService skjermingshjemmelService;

    @Autowired
    private MerknadstypeService merknadstypeService;

    @Autowired
    private VariantformatService variantformatService;

    @Autowired
    private CaseCategoryService caseCategoryService;

    @Autowired
    private KlassifikasjonssystemService klassifikasjonssystemService;

    @Autowired
    private KlasseService klasseService;

    @Autowired
    private FilformatService filformatService;

    @Autowired
    private ResponseCodeService responseCodeService;

    @Autowired
    private TilgangsgruppeService tilgangsgruppeService;

    @Getter
    private List<SaksmappetypeResource> saksmappetype;

    @Getter
    private List<SaksstatusResource> saksstatus;

    @Getter
    private List<DokumentStatusResource> dokumentStatus;

    @Getter
    private List<DokumentTypeResource> dokumentType;

    @Getter
    private List<JournalpostTypeResource> journalpostType;

    @Getter
    private List<KorrespondansepartTypeResource> korrespondansepartType;

    @Getter
    private List<PartRolleResource> partRolle;

    @Getter
    private List<TilknyttetRegistreringSomResource> tilknyttetRegistreringSom;

    @Getter
    private List<JournalStatusResource> journalStatus;

    @Getter
    private List<TilgangsrestriksjonResource> tilgangsrestriksjon;

    @Getter
    private List<SkjermingshjemmelResource> skjermingshjemmel;

    @Getter
    private List<MerknadstypeResource> merknadstype;

    @Getter
    private List<VariantformatResource> variantformat;

    @Getter
    private List<KlassifikasjonssystemResource> klassifikasjonssystem;

    @Getter
    private List<KlasseResource> klasse;

    @Getter
    private List<FilformatResource> filformat;

    @Getter
    private List<String> avskrivingsmate;

    @Getter
    private List<TilgangsgruppeResource> tilgangsgruppe;

    private transient boolean healthy = false;

    @Scheduled(initialDelay = 10000, fixedDelayString = "${fint.kodeverk.refresh-interval:1500000}")
    public void refresh() {
        saksmappetype = saksmappetypeService.getCaseTypeTable().toList();
        saksstatus = saksStatusService.getCaseStatusTable().toList();
        dokumentStatus = dokumentstatusService.getDocumentStatusTable().toList();
        dokumentType = dokumenttypeService.getDocumenttypeTable().toList();
        journalpostType = journalpostTypeService.getDocumentCategoryTable().toList();
        korrespondansepartType = korrespondansepartTypeService.getKorrespondansepartType().toList();
        journalStatus = journalStatusService.getJournalStatusTable().toList();
        tilknyttetRegistreringSom = tilknyttetRegistreringSomService.getDocumentRelationTable().toList();
        partRolle = partRolleService.getPartRolle().toList();
        merknadstype = merknadstypeService.getMerknadstype().toList();
        tilgangsrestriksjon = tilgangsrestriksjonService.getAccessCodeTable().toList();
        skjermingshjemmel = skjermingshjemmelService.getLawTable().toList();
        variantformat = variantformatService.getVersionFormatTable().toList();
        klassifikasjonssystem = klassifikasjonssystemService.getKlassifikasjonssystem().toList();
        klasse = klasseService.getKlasse().toList();
        filformat = filformatService.getFilformatTable().toList();
        avskrivingsmate = responseCodeService.getResponseCodeTable().toList();
        log.info("Refreshed code lists");
        log.info("Case Category Table: {}", caseCategoryService.getCaseCategoryTable().collect(Collectors.joining(", ")));
        tilgangsgruppe = tilgangsgruppeService.getTilgangsgruppeResource().toList();
        healthy = true;
    }

    public boolean health() {
        return healthy;
    }
}
