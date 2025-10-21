package no.novari.p360.data.utilities;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.felles.kodeverk.iso.Landkode;
import no.fint.model.felles.kompleksedatatyper.*;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource;
import no.p360.model.CaseService.Address;
import no.p360.model.ContactService.*;
import no.p360.model.DocumentService.Contact__1;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public enum FintUtils {
    ;

    private static final DateTimeFormatter formatter = createDateTimeFormatter();

    private static DateTimeFormatter createDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][x]").withZone(ZoneOffset.UTC);
    }

    public static Identifikator createIdentifikator(String value) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(value);
        return identifikator;
    }

    public static Periode createPeriode(Date fromDate, Date toDate) {
        Periode periode = new Periode();
        periode.setStart(fromDate);
        periode.setSlutt(toDate);
        return periode;
    }

    public static Periode createPeriode(Date fromDate) {
        Periode periode = new Periode();
        periode.setStart(fromDate);
        return periode;
    }

    public static Date parseDate(String value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH);
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            log.warn("Unable to parse date {}", value);
            return null;
        }
    }

    public static Date parseIsoDate(String value) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, formatter);
        return Date.from(zonedDateTime.toInstant());
    }

    public static String formatIsoDate(Date date) {
        return formatter.format(date.toInstant());
    }

    public static Kontaktinformasjon createKontaktinformasjon(PrivatePerson result) {
        return getKontaktinformasjon(result.getEmail(), result.getMobilePhone(), result.getPhoneNumber());
    }

    public static Kontaktinformasjon createKontaktinformasjon(ContactPerson result) {
        return getKontaktinformasjon(result.getEmail(), result.getMobilePhone(), result.getPhoneNumber());
    }

    public static Kontaktinformasjon createKontaktinformasjon(Enterprise result) {
        return getKontaktinformasjon(result.getEmail(), result.getMobilePhone(), result.getPhoneNumber());
    }

    public static AdresseResource createAdresse(PrivatePerson result) {
        return optionalValue(result.getPostAddress()).map(FintUtils::createAdresseResource).orElse(null);
    }

    public static AdresseResource createAdresse(ContactPerson result) {
        return optionalValue(result.getPostAddress()).map(FintUtils::createAddressResource).orElse(null);
    }

    public static AdresseResource createAdresseResource(Contact__1 address) {
        AdresseResource adresseResource = new AdresseResource();
        adresseResource.setAdresselinje(Collections.singletonList(address.getAddress()));
        adresseResource.setPoststed(address.getZipPlace());
        adresseResource.setPostnummer(address.getZipCode());
        if (StringUtils.isNotBlank(address.getCountry()))
            adresseResource.addLand(Link.with(Landkode.class, "systemid", address.getCountry()));

        return cleanAdresseResource(adresseResource);
    }

    private static AdresseResource createAdresseResource(PostAddress__2 address) {
        PostAddress postAddress = new PostAddress();
        postAddress.setStreetAddress(address.getStreetAddress());
        postAddress.setZipPlace(address.getZipPlace());
        postAddress.setZipCode(address.getZipCode());
        return createAddressResource(postAddress);
    }

    private static AdresseResource createAdresseResource(PostAddress__1 address) {
        PostAddress postAddress = new PostAddress();
        postAddress.setStreetAddress(address.getStreetAddress());
        postAddress.setZipPlace(address.getZipPlace());
        postAddress.setZipCode(address.getZipCode());
        return createAddressResource(postAddress);
    }

    private static AdresseResource createAddressResource(PostAddress address) {
        AdresseResource adresseResource = new AdresseResource();
        adresseResource.setAdresselinje(Collections.singletonList(address.getStreetAddress()));
        adresseResource.setPoststed(address.getZipPlace());
        adresseResource.setPostnummer(address.getZipCode());

        return cleanAdresseResource(adresseResource);
    }

    public static AdresseResource createAdresseResource(Address address) {
        AdresseResource adresseResource = new AdresseResource();
        adresseResource.setAdresselinje(Collections.singletonList(address.getStreetAddress()));
        adresseResource.setPoststed(address.getZipPlace());
        adresseResource.setPostnummer(address.getZipCode());
        if (StringUtils.isNotBlank(address.getCountry())) {
            adresseResource.addLand(Link.with(Landkode.class, "systemid", address.getCountry()));
        }
        return cleanAdresseResource(adresseResource);
    }

    public static AdresseResource createAdresse(Enterprise result) {
        return optionalValue(result.getPostAddress()).map(FintUtils::createAdresseResource).orElse(null);
    }

    private static AdresseResource cleanAdresseResource(AdresseResource adresseResource) {
        AdresseResource cleanAdresseResource = new AdresseResource();

        if (adresseResource.getAdresselinje() != null && adresseResource.getAdresselinje().size() == 1) {
            if (StringUtils.isNotBlank(adresseResource.getAdresselinje().get(0))) {
                cleanAdresseResource.setAdresselinje(adresseResource.getAdresselinje());
            }
        }

        if (adresseResource.getPostnummer() != null && !adresseResource.getPostnummer().isBlank()) {
            cleanAdresseResource.setPostnummer(adresseResource.getPostnummer());
        }

        if (adresseResource.getPoststed() != null && !adresseResource.getPoststed().isBlank()) {
            cleanAdresseResource.setPoststed(adresseResource.getPoststed());
        }

        return cleanAdresseResource;
    }

    public static Personnavn parsePersonnavn(String input) {
        Personnavn personnavn = new Personnavn();
        if (StringUtils.contains(input, ", ")) {
            personnavn.setEtternavn(StringUtils.substringBefore(input, ", "));
            personnavn.setFornavn(StringUtils.substringAfter(input, ", "));
        } else if (StringUtils.contains(input, ' ')) {
            personnavn.setEtternavn(StringUtils.substringAfterLast(input, " "));
            personnavn.setFornavn(StringUtils.substringBeforeLast(input, " "));
        } else {
            throw new IllegalArgumentException("Ugyldig personnavn: " + input);
        }
        return personnavn;
    }

    public static String getFullNameString(PrivatePerson result) {
        return String.format("%s %s", result.getFirstName(), result.getLastName());
    }

    public static String getFullNameString(ContactPerson result) {
        return String.format("%s %s", result.getFirstName(), result.getLastName());
    }

    public static String getKontaktpersonString(Enterprise result) {

        if (!result.getContactRelations().isEmpty()) {
            return result.getContactRelations().get(0).getName();
        }
        return "";
    }

    public static <T> Optional<T> optionalValue(T element) {
        return Optional.ofNullable(element);
    }

    // FIXME: 2019-05-08 Must handle if all three elements is empty. Then we should return null
    private static Kontaktinformasjon getKontaktinformasjon(String email, String mobilePhone, String phoneNumber) {
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        optionalValue(email).ifPresent(kontaktinformasjon::setEpostadresse);
        optionalValue(mobilePhone).ifPresent(kontaktinformasjon::setMobiltelefonnummer);
        optionalValue(phoneNumber).ifPresent(kontaktinformasjon::setTelefonnummer);
        return kontaktinformasjon;
    }

    public static Kontaktinformasjon createKontaktinformasjon(Contact__1 contact) {
        return getKontaktinformasjon(contact.getEmail(), null, null);
    }

    public static Optional<String> getIdFromLink(List<Link> links){
        return links
                .stream()
                .filter(Objects::nonNull)
                .map(Link::getHref)
                .filter(StringUtils::isNotBlank)
                .map(s -> StringUtils.substringAfterLast(s, "/"))
                .findFirst();
    }

}
