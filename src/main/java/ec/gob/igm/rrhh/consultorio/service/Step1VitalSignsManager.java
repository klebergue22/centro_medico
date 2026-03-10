package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Stateless
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
        final int[] pa = parseBloodPressureOrThrow(paStr);

        SignosVitales sv = null;
        if (current != null && current.getIdSignos() != null) {
            sv = signosService.buscarPorId(current.getIdSignos());
        }

        if (sv == null) {
            sv = new SignosVitales();
        }

        sv.setTemperaturaC(bd(temp, 1));
        sv.setPaSistolica(pa[0]);
        sv.setPaDiastolica(pa[1]);
        sv.setFrecuenciaCard(fc);
        sv.setFrecuenciaResp(fr);
        sv.setSatO2(satO2);
        sv.setPesoKg(bd(peso, 2));

        BigDecimal tallaM = (tallaCm == null)
                ? null
                : bd(tallaCm, 2).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        sv.setTallaM(tallaM);
        sv.setPerimetroAbdCm(bd(perimetroAbd, 1));

        stampAuditFields(sv, now, user);

        return signosService.guardar(sv);
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
            throw new IllegalArgumentException("El formato de PA debe ser 120/80 (números enteros separados por '/').");
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
