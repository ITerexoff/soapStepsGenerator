package com.example.soapStepsGenerator.external.utils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

//fixme include temporary, need to exclude from classLoader
public final class DateUtils {

    public static ZonedDateTime toZoneDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime();
    }

    public static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return toZoneDateTime(xmlGregorianCalendar).toOffsetDateTime();
    }

}
