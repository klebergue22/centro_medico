package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

/**
 * Estado de navegación del wizard de Centro Médico.
 */
public class WizardViewState implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activeStep = "step1";
    private int stepIndex = 1;
    private boolean preRenderDone;
    private boolean cedulaDlgAutoOpened;

    public String getActiveStep() {
        return activeStep;
    }

    public void setActiveStep(String activeStep) {
        this.activeStep = activeStep;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public boolean isPreRenderDone() {
        return preRenderDone;
    }

    public void setPreRenderDone(boolean preRenderDone) {
        this.preRenderDone = preRenderDone;
    }

    public boolean isCedulaDlgAutoOpened() {
        return cedulaDlgAutoOpened;
    }

    public void setCedulaDlgAutoOpened(boolean cedulaDlgAutoOpened) {
        this.cedulaDlgAutoOpened = cedulaDlgAutoOpened;
    }
}
