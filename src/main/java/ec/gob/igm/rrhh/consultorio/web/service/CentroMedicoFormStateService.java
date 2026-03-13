package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;

@ApplicationScoped
public class CentroMedicoFormStateService implements Serializable {

    private static final long serialVersionUID = 1L;

    public void prepareStep3Collections(CentroMedicoCtrl ctrl, int hRows, int diagRows, int examRows) {
        ensureExamenesSize(ctrl, examRows);
        ensureActLabSize(ctrl, hRows);
        ensureDiagSize(ctrl, diagRows);
    }

    public ConsultaDiagnostico ensureDiag(CentroMedicoCtrl ctrl, int index) {
        if (index < 0) {
            return null;
        }
        List<ConsultaDiagnostico> listaDiag = ctrl.getListaDiag();
        if (listaDiag == null) {
            listaDiag = new ArrayList<>();
            ctrl.setListaDiag(listaDiag);
        }

        while (listaDiag.size() <= index) {
            listaDiag.add(new ConsultaDiagnostico());
        }

        ConsultaDiagnostico diag = listaDiag.get(index);
        if (diag == null) {
            diag = new ConsultaDiagnostico();
            listaDiag.set(index, diag);
        }

        return diag;
    }

    public void ensureDiagSize(CentroMedicoCtrl ctrl, int size) {
        if (size <= 0) {
            return;
        }

        List<ConsultaDiagnostico> listaDiag = ctrl.getListaDiag();
        if (listaDiag == null) {
            listaDiag = new ArrayList<>();
            ctrl.setListaDiag(listaDiag);
        }

        while (listaDiag.size() < size) {
            ConsultaDiagnostico d = new ConsultaDiagnostico();
            d.setTipoDiag("P");
            listaDiag.add(d);
        }
    }

    public void ensureActLabSize(CentroMedicoCtrl ctrl, int size) {
        initActivityLabListsIfNull(ctrl);

        ensureListSize(ctrl.getActLabCentroTrabajo(), size, null);
        ensureListSize(ctrl.getActLabActividad(), size, null);
        ensureListSize(ctrl.getActLabTiempo(), size, null);
        ensureListSize(ctrl.getActLabObservaciones(), size, null);
        ensureListSize(ctrl.getIessEspecificar(), size, null);

        ensureListSize(ctrl.getActLabTrabajoAnterior(), size, null);
        ensureListSize(ctrl.getActLabTrabajoActual(), size, null);
        ensureListSize(ctrl.getActLabIncidenteChk(), size, null);
        ensureListSize(ctrl.getActLabAccidenteChk(), size, null);
        ensureListSize(ctrl.getActLabEnfermedadChk(), size, null);
        ensureListSize(ctrl.getIessSi(), size, null);
        ensureListSize(ctrl.getIessNo(), size, null);
        ensureListSize(ctrl.getIessFecha(), size, null);

        List<String> rows = ctrl.getActLabRows();
        rows.clear();
        for (int i = 1; i <= size; i++) {
            rows.add(String.valueOf(i));
        }
    }

    private void ensureExamenesSize(CentroMedicoCtrl ctrl, int size) {
        if (ctrl.getExamNombre() == null) {
            ctrl.setExamNombre(new ArrayList<>(Collections.nCopies(size, "")));
        }
        if (ctrl.getExamFecha() == null) {
            ctrl.setExamFecha(new ArrayList<>(Collections.nCopies(size, null)));
        }
        if (ctrl.getExamResultado() == null) {
            ctrl.setExamResultado(new ArrayList<>(Collections.nCopies(size, "")));
        }

        ensureListSize(ctrl.getExamNombre(), size, "");
        ensureListSize(ctrl.getExamFecha(), size, null);
        ensureListSize(ctrl.getExamResultado(), size, "");
    }

    private void initActivityLabListsIfNull(CentroMedicoCtrl ctrl) {
        if (ctrl.getActLabRows() == null) {
            ctrl.setActLabRows(new ArrayList<>());
        }
        if (ctrl.getActLabCentroTrabajo() == null) {
            ctrl.setActLabCentroTrabajo(new ArrayList<>());
        }
        if (ctrl.getActLabActividad() == null) {
            ctrl.setActLabActividad(new ArrayList<>());
        }
        if (ctrl.getActLabTiempo() == null) {
            ctrl.setActLabTiempo(new ArrayList<>());
        }
        if (ctrl.getActLabObservaciones() == null) {
            ctrl.setActLabObservaciones(new ArrayList<>());
        }

        if (ctrl.getActLabTrabajoAnterior() == null) {
            ctrl.setActLabTrabajoAnterior(new ArrayList<>());
        }
        if (ctrl.getActLabTrabajoActual() == null) {
            ctrl.setActLabTrabajoActual(new ArrayList<>());
        }
        if (ctrl.getActLabIncidenteChk() == null) {
            ctrl.setActLabIncidenteChk(new ArrayList<>());
        }
        if (ctrl.getActLabAccidenteChk() == null) {
            ctrl.setActLabAccidenteChk(new ArrayList<>());
        }
        if (ctrl.getActLabEnfermedadChk() == null) {
            ctrl.setActLabEnfermedadChk(new ArrayList<>());
        }

        if (ctrl.getIessSi() == null) {
            ctrl.setIessSi(new ArrayList<>());
        }
        if (ctrl.getIessNo() == null) {
            ctrl.setIessNo(new ArrayList<>());
        }
        if (ctrl.getIessFecha() == null) {
            ctrl.setIessFecha(new ArrayList<>());
        }
        if (ctrl.getIessEspecificar() == null) {
            ctrl.setIessEspecificar(new ArrayList<>());
        }
    }

    private <T> void ensureListSize(List<T> list, int size, T defaultValue) {
        while (list.size() < size) {
            list.add(defaultValue);
        }
        if (list.size() > size) {
            list.subList(size, list.size()).clear();
        }
    }
}
