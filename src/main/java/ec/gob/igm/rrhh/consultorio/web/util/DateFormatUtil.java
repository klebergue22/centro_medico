package ec.gob.igm.rrhh.consultorio.web.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public final class DateFormatUtil {

    private DateFormatUtil() {
    }

    public static Date toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date d) {
            return d;
        }
        if (value instanceof LocalDate ld) {
            return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    public static String fmtDate(Date d) {
        if (d == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(d);
    }
}
