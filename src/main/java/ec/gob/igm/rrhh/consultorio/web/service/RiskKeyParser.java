package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import jakarta.ejb.Stateless;

@Stateless
/**
 * Class RiskKeyParser: orquesta la lógica de presentación y flujo web.
 */
public class RiskKeyParser implements Serializable {

    public RiskKey parseRiskKey(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();
        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }
        Integer act = parseActividad(k.substring(last + 1));
        if (act == null || k.length() < 3) {
            return null;
        }

        String prefRaw = k.substring(0, 3);
        String grupo = grupoFromPrefix(prefRaw);
        String item = k.substring(0, last).replace('_', ' ');
        String prefWithSpace = prefRaw + " ";
        if (item.startsWith(prefWithSpace)) {
            item = item.substring(prefWithSpace.length());
        }
        return new RiskKey(grupo, item, act);
    }

    public RiskKey parseRiskKeyOtros(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();
        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }
        Integer act = parseActividad(k.substring(last + 1));
        if (act == null || k.length() < 3) {
            return null;
        }

        String prefRaw = k.substring(0, 3);
        String grupo = grupoFromPrefix(prefRaw);
        return new RiskKey(grupo, "OTROS", act);
    }

    public String prefixFromGrupoLower(String grupo) {
        if (grupo == null) {
            return "otr";
        }
        switch (grupo.toUpperCase()) {
            case "FISICO":
                return "fis";
            case "SEGURIDAD":
                return "seg";
            case "QUIMICO":
                return "qui";
            case "BIOLOGICO":
                return "bio";
            case "ERGONOMICO":
                return "erg";
            case "PSICOSOCIAL":
                return "psi";
            default:
                return "otr";
        }
    }

    private Integer parseActividad(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String grupoFromPrefix(String pref) {
        switch (pref) {
            case "FIS":
                return "FISICO";
            case "SEG":
                return "SEGURIDAD";
            case "QUI":
                return "QUIMICO";
            case "BIO":
                return "BIOLOGICO";
            case "ERG":
                return "ERGONOMICO";
            case "PSI":
                return "PSICOSOCIAL";
            default:
                return "OTROS";
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public record RiskKey(String grupo, String item, Integer actividad) {
    }
}
