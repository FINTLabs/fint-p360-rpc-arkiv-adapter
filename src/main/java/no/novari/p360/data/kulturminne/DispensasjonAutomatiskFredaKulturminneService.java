package no.novari.p360.data.kulturminne;

import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.arkiv.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class DispensasjonAutomatiskFredaKulturminneService {

    private final DispensasjonAutomatiskFredaKulturminneFactory dispensasjonAutomatiskFredaKulturminneFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    public DispensasjonAutomatiskFredaKulturminneService(
            DispensasjonAutomatiskFredaKulturminneFactory dispensasjonAutomatiskFredaKulturminneFactory,
            CaseQueryService caseQueryService, FilterSetService filterSetService) {
        this.dispensasjonAutomatiskFredaKulturminneFactory = dispensasjonAutomatiskFredaKulturminneFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getFilterSetForCaseType(DispensasjonAutomatiskFredaKulturminneResource.class);
    }

    public void getDispensasjonAutomatiskFredaKulturminneForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(filterSet, query).map(dispensasjonAutomatiskFredaKulturminneFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
