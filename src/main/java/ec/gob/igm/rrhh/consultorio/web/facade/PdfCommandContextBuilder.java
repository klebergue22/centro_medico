package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.ctrl.ControllerActionTemplate;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;

@ApplicationScoped
public class PdfCommandContextBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    public PdfPreviewCommandFactory.PdfCommandContext build(
            ControllerActionTemplate controllerActionTemplate,
            PdfPreviewState pdfPreviewState,
            PdfSessionStore pdfSessionStore,
            CentroMedicoCtrl ctrl,
            Logger log,
            String activeStep,
            Integer noPersonaSel,
            String cedulaBusqueda,
            Runnable asegurarPersonaAuxPersistida,
            Runnable syncCamposDesdeObjetos,
            Runnable recalcularImc,
            Supplier<Boolean> verificarFichaCompleta,
            Consumer<java.util.Date> setFechaEmision,
            int hRows,
            Consumer<FichaOcupacional> setFicha,
            Consumer<String> setActiveStep,
            Consumer<Boolean> setMostrarDlgCedula) {
        return new PdfPreviewCommandFactory.PdfCommandContext(
                controllerActionTemplate,
                pdfPreviewState,
                pdfSessionStore,
                ctrl,
                log,
                activeStep,
                noPersonaSel,
                cedulaBusqueda,
                asegurarPersonaAuxPersistida,
                syncCamposDesdeObjetos,
                recalcularImc,
                verificarFichaCompleta,
                setFechaEmision,
                hRows,
                setFicha,
                setActiveStep,
                setMostrarDlgCedula);
    }
}
