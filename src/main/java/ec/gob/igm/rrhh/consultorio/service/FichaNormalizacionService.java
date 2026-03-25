package ec.gob.igm.rrhh.consultorio.web.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.Stateless;

/**
 * Normalizaciones de examen fisico para impresion/consistencia.
 */
@Stateless
public class FichaNormalizacionService {

    public void normalizarExamenFisicoUltimos(FichaOcupacional f) {
        if (f == null) {
            return;
        }
        normalizarNariz(f);
        normalizarTorax(f);
        normalizarAbdomen(f);
        normalizarColumna(f);
    }

    private String normalizarUltimo(String ultimo, String... anteriores) {
        if (anteriores != null) {
            for (String a : anteriores) {
                if (SnUtils.isS(a)) {
                    return SnUtils.snNoNull(ultimo);
                }
            }
        }
        return "S";
    }

    private void normalizarNariz(FichaOcupacional f) {
        f.setExfNarizSenosParanasa(normalizarUltimo(
                f.getExfNarizSenosParanasa(),
                f.getExfNarizTabique(),
                f.getExfNarizCornetes(),
                f.getExfNarizMucosas()
        ));
    }

    private void normalizarTorax(FichaOcupacional f) {
        f.setExfToraxParrillaCostal(normalizarUltimo(
                f.getExfToraxParrillaCostal(),
                f.getExfToraxMamas(),
                f.getExfToraxPulmones(),
                f.getExfToraxCorazon()
        ));
    }

    private void normalizarAbdomen(FichaOcupacional f) {
        f.setExfAbdParedAbdominal(normalizarUltimo(
                f.getExfAbdParedAbdominal(),
                f.getExfAbdVisceras()
        ));
    }

    private void normalizarColumna(FichaOcupacional f) {
        f.setExfColDolor(normalizarUltimo(
                f.getExfColDolor(),
                f.getExfColFlexibilidad(),
                f.getExfColDesviacion()
        ));
    }
}
