package ec.gob.igm.rrhh.consultorio.web.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.Stateless;

/**
 * Normalizaciones de examen físico para impresión/consistencia.
 *
 * Regla usada:
 * - Si en un tópico NO hay ningún "S" en sus ítems "anteriores",
 *   entonces el "último" ítem se marca como "S" (Normal/Sin hallazgos).
 *
 * Esto aplica a los tópicos donde el último check representa "Normal".
 */
@Stateless
public class FichaNormalizacionService {

    public void normalizarExamenFisicoUltimos(FichaOcupacional f) {
        if (f == null) return;

        // Nariz: Tabique, Cornetes, Mucosas => último Senos
        f.setExfNarizSenosParanasa(
                normalizarUltimo(f.getExfNarizSenosParanasa(),
                        f.getExfNarizTabique(),
                        f.getExfNarizCornetes(),
                        f.getExfNarizMucosas()
                )
        );

        // Tórax: Mamas, Pulmones, Corazón => último Parrilla
        f.setExfToraxParrillaCostal(
                normalizarUltimo(f.getExfToraxParrillaCostal(),
                        f.getExfToraxMamas(),
                        f.getExfToraxPulmones(),
                        f.getExfToraxCorazon()
                )
        );

        // Abdomen: Vísceras => último Pared
        f.setExfAbdParedAbdominal(
                normalizarUltimo(f.getExfAbdParedAbdominal(),
                        f.getExfAbdVisceras()
                )
        );

        // Columna: Flexibilidad, Desviación => último Dolor (según tu formulario)
        f.setExfColDolor(
                normalizarUltimo(f.getExfColDolor(),
                        f.getExfColFlexibilidad(),
                        f.getExfColDesviacion()
                )
        );

        // Extremidades: Vascular => último Miembros Inf (si tu último representa normal, ajusta)
        // Aquí NO forzamos porque usualmente no es "último = normal" en todos los formularios.
        // Si en tu formulario el último SÍ es "normal", me dices cuál y lo agrego.
    }

    private String normalizarUltimo(String ultimo, String... anteriores) {
        // Si hay algún S en los anteriores: no forzar
        if (anteriores != null) {
            for (String a : anteriores) {
                if (SnUtils.isS(a)) {
                    return SnUtils.snNoNull(ultimo);
                }
            }
        }
        // No hubo S en anteriores => normal = S
        return "S";
    }
}
