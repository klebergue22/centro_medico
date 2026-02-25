package ec.gob.igm.rrhh.consultorio.web.audit;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs de auditoría técnica (DEBUG).
 * IMPORTANTE: no loggear datos sensibles (diagnósticos, observaciones clínicas detalladas, etc.)
 */
@Stateless
public class FichaAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(FichaAuditLogger.class);

    public void logExamenFisicoSn(FichaOcupacional f) {
        if (f == null) return;
        if (!log.isDebugEnabled()) return;

        log.debug("EXF[Nariz] tabique={}, cornetes={}, mucosas={}, senos={}",
                f.getExfNarizTabique(), f.getExfNarizCornetes(), f.getExfNarizMucosas(), f.getExfNarizSenosParanasa());

        log.debug("EXF[Cuello] tiroidesMasas={}, movilidad={}",
                f.getExfCuelloTiroidesMasas(), f.getExfCuelloMovilidad());

        log.debug("EXF[Tórax] mamas={}, pulmones={}, corazon={}, parrilla={}",
                f.getExfToraxMamas(), f.getExfToraxPulmones(), f.getExfToraxCorazon(), f.getExfToraxParrillaCostal());

        log.debug("EXF[Abdomen] visceras={}, pared={}",
                f.getExfAbdVisceras(), f.getExfAbdParedAbdominal());

        log.debug("EXF[Columna] flex={}, desv={}, dolor={}",
                f.getExfColFlexibilidad(), f.getExfColDesviacion(), f.getExfColDolor());

        log.debug("EXF[Extremidades] vascular={}, sup={}, inf={}",
                f.getExfExtVascular(), f.getExfExtMiembrosSup(), f.getExfExtMiembrosInf());
    }
}
