package ec.gob.igm.rrhh.consultorio.web.util;

import java.lang.reflect.Field;

/**
 * Utilidades para manejo de valores "S"/"N" y lectura de propiedades Boolean
 * desde la entidad FichaOcupacional (o el bean que se pase).
 *
 * En Oracle, "" suele tratarse como NULL. Por convención, devolvemos "N".
 */
public final class SnUtils {

    private SnUtils() {}

    public static boolean isS(String v) {
        return "S".equalsIgnoreCase(trimToNull(v));
    }

    public static String snNoNull(String v) {
        return isS(v) ? "S" : "N";
    }

    public static String fromBoolean(Boolean b) {
        return (b != null && b) ? "S" : "N";
    }

    /**
     * Lee un campo Boolean por reflexión (por nombre exacto) en el objeto target.
     * Retorna null si no existe o no es accesible.
     */
    public static Boolean readBooleanField(Object target, String fieldName) {
        if (target == null || fieldName == null || fieldName.isBlank()) return null;
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                Object val = f.get(target);
                return (val instanceof Boolean) ? (Boolean) val : null;
            } catch (NoSuchFieldException ex) {
                c = c.getSuperclass();
            } catch (IllegalAccessException ex) {
                return null;
            }
        }
        return null;
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
