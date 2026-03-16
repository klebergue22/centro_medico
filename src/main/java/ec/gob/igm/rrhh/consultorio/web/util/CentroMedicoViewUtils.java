package ec.gob.igm.rrhh.consultorio.web.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Class CentroMedicoViewUtils: provee utilidades de apoyo para la capa web.
 */
public final class CentroMedicoViewUtils {

    private CentroMedicoViewUtils() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean esVacio(String s) {
        return isBlank(s);
    }

    public static <T> T getSafe(List<T> list, int idx) {
        if (list == null || idx < 0 || idx >= list.size()) {
            return null;
        }
        return list.get(idx);
    }

    public static boolean isTrue(Boolean b) {
        return b != null && b;
    }

    public static String sn(Boolean b) {
        return Boolean.TRUE.equals(b) ? "S" : "N";
    }

    public static String sn(boolean b) {
        return b ? "S" : "N";
    }

    public static String sn(Object boolOrString) {
        if (boolOrString == null) {
            return "NO";
        }
        String s = String.valueOf(boolOrString).trim();
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "X".equalsIgnoreCase(s) || "SI".equalsIgnoreCase(s)) {
            return "SI";
        }
        return "NO";
    }

    public static String s(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof Date) {
            return new SimpleDateFormat("dd/MM/yyyy").format((Date) v);
        }
        return String.valueOf(v);
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

    public static void putArray1Based(Map<String, String> rep, String prefix, String[] arr, UnaryOperator<String> valueMapper) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + (i + 1), valueMapper.apply(arr[i]));
        }
    }

    public static void putIntArray1Based(Map<String, String> rep, String prefix, Integer[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + (i + 1), (arr[i] == null) ? "" : String.valueOf(arr[i]));
        }
    }

    public static void putBoolArray1Based(Map<String, String> rep, String prefix, Boolean[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + (i + 1), Boolean.TRUE.equals(arr[i]) ? "X" : "");
        }
    }

    public static boolean isTruthyMark(String v) {
        if (v == null) {
            return false;
        }
        String s = v.trim();
        if (s.isEmpty()) {
            return false;
        }
        s = s.toUpperCase();
        return "TRUE".equals(s) || "1".equals(s) || "X".equals(s) || "SI".equals(s) || "S".equals(s);
    }
}
