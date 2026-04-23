package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Stateless
/**
 * Class Step1VitalSignsManager: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class Step1VitalSignsManager {

    @EJB
    private SignosVitalesService signosService;

    public SignosVitales upsertVitalSigns(SignosVitales current,
                                          String paStr,
                                          Double temp,
                                          Integer fc,
                                          Integer fr,
                                          Integer satO2,
                                          Double peso,
                                          Double tallaCm,
                                          Double perimetroAbd,
                                          Date now,
                                          String user) {
        SignosVitales sv = loadExistingOrCreate(current);
        applyMeasurements(sv, parseBloodPressureOrThrow(paStr), temp, fc, fr, satO2, peso, tallaCm, perimetroAbd);
        stampAuditFields(sv, now, user);
        return signosService.guardar(sv, user);
    }

    private SignosVitales loadExistingOrCreate(SignosVitales current) {
        if (current != null && current.getIdSignos() != null) {
            SignosVitales persisted = signosService.buscarPorId(current.getIdSignos());
            if (persisted != null) {
                return persisted;
            }
        }
        return new SignosVitales();
    }

    private void applyMeasurements(SignosVitales sv, int[] pa, Double temp, Integer fc, Integer fr, Integer satO2,
            Double peso, Double tallaCm, Double perimetroAbd) {
        sv.setTemperaturaC(bd(temp, 1));
        sv.setPaSistolica(pa[0]);
        sv.setPaDiastolica(pa[1]);
        sv.setFrecuenciaCard(fc);
        sv.setFrecuenciaResp(fr);
        sv.setSatO2(satO2);
        sv.setPesoKg(bd(peso, 2));
        sv.setTallaM(toMeters(tallaCm));
        sv.setPerimetroAbdCm(bd(perimetroAbd, 1));
    }

    private BigDecimal toMeters(Double tallaCm) {
        if (tallaCm == null) {
            return null;
        }
        return bd(tallaCm, 2).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal bd(Double v, int scale) {
        if (v == null) {
            return null;
        }
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
    }

    private int[] parseBloodPressureOrThrow(String pa) {
        try {
            String[] parts = pa.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid PA");
            }
            Integer sis = Integer.valueOf(parts[0].trim());
            Integer dias = Integer.valueOf(parts[1].trim());
            return new int[]{sis, dias};
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("El formato de PA debe ser 120/80 (nÃºmeros enteros separados por '/').");
        }
    }

    private void stampAuditFields(SignosVitales sv, Date now, String user) {
        if (sv.getIdSignos() == null) {
            sv.setFechaCreacion(now);
            sv.setUsrCreacion(user);
        } else {
            sv.setFechaActualizacion(now);
            sv.setUsrActualizacion(user);
        }
    }
}
