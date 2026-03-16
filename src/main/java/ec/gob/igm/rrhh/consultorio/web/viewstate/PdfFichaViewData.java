package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;

/**
 * Class PdfFichaViewData: contiene la lógica de la aplicación.
 */
public class PdfFichaViewData {

    public final Object source;
    public final Logger log;
    public final FichaOcupacional ficha;
    public final DatEmpleado empleadoSel;
    public final PersonaAux personaAux;
    public final boolean permitirIngresoManual;
    public final Runnable asegurarPersonaAuxPersistida;
    public final CentroMedicoPdfFacade centroMedicoPdfFacade;
    public final PdfResourceResolver pdfResourceResolver;
    public final Runnable syncCamposDesdeObjetos;
    public final Supplier<String> obtenerTipoEvaluacionPdf;
    public final Runnable recalcularIMC;
    public final Consumer<Map<String, String>> cargarAtencionPrioritaria;
    public final Consumer<Map<String, String>> cargarActividadLaboralArrays;
    public final Supplier<String> fallbackObservacionSupplier;
    public final BiFunction<java.util.List<?>, Integer, Object> getSafe;
    public final Function<Object, Date> toDate;

    public PdfFichaViewData(Object source, Logger log, FichaOcupacional ficha, DatEmpleado empleadoSel,
                            PersonaAux personaAux, boolean permitirIngresoManual, Runnable asegurarPersonaAuxPersistida,
                            CentroMedicoPdfFacade centroMedicoPdfFacade, PdfResourceResolver pdfResourceResolver,
                            Runnable syncCamposDesdeObjetos, Supplier<String> obtenerTipoEvaluacionPdf,
                            Runnable recalcularIMC, Consumer<Map<String, String>> cargarAtencionPrioritaria,
                            Consumer<Map<String, String>> cargarActividadLaboralArrays,
                            Supplier<String> fallbackObservacionSupplier,
                            BiFunction<java.util.List<?>, Integer, Object> getSafe, Function<Object, Date> toDate) {
        this.source = source;
        this.log = log;
        this.ficha = ficha;
        this.empleadoSel = empleadoSel;
        this.personaAux = personaAux;
        this.permitirIngresoManual = permitirIngresoManual;
        this.asegurarPersonaAuxPersistida = asegurarPersonaAuxPersistida;
        this.centroMedicoPdfFacade = centroMedicoPdfFacade;
        this.pdfResourceResolver = pdfResourceResolver;
        this.syncCamposDesdeObjetos = syncCamposDesdeObjetos;
        this.obtenerTipoEvaluacionPdf = obtenerTipoEvaluacionPdf;
        this.recalcularIMC = recalcularIMC;
        this.cargarAtencionPrioritaria = cargarAtencionPrioritaria;
        this.cargarActividadLaboralArrays = cargarActividadLaboralArrays;
        this.fallbackObservacionSupplier = fallbackObservacionSupplier;
        this.getSafe = getSafe;
        this.toDate = toDate;
    }
}
