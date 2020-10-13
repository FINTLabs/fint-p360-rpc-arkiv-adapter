package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.FintLinks;
import no.fint.p360.service.CaseQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Slf4j
@Service
public class TilskuddFredaHusPrivatEieService {

    @Autowired
    private TilskuddFredaBygningPrivatEieFactory tilskuddFredaBygningPrivatEieFactory;

    @Autowired
    private CaseQueryService caseQueryService;


    public void getTilskuddFredaHusPrivatEieForQuery(String query, Event<FintLinks> response) {
        response.setData(new LinkedList<>());
        caseQueryService.query(query).map(tilskuddFredaBygningPrivatEieFactory::toFintResource).forEach(response::addData);
        response.setResponseStatus(ResponseStatus.ACCEPTED);
    }
}
