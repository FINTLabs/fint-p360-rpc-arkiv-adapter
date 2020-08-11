package no.fint.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.AdapterProps;
import no.fint.p360.data.exception.FileNotFound;
import no.p360.model.FileService.File;
import no.p360.model.FileService.GetFileWithMetadataArgs;
import no.p360.model.FileService.GetFileWithMetadataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileService extends P360Service {

    @Autowired
    private AdapterProps props;

    public File getFileByRecNo(String recNo) {
        log.info("Retrieving {} ...", recNo);
        GetFileWithMetadataArgs getFileWithMetadataArgs = new GetFileWithMetadataArgs();
        getFileWithMetadataArgs.setRecno(Integer.parseInt(recNo));
        getFileWithMetadataArgs.setIncludeFileData(true);
        getFileWithMetadataArgs.setADContextUser(props.getP360User());

        GetFileWithMetadataResponse fileWithMetadata = call("FileService/GetFileWithMetadata", getFileWithMetadataArgs, GetFileWithMetadataResponse.class);

        if (fileWithMetadata.getSuccessful()) {
            log.info("Retrieving {} successfully", recNo);
            return fileWithMetadata.getFile();
        }

        log.info("Retrieving {} failed: {}", recNo, fileWithMetadata.getErrorDetails());
        throw new FileNotFound(fileWithMetadata.getErrorMessage());
    }

    public boolean ping()  {
        return getHealth("FileService/Ping");
    }
}
