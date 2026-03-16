package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.facade.PacienteRegistrationFacade;
import ec.gob.igm.rrhh.consultorio.web.service.PacienteControllerSupport;
import jakarta.ejb.Stateless;
import java.util.function.Consumer;
import org.slf4j.Logger;

@Stateless
public class PacienteSearchInputAssembler {

    public PacienteControllerSupport.BuscarCedulaInput buildBuscarCedulaInput(CentroMedicoCtrl source,
            Logger logger,
            Consumer<PacienteRegistrationFacade.UiResult> applyUiResult) {
        return PacienteControllerSupport.BuscarCedulaInput.builder()
                .cedulaBusqueda(source.getCedulaBusqueda())
                .ficha(source.getFicha())
                .personaAux(source.getPersonaAux())
                .permitirIngresoManual(source.isPermitirIngresoManual())
                .activeStep(source.getActiveStep())
                .noPersonaSel(source.getNoPersonaSel())
                .logger(logger)
                .applyUiResult(applyUiResult)
                .build();
    }
}
