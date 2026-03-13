package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CedulaDialogStateService implements Serializable {

    private static final long serialVersionUID = 1L;

    public AutoOpenState autoOpenIfNeeded(String activeStep, boolean mostrarDlgCedula, boolean autoOpened) {
        boolean shouldOpen = !autoOpened && "step1".equals(activeStep) && mostrarDlgCedula;
        return new AutoOpenState(shouldOpen, autoOpened || shouldOpen);
    }

    public AutoOpenState consumeAutoOpen(String activeStep, boolean empleadoMissing, boolean autoOpened) {
        boolean shouldOpen = !autoOpened && "step1".equals(activeStep) && empleadoMissing;
        return new AutoOpenState(shouldOpen, autoOpened || shouldOpen);
    }

    public static final class AutoOpenState {
        private final boolean open;
        private final boolean autoOpened;

        public AutoOpenState(boolean open, boolean autoOpened) {
            this.open = open;
            this.autoOpened = autoOpened;
        }

        public boolean isOpen() { return open; }
        public boolean isAutoOpened() { return autoOpened; }
    }
}
