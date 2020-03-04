package no.fint.p360.data.noark.codes.filformat;

import lombok.*;
import no.fint.model.FintMainObject;
import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FilformatResource extends Begrep implements FintMainObject, FintLinks {

    // Relations
    @Getter
    private final Map<String, List<Link>> links = createLinks();
}
