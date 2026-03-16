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

        recalcularImc.run();
        cargarAtencionPrioritaria.run();
        cargarActividadLaboral.run();

        Map<String, String> rep = new LinkedHashMap<>(snapshotSupplier.get());
        rep.putAll(assembler.buildReemplazosFicha(fichaStateSupplier.get()));
        return rep;
    }
}
