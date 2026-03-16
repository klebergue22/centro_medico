package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;

@Stateless
/**
 * Class FichaPdfPreparationService: orquesta la lógica de presentación y flujo web.
 */
public class FichaPdfPreparationService implements Serializable {

    @EJB
    private FichaPdfValidationService fichaPdfValidationService;
    @EJB
    private FichaOcupacionalService fichaService;

    public FichaPdfPrepareResult preparar(
            FichaOcupacional ficha,
            DatEmpleado empleadoSel,
            PersonaAux personaAux,
            boolean permitirIngresoManual,
            Runnable asegurarPersonaAuxPersistida,
            Supplier<String> htmlBuilder,
            CentroMedicoPdfFacade facade) {

        List<String> errores = fichaPdfValidationService.validar(ficha, empleadoSel, personaAux, permitirIngresoManual);
        if (!errores.isEmpty()) {
            return FichaPdfPrepareResult.invalid(errores);
        }

        asegurarPersonaAuxPersistida.run();

        FichaOcupacional actualizada = fichaService.actualizar(ficha);
        if (actualizada != null && actualizada.getIdFicha() != null) {
            FichaOcupacional fresh = fichaService.reloadById(actualizada.getIdFicha());
            if (fresh != null) {
                actualizada = fresh;
            }
        }

        CentroMedicoPdfFacade.PdfPreviewResult result = facade.prepararPreviewDesdeHtml(
                null,
                htmlBuilder,
                "FICHA_");

        return FichaPdfPrepareResult.ready(actualizada, result.getToken(), result.isListo());
    }

    public static class FichaPdfPrepareResult implements Serializable {

        public final boolean valid;
        public final List<String> errores;
        public final FichaOcupacional ficha;
        public final String token;
        public final boolean listo;

        private FichaPdfPrepareResult(boolean valid, List<String> errores, FichaOcupacional ficha, String token, boolean listo) {
            this.valid = valid;
            this.errores = errores;
            this.ficha = ficha;
            this.token = token;
            this.listo = listo;
        }

        static FichaPdfPrepareResult invalid(List<String> errores) {
            return new FichaPdfPrepareResult(false, errores, null, null, false);
        }

        static FichaPdfPrepareResult ready(FichaOcupacional ficha, String token, boolean listo) {
            return new FichaPdfPrepareResult(true, List.of(), ficha, token, listo);
        }
    }
}
