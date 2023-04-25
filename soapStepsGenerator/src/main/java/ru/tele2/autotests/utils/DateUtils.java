package ru.tele2.autotests.utils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

//fixme временно подключил, нужно брать из classLoader-а
public final class DateUtils {

    public static ZonedDateTime toZoneDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime();
    }

    public static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return toZoneDateTime(xmlGregorianCalendar).toOffsetDateTime();
    }

}
