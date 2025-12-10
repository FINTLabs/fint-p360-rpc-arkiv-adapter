package no.novari.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.arkiv.CaseDefaultsService;
import no.novari.fint.arkiv.CaseProperties;
import no.p360.model.CaseService.CreateCaseArgs;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class P360CaseDefaultsService extends CaseDefaultsService {

    public CreateCaseArgs applyDefaultsToCreateCaseParameter(CaseProperties properties, CreateCaseArgs createCaseArgs) {

        if (properties.getNoekkelord() != null) {
            createCaseArgs.setKeywords(Arrays.asList(properties.getNoekkelord()));
        }
        createCaseArgs.setFiledOnPaper(false);

        return createCaseArgs;
    }

}
