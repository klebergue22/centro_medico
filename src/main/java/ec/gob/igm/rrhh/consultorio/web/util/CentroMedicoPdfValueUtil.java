package ec.gob.igm.rrhh.consultorio.web.util;

public final class CentroMedicoPdfValueUtil {

    private CentroMedicoPdfValueUtil() {
    }

    public static String safe(String s) {
        if (s == null) {
            return "";
        }
        String out = s;
        out = out.replace("&", "&amp;");
        out = out.replace("<", "&lt;");
        out = out.replace(">", "&gt;");
        out = out.replace("\"", "&quot;");
        out = out.replace("'", "&#39;");
        return out;
    }

    public static String markX(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return "";
        }
        s = s.toUpperCase();
        if ("S".equals(s) || "SI".equals(s) || "TRUE".equals(s) || "1".equals(s) || "X".equals(s) || "✔".equals(s)) {
            return "X";
        }
        return "";
    }

    public static boolean isYes(Object v) {
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toUpperCase();
        return "S".equals(s) || "SI".equals(s) || "TRUE".equals(s) || "1".equals(s) || "X".equals(s) || "✔".equals(s);
    }

    public static boolean isNo(Object v) {
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toUpperCase();
        return "N".equals(s) || "NO".equals(s) || "FALSE".equals(s) || "0".equals(s);
    }

    public static String normalizarXhtml(String html) {
        if (html == null) {
            return "";
        }
        String out = html;
        out = out.replace("&nbsp;", " ");
        out = out.replaceAll("(?i)<br(\\s*)>", "<br/>");
        out = out.replaceAll("(?i)<hr(\\s*)>", "<hr/>");
        return out;
    }
}
