package ec.gob.igm.rrhh.consultorio.web.util;

/**
 * Class CentroMedicoTextUtil: provee utilidades de apoyo para la capa web.
 */
public final class CentroMedicoTextUtil {

    private CentroMedicoTextUtil() {
    }

    public static String esNulo(String s) {
        return s == null ? "" : s;
    }

    public static String[] splitEnDos(String valor) {
        String res1 = null;
        String res2 = null;

        if (!CentroMedicoViewUtils.isBlank(valor)) {
            String trimmed = valor.trim();
            String[] partes = trimmed.split("\\s+");
            if (partes.length == 1) {
                res1 = partes[0];
            } else if (partes.length > 1) {
                res1 = partes[0];
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < partes.length; i++) {
                    if (i > 1) {
                        sb.append(' ');
                    }
                    sb.append(partes[i]);
                }
                res2 = sb.toString();
            }
        }

        return new String[]{res1, res2};
    }

    public static String primerToken(String texto) {
        if (CentroMedicoViewUtils.esVacio(texto)) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        return partes[0];
    }

    public static String restoTokens(String texto) {
        if (CentroMedicoViewUtils.esVacio(texto)) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        if (partes.length <= 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < partes.length; i++) {
            if (i > 1) {
                sb.append(' ');
            }
            sb.append(partes[i]);
        }
        return sb.toString();
    }

    public static String firstNonEmpty(String... vals) {
        if (vals == null) {
            return null;
        }
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }
}

