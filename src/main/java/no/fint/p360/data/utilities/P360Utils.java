package no.fint.p360.data.utilities;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.p360.model.CaseService.ExternalId;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;

public enum P360Utils {
    ;

/*    public static JAXBElement<ArrayOfstring> getKeywords(List<String> keywords) {
        ObjectFactory objectFactory = new ObjectFactory();

        ArrayOfstring keywordArray = objectFactory.createArrayOfstring();
        keywords.forEach(keywordArray.getString()::add);

        return objectFactory.createCaseParameterBaseKeywords(keywordArray);
    }

    public static URL getURL(String location) throws MalformedURLException {
        if (StringUtils.startsWithAny(location, "file:", "http:", "https:")) {
            return new URL(location);
        }
        return new URL("file:" + location);
    }
*/

    public static ExternalId getExternalIdParameter(Identifikator id) {

        ExternalId externalId = new ExternalId();
        externalId.setId(id.getIdentifikatorverdi());
        externalId.setType(Constants.EXTERNAL_ID_TYPE);

        return externalId;
    }

    /*
        public static JAXBElement<ArrayOfClassCodeParameter> getArchiveCodes(String type, String code) {
            ObjectFactory objectFactory = new ObjectFactory();

            ArrayOfClassCodeParameter arrayOfClassCodeParameter = objectFactory.createArrayOfClassCodeParameter();
            ClassCodeParameter classCodeParameter = objectFactory.createClassCodeParameter();

            classCodeParameter.setSort(1);
            classCodeParameter.setIsManualText(Boolean.FALSE);
            classCodeParameter.setArchiveCode(objectFactory.createClassCodeParameterArchiveCode(code));
            classCodeParameter.setArchiveType(objectFactory.createClassCodeParameterArchiveType(type));
            arrayOfClassCodeParameter.getClassCodeParameter().add(classCodeParameter);

            return objectFactory.createCaseParameterBaseArchiveCodes(arrayOfClassCodeParameter);
        }
    */
    public static <T> void applyParameterFromLink(List<Link> links, Consumer<String> consumer) {
        links.stream()
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .findFirst()
                .ifPresent(consumer);
    }
}
