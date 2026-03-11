package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CentroMedicoWizardService implements Serializable {

    private static final long serialVersionUID = 1L;

    public String guardarStepActual(
            String activeStep,
            Runnable guardarStep1,
            Runnable guardarStep2,
            Runnable guardarStep3,
            Runnable onEnterStep4) {

        String next = saveCurrentStepAndGetNext(activeStep, guardarStep1, guardarStep2, guardarStep3);
        if ("step4".equals(next) && onEnterStep4 != null) {
            onEnterStep4.run();
        }
        return next;
    }

    public String saveCurrentStepAndGetNext(
            String activeStep,
            Runnable guardarStep1,
            Runnable guardarStep2,
            Runnable guardarStep3) {

        if ("step1".equals(activeStep)) {
            guardarStep1.run();
            return "step2";
        }
        if ("step2".equals(activeStep)) {
            guardarStep2.run();
            return "step3";
        }
        if ("step3".equals(activeStep)) {
            guardarStep3.run();
            return "step4";
        }
        return null;
    }

    public String retrocederStep(String activeStep) {
        if ("step2".equals(activeStep)) {
            return "step1";
        }
        if ("step3".equals(activeStep)) {
            return "step2";
        }
        if ("step4".equals(activeStep)) {
            return "step3";
        }
        return activeStep;
    }
}
