package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class PdfTextUtil: gestiona la construcción y renderización de documentos PDF.
 */
public final class PdfTextUtil {

    private PdfTextUtil() {
    }

    public static String safePdf(String s) {
        return s == null ? "" : escHtml(s.trim());
    }

    public static String safeDate(Date d) {
        if (d == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "EC")).format(d);
    }

    public static String safeNum(Object n) {
        return n == null ? "" : String.valueOf(n);
    }

    public static String escHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String normalizarXhtmlPdf(String html) {
        if (html == null) {
            return "";
        }
        return html.replace("\u00A0", " ");
    }

    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String preferCurrent(String current, String legacy) {
        return current != null ? current : legacy;
    }
}
