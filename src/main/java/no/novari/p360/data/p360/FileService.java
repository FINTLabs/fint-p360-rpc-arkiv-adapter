package no.novari.p360.data.p360;

import lombok.extern.slf4j.Slf4j;
import no.novari.p360.AdapterProps;
import no.novari.p360.data.exception.FileNotFound;
import no.novari.p360.model.FilterSet;
import no.novari.p360.service.FilterSetService;
import no.p360.model.FileService.File;
import no.p360.model.FileService.GetFileWithMetadataArgs;
import no.p360.model.FileService.GetFileWithMetadataResponse;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileService extends P360Service {

    private final AdapterProps props;
    private final FilterSet filterSet;

    public FileService(AdapterProps props, FilterSetService filterSetService) {
        this.props = props;
        filterSet = filterSetService.getDefaultFilterSet();
    }

    public File getFileByRecNo(String recNo) {
        log.info("Retrieving {} ...", recNo);
        GetFileWithMetadataArgs getFileWithMetadataArgs = new GetFileWithMetadataArgs();
        getFileWithMetadataArgs.setRecno(Integer.parseInt(recNo));
        getFileWithMetadataArgs.setIncludeFileData(true);
        // TODO Rejected by P360: getFileWithMetadataArgs.setADContextUser(props.getP360User());

        GetFileWithMetadataResponse fileWithMetadata = call(filterSet, "FileService/GetFileWithMetadata", getFileWithMetadataArgs, GetFileWithMetadataResponse.class);

        if (fileWithMetadata.getSuccessful()) {
            log.info("Retrieving {} successfully", recNo);
            return fileWithMetadata.getFile();
        }

        log.info("Retrieving {} failed: {}", recNo, fileWithMetadata.getErrorMessage());
        throw new FileNotFound(fileWithMetadata.getErrorMessage());
    }

    public boolean ping()  {
        return getHealth(filterSet, "FileService/Ping");
    }
}
