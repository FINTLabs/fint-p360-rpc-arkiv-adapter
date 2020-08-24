package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.arkiv.SaksmappeResource;
import no.fint.p360.data.CaseProperties;
import no.fint.p360.data.utilities.Constants;
import no.p360.model.CaseService.ArchiveCode;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class CaseDefaultsService {

    public void applyDefaultsForCreation(CaseProperties properties, SaksmappeResource resource) {
        if (properties == null) {
            return;
        }
        if (resource.getSaksstatus().isEmpty()) {
            resource.addSaksstatus(Link.with(
                    Saksstatus.class,
                    "systemid",
                    properties.getSaksstatus()
            ));
        }
        if (resource.getArkivdel().isEmpty()) {
            resource.addArkivdel(Link.with(
                    Arkivdel.class,
                    "systemid",
                    properties.getArkivdel()
            ));
        }
        if (resource.getAdministrativEnhet().isEmpty()) {
            resource.addAdministrativEnhet(Link.with(
                    AdministrativEnhet.class,
                    "systemid",
                    properties.getAdministrativEnhet()
            ));
        }
        applyDefaultsForUpdate(properties, resource);
    }

    public void applyDefaultsForUpdate(CaseProperties properties, SaksmappeResource resource) {
        if (properties == null) {
            return;
        }
        if (resource.getJournalpost() == null || resource.getJournalpost().isEmpty()) {
            return;
        }
        resource.getJournalpost().forEach(journalpost -> {
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

    public CreateCaseArgs applyDefaultsToCreateCaseParameter(CaseProperties properties, CreateCaseArgs createCaseArgs) {

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

        return createCaseArgs;
    }


}
