package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfPlaceholderAssembler.FichaState;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfPlaceholderBuilder;

@Stateless
/**
 * Class FichaPdfTemplateService: gestiona la construcción y renderización de documentos PDF.
 */
public class FichaPdfTemplateService {

    public String construirHtmlFichaDesdePlantilla(
            FichaPdfPlaceholderBuilder builder,
            Supplier<String> templateLoader,
            Runnable syncData,
            Supplier<Map<String, String>> reemplazosSupplier,
            Supplier<String> tipoEvaluacionSupplier,
            CentroMedicoPdfFacade facade) {
        try {
            return builder.construirHtmlFichaDesdePlantilla(
                    templateLoader,
                    syncData,
                    reemplazosSupplier,
                    tipoEvaluacionSupplier,
                    facade);
        } catch (Exception e) {
            throw new RuntimeException("Error construyendo HTML de ficha desde plantilla", e);
        }
    }

    public Map<String, String> buildReemplazosFicha(
            Runnable recalcularImc,
            Runnable cargarAtencionPrioritaria,
            Runnable cargarActividadLaboral,
            Supplier<Map<String, String>> snapshotSupplier,
            FichaPdfPlaceholderAssembler assembler,
            Supplier<FichaState> fichaStateSupplier) {

        runIfPresent(recalcularImc);
        runIfPresent(cargarAtencionPrioritaria);
        runIfPresent(cargarActividadLaboral);

        Map<String, String> rep = new LinkedHashMap<>();
        if (snapshotSupplier != null) {
            Map<String, String> snapshot = snapshotSupplier.get();
            if (snapshot != null) {
                rep.putAll(snapshot);
            }
        }

        if (assembler != null && fichaStateSupplier != null) {
            FichaState state = fichaStateSupplier.get();
            if (state != null) {
                rep.putAll(assembler.buildReemplazosFicha(state));
            }
        }
        return rep;
    }

    private void runIfPresent(Runnable action) {
        if (action != null) {
            action.run();
        }
    }
}
