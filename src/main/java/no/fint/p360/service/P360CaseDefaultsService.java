package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.arkiv.CaseDefaultsService;
import no.fint.arkiv.CaseProperties;
import no.fint.p360.data.utilities.Constants;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class P360CaseDefaultsService extends CaseDefaultsService {

    public CreateCaseArgs applyDefaultsToCreateCaseParameter(CaseProperties properties, CreateCaseArgs createCaseArgs) {

        createCaseArgs.setKeywords(Arrays.asList(properties.getNoekkelord()));
        createCaseArgs.setFiledOnPaper(false);
        createCaseArgs.setCaseType(Constants.CASE_TYPE_NOARK);

        return createCaseArgs;
    }


}
