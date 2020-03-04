package no.fint.p360.data.kulturminne;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.p360.data.exception.*;
import no.fint.p360.data.p360.CaseService;
import no.fint.p360.data.p360.DocumentService;
import no.fint.p360.data.utilities.Constants;
import no.fint.p360.data.utilities.FintUtils;
import no.p360.model.CaseService.Case;
import no.p360.model.DocumentService.CreateDocumentArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TilskuddfartoyService {

    @Autowired
    private CaseService caseService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private TilskuddFartoyFactory tilskuddFartoyFactory;

    public TilskuddFartoyResource createTilskuddFartoyCase(TilskuddFartoyResource tilskuddFartoy) throws NotTilskuddfartoyException, CreateCaseException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, CreateDocumentException, GetDocumentException, IllegalCaseNumberFormat {

        String caseNumber = caseService.createCase(tilskuddFartoyFactory.convertToCreateCase(tilskuddFartoy));

        for (JournalpostResource journalpostResource : tilskuddFartoy.getJournalpost()) {
            CreateDocumentArgs tilskuddFartoyDocument = tilskuddFartoyFactory.convertToCreateDocument(journalpostResource, caseNumber);
            documentService.createDocument(tilskuddFartoyDocument);
        }
        return getTilskuddFartoyCaseByCaseNumber(caseNumber);
    }

    public TilskuddFartoyResource updateTilskuddFartoyCase(String caseNumber, TilskuddFartoyResource tilskuddFartoyResource) throws NotTilskuddfartoyException, GetTilskuddFartoyNotFoundException, GetTilskuddFartoyException, CreateDocumentException, GetDocumentException, IllegalCaseNumberFormat {
        Case caseByCaseNumber = caseService.getCaseByCaseNumber(caseNumber);
        if (!isTilskuddFartoy(caseByCaseNumber)) {
            throw new NotTilskuddfartoyException("Ikke en Tilskuddfartøy sak: " + caseNumber);
        }
        for (JournalpostResource journalpostResource : tilskuddFartoyResource.getJournalpost()) {
            CreateDocumentArgs tilskuddFartoyDocument = tilskuddFartoyFactory.convertToCreateDocument(journalpostResource, caseNumber);
            documentService.createDocument(tilskuddFartoyDocument);
        }
        return getTilskuddFartoyCaseByCaseNumber(caseNumber);
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByCaseNumber(String caseNumber) throws NotTilskuddfartoyException, GetDocumentException, IllegalCaseNumberFormat, GetTilskuddFartoyNotFoundException {
        Case caseByCaseNumber = caseService.getCaseByCaseNumber(caseNumber);

        if (isTilskuddFartoy(caseByCaseNumber)) {
            return tilskuddFartoyFactory.toFintResource(caseByCaseNumber);
        }

        throw new NotTilskuddfartoyException(String.format("MappeId %s er ikke en Tilskuddfartøy sak", caseNumber));
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseByExternalId(String externalId) throws NotTilskuddfartoyException, GetDocumentException, IllegalCaseNumberFormat, GetTilskuddFartoyNotFoundException {
        Case caseResult = caseService.getCaseByExternalId(externalId);

        // TODO Delegate this to tilskuddFartoyFactory
        if (isTilskuddFartoy(caseResult)) {
            return tilskuddFartoyFactory.toFintResource(caseResult);
        }

        throw new NotTilskuddfartoyException("Søknadsnummer " + externalId + " er ikke en Tilskuddfartøy sak");
    }

    public TilskuddFartoyResource getTilskuddFartoyCaseBySystemId(String systemId) throws NotTilskuddfartoyException, GetDocumentException, IllegalCaseNumberFormat, GetTilskuddFartoyNotFoundException {
        Case sakBySystemId = caseService.getCaseBySystemId(systemId);

        // TODO Delegate this to tilskuddFartoyFactory
        if (isTilskuddFartoy(sakBySystemId)) {
            return tilskuddFartoyFactory.toFintResource(sakBySystemId);
        }
        throw new NotTilskuddfartoyException(String.format("SystemId %s er ikke en Tilskuddfartøy sak", systemId));
    }

    public List<TilskuddFartoyResource> searchTilskuddFartoyCaseByTitle(Map<String, String> query) throws GetDocumentException, IllegalCaseNumberFormat {

        String title = String.format("%%%s%%", query.get("title"));
        String maxReturnedCases = query.getOrDefault("maxResult", "10");

        return tilskuddFartoyFactory.toFintResourceList(
                caseService.getCasesByTitle(title, query.getOrDefault(maxReturnedCases, "10"))
                        .stream()
                        .filter(this::isTilskuddFartoy)
                        .collect(Collectors.toList())
        );
    }

    // TODO: 2019-05-11 Should we check for both archive classification and external id (is it a digisak)
    // TODO Compare with CaseProperties
    private boolean isTilskuddFartoy(Case caseResult) {

        if (FintUtils.optionalValue(caseResult.getExternalId()).isPresent() && FintUtils.optionalValue(caseResult.getArchiveCodes()).isPresent()) {
            return caseResult.getExternalId().getType().equals(Constants.EXTERNAL_ID_TYPE);
        }

        return false;

    }

}
