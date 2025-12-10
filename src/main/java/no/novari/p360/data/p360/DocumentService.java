package no.novari.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.novari.p360.data.exception.CreateDocumentException;
import no.novari.p360.data.exception.GetDocumentException;
import no.novari.p360.model.FilterSet;
import no.p360.model.DocumentService.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentService extends P360Service {

    public void createDocument(FilterSet filterSet,
                               CreateDocumentArgs createDocumentArgs) throws CreateDocumentException {
        log.debug("Create Document: {}", createDocumentArgs);
        CreateDocumentResponse createDocumentResponse = call(filterSet,
                "DocumentService/CreateDocument",
                createDocumentArgs,
                CreateDocumentResponse.class);
        log.debug("Create Document Result: {}", createDocumentResponse);

        if (createDocumentResponse.getSuccessful()) {
            log.info("Documents successfully created");
            return;
        }
        throw new CreateDocumentException(createDocumentResponse.getErrorMessage());
    }

    public Document__1 getDocumentBySystemId(FilterSet filterSet, String systemId) throws GetDocumentException {
        GetDocumentsArgs getDocumentsArgs = new GetDocumentsArgs();
        getDocumentsArgs.setRecno(Integer.valueOf(systemId));
        getDocumentsArgs.setIncludeRemarks(Boolean.TRUE);
        getDocumentsArgs.setIncludeCustomFields(Boolean.TRUE);

        GetDocumentsResponse getDocumentsResponse = call(filterSet,
                "DocumentService/GetDocuments",
                getDocumentsArgs,
                GetDocumentsResponse.class);

        log.debug("DocumentsResult: {}", getDocumentsResponse);
        if (!getDocumentsResponse.getSuccessful()) {
            throw new GetDocumentException(getDocumentsResponse.getErrorMessage());
        } else if (getDocumentsResponse.getDocuments().size() == 1) {
            return getDocumentsResponse.getDocuments().get(0);
        } else if (getDocumentsResponse.getTotalPageCount() != 1) {
            throw new GetDocumentException("Document could not be found");
        }
        throw new GetDocumentException("Cannot understand the GetDocuments response!");
    }

}
