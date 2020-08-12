package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.CaseDefaults;
import no.fint.p360.data.CaseProperties;
import no.fint.p360.data.utilities.Constants;
import no.p360.model.CaseService.ArchiveCode;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TilskuddFartoyDefaults {
    @Autowired
    private CaseDefaults caseDefaults;

    private CaseProperties properties;

    @PostConstruct
    public void init() {
        log.info("Case Defaults: {}", caseDefaults);
        properties = caseDefaults.getCasetype().get("tilskudd-fartoy");
        log.info("Defaults for TilskuddFartoy: {}", properties);
    }

    public void applyDefaultsForCreation(TilskuddFartoyResource tilskuddFartoy) {
        if (tilskuddFartoy.getSaksstatus().isEmpty()) {
            tilskuddFartoy.addSaksstatus(Link.with(
                    Saksstatus.class,
                    "systemid",
                    properties.getSaksstatus()
            ));
        }
        if (tilskuddFartoy.getArkivdel().isEmpty()) {
            tilskuddFartoy.addArkivdel(Link.with(
                    Arkivdel.class,
                    "systemid",
                    properties.getArkivdel()
            ));
        }
        applyDefaultsForUpdate(tilskuddFartoy);
    }

    public void applyDefaultsForUpdate(TilskuddFartoyResource tilskuddFartoy) {
        if (tilskuddFartoy.getJournalpost() == null || tilskuddFartoy.getJournalpost().isEmpty()) {
            return;
        }
        tilskuddFartoy.getJournalpost().forEach(journalpost -> {
            journalpost.getKorrespondansepart().forEach(korrespondanse -> {
                if (korrespondanse.getKorrespondanseparttype().isEmpty()) {
                    korrespondanse.addKorrespondanseparttype(Link.with(
                            KorrespondansepartType.class,
                            "systemid",
                            properties.getKorrespondansepartType()));
                }
            });
            journalpost.getDokumentbeskrivelse().forEach(dokumentbeskrivelse -> {
                if (dokumentbeskrivelse.getDokumentstatus().isEmpty()) {
                    dokumentbeskrivelse.addDokumentstatus(Link.with(
                            DokumentStatus.class,
                            "systemid",
                            properties.getDokumentstatus()
                    ));
                }
                if (dokumentbeskrivelse.getDokumentType().isEmpty()) {
                    dokumentbeskrivelse.addDokumentType(Link.with(
                            DokumentType.class,
                            "systemid",
                            properties.getDokumentType()
                    ));
                }
                if (dokumentbeskrivelse.getTilknyttetRegistreringSom().isEmpty()) {
                    dokumentbeskrivelse.addTilknyttetRegistreringSom(Link.with(
                            TilknyttetRegistreringSom.class,
                            "systemid",
                            properties.getTilknyttetRegistreringSom()
                    ));
                }
            });
            if (journalpost.getJournalposttype().isEmpty()) {
                journalpost.addJournalposttype(Link.with(
                        JournalpostType.class,
                        "systemid",
                        properties.getJournalpostType()));
            }
            if (journalpost.getJournalstatus().isEmpty()) {
                journalpost.addJournalstatus(Link.with(
                        JournalStatus.class,
                        "systemid",
                        properties.getJournalstatus()));
            }
            if (journalpost.getJournalenhet().isEmpty()) {
                journalpost.addJournalenhet(Link.with(
                        AdministrativEnhet.class,
                        "systemid",
                        properties.getAdministrativEnhet()
                ));
            }
            if (journalpost.getAdministrativEnhet().isEmpty()) {
                journalpost.addAdministrativEnhet(Link.with(
                        AdministrativEnhet.class,
                        "systemid",
                        properties.getAdministrativEnhet()
                ));
            }
            if (journalpost.getArkivdel().isEmpty()) {
                journalpost.addArkivdel(Link.with(
                        Arkivdel.class,
                        "systemid",
                        properties.getArkivdel()
                ));
            }
        });
    }

    public void applyDefaultsToCreateCaseParameter(CreateCaseArgs createCaseArgs) {

        createCaseArgs.setKeywords(Arrays.asList(properties.getNoekkelord()));
        createCaseArgs.setFiledOnPaper(false);
        createCaseArgs.setCaseType(Constants.CASE_TYPE_NOARK);

        List<ArchiveCode> archiveCodes = new ArrayList<>();
        ArchiveCode archiveCode = new ArchiveCode();
        archiveCode.setArchiveType(properties.getKlassifikasjon());
        archiveCode.setArchiveCode(properties.getKlasse());
        archiveCode.setSort(1);
        archiveCode.setIsManualText(false);
        archiveCodes.add(archiveCode);

        createCaseArgs.setArchiveCodes(archiveCodes);
    }
}
