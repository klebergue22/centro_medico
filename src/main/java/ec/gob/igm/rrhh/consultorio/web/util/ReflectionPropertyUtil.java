package ec.gob.igm.rrhh.consultorio.web.util;

public final class ReflectionPropertyUtil {

    private ReflectionPropertyUtil() {
    }

    public static String val(Object root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return "";
        }
        Object cur = root;
        for (String part : path.split("\\.")) {
            if (cur == null) {
                return "";
            }
            cur = readProperty(cur, part);
        }
        return cur == null ? "" : String.valueOf(cur);
    }

    public static Object readProperty(Object obj, String prop) {
        try {
            String m = "get" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
            return obj.getClass().getMethod(m).invoke(obj);
        } catch (Exception ignore) {
            try {
                String m = "is" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
                return obj.getClass().getMethod(m).invoke(obj);
            } catch (Exception ignore2) {
                try {
                    java.lang.reflect.Field f = obj.getClass().getDeclaredField(prop);
                    f.setAccessible(true);
                    return f.get(obj);
                } catch (Exception ignore3) {
                    return null;
                }
            }
        }
    }

    public static String getFichaStringByReflection(Object fo, String... getterNames) {
        if (fo == null || getterNames == null) {
            return null;
        }
        for (String g : getterNames) {
            if (g == null || g.trim().isEmpty()) {
                continue;
            }
            try {
                java.lang.reflect.Method m = fo.getClass().getMethod(g);
                Object r = m.invoke(fo);
                if (r != null) {
                    String s = String.valueOf(r);
                    if (!s.trim().isEmpty()) {
                        return s;
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }
}
