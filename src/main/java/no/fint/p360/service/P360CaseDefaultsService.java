package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaultsService;
import no.fint.arkiv.CaseProperties;
import no.p360.model.CaseService.CreateCaseArgs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class P360CaseDefaultsService extends CaseDefaultsService {

    @Value("${fint.case.defaults.drosjeloyve.saksmappeType:Case}")
    private String saksmappeType;

    @Value("${fint.case.defaults.drosjeloyve.tilgangsgruppe:Alle}")
    private String tilgangsgruppe;

    public CreateCaseArgs applyDefaultsToCreateCaseParameter(CaseProperties properties, CreateCaseArgs createCaseArgs) {

        createCaseArgs.setKeywords(Arrays.asList(properties.getNoekkelord()));
        createCaseArgs.setFiledOnPaper(false);
        createCaseArgs.setCaseType(properties.getSaksmappeType());
        if (StringUtils.isNotBlank(saksmappeType)) {
            createCaseArgs.setCaseType(saksmappeType);
        }

        if (StringUtils.isNotBlank(tilgangsgruppe)) {
            createCaseArgs.setAccessGroup(tilgangsgruppe);
        }

        return createCaseArgs;
    }


}
