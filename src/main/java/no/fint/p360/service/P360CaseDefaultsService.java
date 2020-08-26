package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaultsService;
import no.fint.arkiv.CaseProperties;
import no.fint.p360.data.utilities.Constants;
import no.p360.model.CaseService.ArchiveCode;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class P360CaseDefaultsService extends CaseDefaultsService {

    public CreateCaseArgs applyDefaultsToCreateCaseParameter(CaseProperties properties, CreateCaseArgs createCaseArgs) {

        createCaseArgs.setKeywords(Arrays.asList(properties.getNoekkelord()));
        createCaseArgs.setFiledOnPaper(false);
        createCaseArgs.setCaseType(Constants.CASE_TYPE_NOARK);

        if (properties.getKlassifikasjon() != null && properties.getKlasse() != null) {
            List<ArchiveCode> archiveCodes = new ArrayList<>(properties.getKlasse().length);
            for (int i = 0; i < properties.getKlasse().length; i++) {
                String code = properties.getKlasse()[i];
                ArchiveCode archiveCode = new ArchiveCode();
                archiveCode.setArchiveType(
                        properties.getKlassifikasjon()[
                                Math.min(
                                        properties.getKlassifikasjon().length, i
                                )]
                );
                archiveCode.setArchiveCode(code);
                archiveCode.setSort(i+1);
                archiveCode.setIsManualText(false);
                archiveCodes.add(archiveCode);
            }
            createCaseArgs.setArchiveCodes(archiveCodes);
        }

        return createCaseArgs;
    }


}
