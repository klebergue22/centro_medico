package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;

@ApplicationScoped
/**
 * Class CentroMedicoFormInitializer: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoFormInitializer implements Serializable {

    private static final long serialVersionUID = 1L;

    public void initUiDefaults(CentroMedicoCtrl ctrl) {
        ctrl.setMostrarDlgCedula(true);
        ctrl.setFechaAtencion(new Date());

        initActLab(ctrl, 3);
        initActividadesExtra(ctrl, 3);

        ctrl.setTipoEval("INGRESO");
        ctrl.setSexo("M");
        ctrl.setGrupoSanguineo("");
        ctrl.setLateralidad("");
        ctrl.setExamenReproMasculino("");

        String institucion = "Instituto Geográfico Militar".toUpperCase();
        ctrl.setInstitucion(institucion);
        ctrl.setRuc("1768007200001");
    }

    public void initDomainDefaults(CentroMedicoCtrl ctrl) {
        FichaOcupacional ficha = new FichaOcupacional();
        ficha.setNoHistoriaClinica(null);
        ficha.setInstSistema(ctrl.getInstitucion());

        SignosVitales signos = new SignosVitales();
        ConsultaMedica consulta = new ConsultaMedica();

        FichaRiesgo fichaRiesgo = new FichaRiesgo();
        fichaRiesgo.setFicha(ficha);
        fichaRiesgo.setEstado("BORRADOR");

        ctrl.setFicha(ficha);
        ctrl.setSignos(signos);
        ctrl.setConsulta(consulta);
        ctrl.setFichaRiesgo(fichaRiesgo);
        ctrl.setPersonaAux(new PersonaAux());

        if (ctrl.getMedidasPreventivas() == null) {
            ctrl.setMedidasPreventivas(new ArrayList<>());
        }

        if (ctrl.getEmpleadoSel() != null) {
            ficha.setEmpleado(ctrl.getEmpleadoSel());
            consulta.setEmpleado(ctrl.getEmpleadoSel());
        }

        if (ficha.getRucEstablecimiento() == null || ficha.getRucEstablecimiento().isBlank()) {
            ficha.setRucEstablecimiento(ctrl.getRuc());
        }

        ficha.setFechaEvaluacion(ctrl.getFechaAtencion());
        ficha.setTipoEvaluacion(ctrl.getTipoEval());

        ctrl.setListaDiag(initDefaultDiagnosisRows(6));
    }

    public void initStep2Defaults(CentroMedicoCtrl ctrl, List<String> staticRiskCols) {
        if (ctrl.getFicha() == null) {
            ctrl.setFicha(new FichaOcupacional());
        }

        if (ctrl.getFichaRiesgo() == null) {
            FichaRiesgo fichaRiesgo = new FichaRiesgo();
            fichaRiesgo.setFicha(ctrl.getFicha());
            fichaRiesgo.setEstado("BORRADOR");
            ctrl.setFichaRiesgo(fichaRiesgo);
        }

        if (ctrl.getActividadesLab() == null) {
            ctrl.setActividadesLab(new ArrayList<>());
        }
        while (ctrl.getActividadesLab().size() < 7) {
            ctrl.getActividadesLab().add(null);
        }

        if (ctrl.getMedidasPreventivas() == null) {
            ctrl.setMedidasPreventivas(new ArrayList<>());
        }
        while (ctrl.getMedidasPreventivas().size() < 7) {
            ctrl.getMedidasPreventivas().add(null);
        }

        if (ctrl.getRiesgos() == null) {
            ctrl.setRiesgos(new LinkedHashMap<>());
        }
        if (ctrl.getOtrosRiesgos() == null) {
            ctrl.setOtrosRiesgos(new LinkedHashMap<>());
        }

        ctrl.setRiskCols(staticRiskCols);
    }

    public void initStep3Defaults(CentroMedicoCtrl ctrl, int hRows, int diagRows) {
        ctrl.setGinecoExamen1("");
        ctrl.setGinecoTiempo1("");
        ctrl.setGinecoResultado1("");
        ctrl.setGinecoExamen2("");
        ctrl.setGinecoTiempo2("");
        ctrl.setGinecoResultado2("");
        ctrl.setGinecoObservacion("");
        ctrl.setEnfermedadActual("");

        ctrl.setExfPielCicatrices("");
        ctrl.setExfOjosParpados("");
        ctrl.setExfOjosConjuntivas("");
        ctrl.setExfOjosPupilas("");
        ctrl.setExfOjosCornea("");
        ctrl.setExfOjosMotilidad("");
        ctrl.setExfOidoConducto("");
        ctrl.setExfOidoPabellon("");
        ctrl.setExfOidoTimpanos("");
        ctrl.setExfOroLabios("");
        ctrl.setExfOroLengua("");
        ctrl.setExfOroFaringe("");
        ctrl.setExfOroAmigdalas("");
        ctrl.setExfOroDentadura("");
        ctrl.setExfNarizTabique("");
        ctrl.setExfNarizCornetes("");
        ctrl.setExfNarizMucosas("");
        ctrl.setExfNarizSenos("");
        ctrl.setExfCuelloTiroides("");
        ctrl.setExfCuelloMovilidad("");
        ctrl.setExfToraxMamas("");
        ctrl.setExfToraxPulmones("");
        ctrl.setExfToraxCorazon("");
        ctrl.setExfToraxParrilla("");
        ctrl.setExfAbdomenVisceras("");
        ctrl.setExfAbdomenPared("");
        ctrl.setExfColumnaFlexibilidad("");
        ctrl.setExfColumnaDesviacion("");
        ctrl.setExfColumnaDolor("");
        ctrl.setExfPelvisPelvis("");
        ctrl.setExfPelvisGenitales("");
        ctrl.setExfExtVascular("");
        ctrl.setExfExtSup("");
        ctrl.setExfExtInf("");
        ctrl.setExfNeuroFuerza("");
        ctrl.setExfNeuroSensibilidad("");
        ctrl.setExfNeuroMarcha("");
        ctrl.setExfNeuroReflejos("");
        ctrl.setObsExamenFisico("");

        initExamenes(ctrl, 5);

        if (ctrl.getObsJ() == null) {
            ctrl.setObsJ("");
        }

        ctrl.sethCentroTrabajo(new String[hRows]);
        ctrl.sethActividad(new String[hRows]);
        ctrl.sethIncidente(new Boolean[hRows]);
        ctrl.sethAccidente(new Boolean[hRows]);
        ctrl.sethTiempo(new Integer[hRows]);
        ctrl.sethEnfOcupacional(new Boolean[hRows]);
        ctrl.sethEnfComun(new Boolean[hRows]);
        ctrl.sethEnfProfesional(new Boolean[hRows]);
        ctrl.sethOtros(new Boolean[hRows]);
        ctrl.sethOtrosCual(new String[hRows]);
        ctrl.sethFecha(new Date[hRows]);
        ctrl.sethEspecificacion(new String[hRows]);
        ctrl.sethObservacion(new String[hRows]);

        ctrl.setConsOtrasCual(null);
        ctrl.setConsumoVidaCondObs(null);
        initConsumoVidaCond(ctrl, 3);

        initActividadesExtra(ctrl, 3);
        initActLab(ctrl, hRows);
        ensureDiagSize(ctrl, diagRows);

        if (ctrl.getPersonaAux() == null) {
            ctrl.setPersonaAux(new PersonaAux());
        }
        ctrl.setPermitirIngresoManual(false);
    }

    private void initConsumoVidaCond(CentroMedicoCtrl ctrl, int size) {
        initializeConsumoArrays(ctrl, size);
        initializeHabitosArrays(ctrl, size);
        initializeMedicacionArrays(ctrl, size);
        initializeConsumoDefaults(ctrl, size);
        ctrl.setConsumoVidaCondObs(ctrl.getConsumoVidaCondObs() == null ? "" : ctrl.getConsumoVidaCondObs());
    }

    private void initializeConsumoArrays(CentroMedicoCtrl ctrl, int size) {
        ctrl.setConsTiempoConsumoMeses(
                ctrl.getConsTiempoConsumoMeses() == null ? new Integer[size] : ctrl.getConsTiempoConsumoMeses());
        ctrl.setConsExConsumidor(
                ctrl.getConsExConsumidor() == null ? new Boolean[size] : ctrl.getConsExConsumidor());
        ctrl.setConsTiempoAbstinenciaMeses(ctrl.getConsTiempoAbstinenciaMeses() == null
                ? new Integer[size]
                : ctrl.getConsTiempoAbstinenciaMeses());
        ctrl.setConsNoConsume(ctrl.getConsNoConsume() == null ? new Boolean[size] : ctrl.getConsNoConsume());
    }

    private void initializeHabitosArrays(CentroMedicoCtrl ctrl, int size) {
        ctrl.setAfCual(ctrl.getAfCual() == null ? new String[size] : ctrl.getAfCual());
        ctrl.setAfTiempo(ctrl.getAfTiempo() == null ? new String[size] : ctrl.getAfTiempo());
    }

    private void initializeMedicacionArrays(CentroMedicoCtrl ctrl, int size) {
        ctrl.setMedCual(ctrl.getMedCual() == null ? new String[size] : ctrl.getMedCual());
        ctrl.setMedCant(ctrl.getMedCant() == null ? new Integer[size] : ctrl.getMedCant());
    }

    private void initializeConsumoDefaults(CentroMedicoCtrl ctrl, int size) {
        for (int i = 0; i < size; i++) {
            ctrl.getConsExConsumidor()[i] = ctrl.getConsExConsumidor()[i] == null
                    ? Boolean.FALSE
                    : ctrl.getConsExConsumidor()[i];
            ctrl.getConsNoConsume()[i] = ctrl.getConsNoConsume()[i] == null
                    ? Boolean.FALSE
                    : ctrl.getConsNoConsume()[i];
        }
    }

    public void initExamenes(CentroMedicoCtrl ctrl, int n) {
        ctrl.setExamNombre(new ArrayList<>(Collections.nCopies(n, "")));
        ctrl.setExamFecha(new ArrayList<>(Collections.nCopies(n, null)));
        ctrl.setExamResultado(new ArrayList<>(Collections.nCopies(n, "")));
    }

    public void initActLab(CentroMedicoCtrl ctrl, int n) {
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(String.valueOf(i + 1));
        }
        ctrl.setActLabRows(rows);

        ctrl.setActLabCentroTrabajo(new ArrayList<>(Collections.nCopies(n, "")));
        ctrl.setActLabActividad(new ArrayList<>(Collections.nCopies(n, "")));
        ctrl.setActLabIncidente(new ArrayList<>(Collections.nCopies(n, "")));
        ctrl.setActLabFecha(new ArrayList<>(Collections.nCopies(n, null)));
        ctrl.setActLabTiempo(new ArrayList<>(Collections.nCopies(n, "")));

        ctrl.setActLabTrabajoAnterior(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));
        ctrl.setActLabTrabajoActual(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));
        ctrl.setActLabIncidenteChk(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));
        ctrl.setActLabAccidenteChk(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));
        ctrl.setActLabEnfermedadChk(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));

        ctrl.setActLabObservaciones(new ArrayList<>(Collections.nCopies(n, "")));

        ctrl.setIessSi(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));
        ctrl.setIessNo(new ArrayList<>(Collections.nCopies(n, Boolean.FALSE)));
        ctrl.setIessFecha(new ArrayList<>(Collections.nCopies(n, null)));
        ctrl.setIessEspecificar(new ArrayList<>(Collections.nCopies(n, "")));
    }

    public void initActividadesExtra(CentroMedicoCtrl ctrl, int n) {
        ctrl.setFechaAct(new ArrayList<>(Collections.nCopies(n, null)));
        ctrl.setTipoAct(new ArrayList<>(Collections.nCopies(n, "")));
        ctrl.setDescAct(new ArrayList<>(Collections.nCopies(n, "")));
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

    private List<ConsultaDiagnostico> initDefaultDiagnosisRows(int size) {
        List<ConsultaDiagnostico> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ConsultaDiagnostico cd = new ConsultaDiagnostico();
            cd.setTipoDiag(null);
            cd.setCie10(null);
            cd.setCodigo(null);
            cd.setDescripcion(null);
            result.add(cd);
        }
        return result;
    }
}
