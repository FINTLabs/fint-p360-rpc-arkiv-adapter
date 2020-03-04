package no.fint.p360.data.noark.codes.filformat;

import no.fint.p360.data.p360.SupportService;
import no.fint.p360.data.utilities.BegrepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class FilformatService {
    @Autowired
    private SupportService supportService;

    @Value("${fint.p360.tables.file-format:code table: File Format}")
    private String fileFormatTable;

    public Stream<FilformatResource> getFilformatTable() {
        return supportService
                .getCodeTableRowResultStream(fileFormatTable)
                .map(BegrepMapper.mapValue(FilformatResource::new));
    }
}
