package no.fint.p360.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.p360.TitleFormats;
import no.fint.p360.data.utilities.BeanPropertyLookup;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TitleService {

    private final Map<String,String> formats;

    public TitleService(TitleFormats formats) {
        this.formats = formats.getFormat();
    }

    public <T> String getTitle(T object) {
        String type = resourceName(object);
        if (!formats.containsKey(type)) {
            throw new IllegalArgumentException("No format defined for " + type);
        }
        String title = new StringSubstitutor(new BeanPropertyLookup<>(object)).replace(formats.get(type));
        log.info("Title: '{}'", title);
        return title;
    }

    private String resourceName(Object object) {
        return StringUtils.removeEnd(StringUtils.lowerCase(object.getClass().getSimpleName()), "resource");
    }

    public void parseTitle(Object object, String title) {
        String format = formats.get(resourceName(object));
        if (StringUtils.isBlank(format)) {
            log.warn("No format defined for {}", resourceName(object));
            return;
        }
        Pattern names = Pattern.compile("\\$\\{([^}]+)}");
        Matcher nameMatcher = names.matcher(format);
        List<String> nameList = new ArrayList<>();
        while (nameMatcher.find()) {
            nameList.add(nameMatcher.group(1));
        }
        System.out.println("nameList = " + nameList);

        String pattern = RegExUtils.replaceAll(format, names, "(.+)");
        Pattern titlePattern = Pattern.compile("^" + pattern + "$");
        Matcher titleMatcher = titlePattern.matcher(title);
        if (titleMatcher.matches()) {
            for (int i = 1; i <= titleMatcher.groupCount(); ++i) {
                try {
                    BeanUtils.setProperty(object, nameList.get(i-1), titleMatcher.group(i));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
