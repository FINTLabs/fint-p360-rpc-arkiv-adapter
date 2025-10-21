package no.novari.p360.handler.noark;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.arkiv.noark.NoarkActions;
import no.fint.model.resource.FintLinks;
import no.novari.p360.data.exception.CaseNotFound;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.data.exception.IllegalCaseNumberFormat;
import no.novari.p360.data.noark.sak.SakFactory;
import no.novari.p360.handler.Handler;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.CaseQueryService;
import no.novari.p360.service.FilterSetService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

@Slf4j
@Service
public class GetSakHandler implements Handler {
    private final SakFactory sakFactory;
    private final CaseQueryService caseQueryService;
    private final FilterSet filterSet;

    private final MeterRegistry meterRegistry;
    private final Timer.Builder getSakTimer;

    public GetSakHandler(SakFactory sakFactory, CaseQueryService caseQueryService, FilterSetService filterSetService,
                         MeterRegistry meterRegistry) {
        this.sakFactory = sakFactory;
        this.caseQueryService = caseQueryService;
        filterSet = filterSetService.getDefaultFilterSet();

        this.meterRegistry = meterRegistry;
        getSakTimer = Timer.builder("fint.arkiv.sak.timer")
                .description("The P360 Archive Sak Timer");
    }

    @Override
    public void accept(Event<FintLinks> response) {
        String query = response.getQuery();
        log.debug("Try to get a sak based on this query (and we do even counting and do some time analysis): {}", query);

        Timer.Sample sample = Timer.start(meterRegistry);

        if (!caseQueryService.isValidQuery(query)) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("BAD_REQUEST");
            response.setMessage("Invalid query: " + query);
            return;
        }
        response.setData(new LinkedList<>());
        try {
            caseQueryService.query(filterSet, query).map(sakFactory::toFintResource).forEach(response::addData);
            response.setResponseStatus(ResponseStatus.ACCEPTED);
        } catch (CaseNotFound e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setStatusCode("NOT_FOUND");
            response.setMessage(e.getMessage());
        } catch (GetDocumentException | IllegalCaseNumberFormat e) {
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage(e.getMessage());
        } finally {
            sample.stop(getSakTimer.tag("request", "getCase")
                    .tag("status", response.getStatus().name())
                    .tag("statusCode", response.getStatusCode() != null ? response.getStatusCode() : "N/A")
                    .register(meterRegistry));
        }
    }

    @Override
    public Set<String> actions() {
        return Collections.singleton(NoarkActions.GET_SAK.name());
    }

}

