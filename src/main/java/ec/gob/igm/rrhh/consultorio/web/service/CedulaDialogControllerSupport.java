package ec.gob.igm.rrhh.consultorio.web.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.primefaces.PrimeFaces;

@ApplicationScoped
public class CedulaDialogControllerSupport {

    private static final String OPEN_CEDULA_DIALOG_PARAM = "openCedulaDlg";

    public boolean resetDialogVisibility() {
        return false;
    }

    public CedulaDialogStateService.AutoOpenState autoOpenIfNeeded(CedulaDialogStateService cedulaDialogStateService,
            String activeStep,
            boolean mostrarDlgCedula,
            boolean cedulaDlgAutoOpened) {
        CedulaDialogStateService.AutoOpenState state = cedulaDialogStateService.autoOpenIfNeeded(
                activeStep,
                mostrarDlgCedula,
                cedulaDlgAutoOpened);
        addOpenDialogCallback(state.isOpen());
        return state;
    }

    public CedulaDialogStateService.AutoOpenState consumeAutoOpen(CedulaDialogStateService cedulaDialogStateService,
            String activeStep,
            boolean empleadoNotSelected,
            boolean cedulaDlgAutoOpened) {
        CedulaDialogStateService.AutoOpenState state = cedulaDialogStateService.consumeAutoOpen(
                activeStep,
                empleadoNotSelected,
                cedulaDlgAutoOpened);
        addOpenDialogCallback(state.isOpen());
        return state;
    }

    private void addOpenDialogCallback(boolean open) {
        PrimeFaces.current().ajax().addCallbackParam(OPEN_CEDULA_DIALOG_PARAM, open);
    }
}
