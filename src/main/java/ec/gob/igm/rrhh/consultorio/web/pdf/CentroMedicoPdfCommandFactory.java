package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfUiService;

@Stateless
/**
 * Class CentroMedicoPdfCommandFactory: gestiona la construcción y renderización de documentos PDF.
 */
public class CentroMedicoPdfCommandFactory implements Serializable {

    public CentroMedicoPdfUiService.PrepareFichaUiCommand buildPrepareFichaUiCommand(
            FichaOcupacional ficha,
            DatEmpleado empleadoSel,
            PersonaAux personaAux,
            boolean permitirIngresoManual,
            Runnable asegurarPersonaAuxPersistida,
            Supplier<String> htmlFichaSupplier,
            CentroMedicoPdfFacade centroMedicoPdfFacade) {
        return new CentroMedicoPdfUiService.PrepareFichaUiCommand(
                ficha,
                empleadoSel,
                personaAux,
                permitirIngresoManual,
                asegurarPersonaAuxPersistida,
                htmlFichaSupplier,
                centroMedicoPdfFacade);
    }

    public CentroMedicoPdfUiService.PrepareCertificadoUiCommand buildPrepareCertificadoUiCommand(
            FichaOcupacional ficha,
            Supplier<Boolean> verificarFichaCompleta,
            Supplier<String> htmlCertificadoSupplier,
            Consumer<Date> fechaEmisionSetter,
            CentroMedicoPdfFacade centroMedicoPdfFacade) {
        return new CentroMedicoPdfUiService.PrepareCertificadoUiCommand(
                ficha,
                verificarFichaCompleta,
                htmlCertificadoSupplier,
                fechaEmisionSetter,
                centroMedicoPdfFacade);
    }

    public String construirHtmlDesdePlantillaUnchecked(ThrowingSupplier<String> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo construir HTML del certificado", e);
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws IOException;
    }
}
