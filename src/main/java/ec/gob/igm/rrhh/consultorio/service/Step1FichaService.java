package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.Date;

@Stateless
public class Step1FichaService {

    @EJB
    private EmpleadoService empleadoService;
    @EJB
    private PersonaAuxService personaAuxService;
    @EJB
    private FichaOcupacionalService fichaService;
    @EJB
    private Step1VitalSignsManager step1VitalSignsManager;
    @EJB
    private ExamenFisicoRegionalService examenFisicoRegionalService;
    @EJB
    private CentroMedicoAuditService centroMedicoAuditService;

    public Step1Result guardar(Step1Command cmd) {
        final Date now = new Date();
        final String user = cmd.usuario();

        FichaOcupacional ficha = ensureFichaInitialized(cmd.ficha());
        DatEmpleado empleado = resolveSelectedEmployeeIfNeeded(cmd.empleadoSel(), cmd.noPersonaSel());
        PersonaAux personaAux = cmd.personaAux();

        validateStep1InputsOrThrow(cmd, empleado, personaAux);

        PatientAssignment assignment = assignPatientAndHistory(ficha, empleado, personaAux, now, user);
        personaAux = assignment.personaAux();

        mapStep1ToOccupationalRecord(ficha, cmd);
        examenFisicoRegionalService.aplicarExamenFisicoRegional(ficha);

        SignosVitales sv = upsertVitalSigns(ficha, cmd, now, user);
        ficha = saveDraftOccupationalRecord(ficha, personaAux, now, user);

        auditStep1(ficha, sv);

        return new Step1Result(ficha, empleado, personaAux, sv);
    }

    private FichaOcupacional ensureFichaInitialized(FichaOcupacional ficha) {
        return (ficha != null) ? ficha : new FichaOcupacional();
    }

    private DatEmpleado resolveSelectedEmployeeIfNeeded(DatEmpleado empleadoSel, Integer noPersonaSel) {
        if (empleadoSel == null && noPersonaSel != null) {
            return empleadoService.buscarPorId(noPersonaSel);
        }
        return empleadoSel;
    }

    private void validateStep1InputsOrThrow(Step1Command cmd, DatEmpleado empleadoSel, PersonaAux personaAux) {
        if (cmd.fechaAtencion() == null) {
            fail("Debe ingresar la fecha de atención.");
        }
        if (esVacio(cmd.tipoEval())) {
            fail("Debe seleccionar el tipo de evaluación.");
        }
        validatePatientOrThrow(empleadoSel, personaAux);
        validateVitalSignsInputsOrThrow(cmd);
    }

    private void validatePatientOrThrow(DatEmpleado empleadoSel, PersonaAux personaAux) {
        if (empleadoSel != null) {
            return;
        }
        if (personaAux == null || esVacio(personaAux.getCedula())) {
            fail("Debe seleccionar un empleado de RRHH o registrar una persona auxiliar (cédula obligatoria).");
        }
        if (esVacio(personaAux.getApellido1()) || esVacio(personaAux.getNombre1()) || esVacio(personaAux.getSexo())) {
            fail("En Persona Auxiliar: primer apellido, primer nombre y sexo son obligatorios.");
        }
    }

    private void validateVitalSignsInputsOrThrow(Step1Command cmd) {
        if (esVacio(cmd.paStr())) {
            fail("Debe ingresar la presión arterial (PA) en formato 120/80.");
        }
        if (cmd.fc() == null) {
            fail("Debe ingresar la frecuencia cardíaca (FC).");
        }
        if (cmd.peso() == null || cmd.peso() <= 0) {
            fail("Debe ingresar el peso (kg).");
        }
        if (cmd.tallaCm() == null) {
            fail("Debe ingresar la talla (cm).");
        }
    }

    private PatientAssignment assignPatientAndHistory(FichaOcupacional ficha,
                                                      DatEmpleado empleadoSel,
                                                      PersonaAux personaAux,
                                                      Date now,
                                                      String user) {
        String cedulaPaciente;

        if (empleadoSel != null) {
            ficha.setEmpleado(empleadoSel);
            ficha.setPersonaAux(null);
            cedulaPaciente = empleadoSel.getNoCedula();
        } else {
            if (personaAux.getIdPersonaAux() == null) {
                personaAux.setFechaCreacion(now);
                personaAux.setUsrCreacion(user);
                personaAux = personaAuxService.guardar(personaAux);
            }
            ficha.setPersonaAux(personaAux);
            ficha.setEmpleado(null);
            cedulaPaciente = personaAux.getCedula();
        }

        ficha.setNoHistoriaClinica(cedulaPaciente);
        ficha.setNoArchivo(cedulaPaciente);
        return new PatientAssignment(personaAux);
    }

    private void mapStep1ToOccupationalRecord(FichaOcupacional ficha, Step1Command cmd) {
        ficha.setFechaEvaluacion(cmd.fechaAtencion());
        ficha.setTipoEvaluacion(cmd.tipoEval());
        mapGinecologicalData(ficha, cmd);
        mapSpecialConditions(ficha, cmd);
        mapClinicalHistory(ficha, cmd);
        mapReproductiveHealth(ficha, cmd);
        normalizeCurrentDisease(ficha);
        mapDisabilityData(ficha, cmd);
        mapCatastrophicData(ficha, cmd);
        mapRetiroData(ficha, cmd);
        mapConsumoVidaCondToFicha(ficha, cmd);
    }

    private void mapGinecologicalData(FichaOcupacional ficha, Step1Command cmd) {
        ficha.setGinecoExamen1(cmd.ginecoExamen1());
        ficha.setGinecoTiempo1(cmd.ginecoTiempo1());
        ficha.setGinecoResultado1(cmd.ginecoResultado1());
        ficha.setGinecoExamen2(cmd.ginecoExamen2());
        ficha.setGinecoTiempo2(cmd.ginecoTiempo2());
        ficha.setGinecoResultado2(cmd.ginecoResultado2());
        ficha.setGinecoObservacion(cmd.ginecoObservacion());
    }

    private void mapSpecialConditions(FichaOcupacional ficha, Step1Command cmd) {
        ficha.setApEmbarazada(SnUtils.fromBoolean(cmd.apEmbarazada()));
        ficha.setApDiscapacidad(SnUtils.fromBoolean(cmd.apDiscapacidad()));
        ficha.setApCatastrofica(SnUtils.fromBoolean(cmd.apCatastrofica()));
        ficha.setApLactancia(SnUtils.fromBoolean(cmd.apLactancia()));
        ficha.setApAdultoMayor(SnUtils.fromBoolean(cmd.apAdultoMayor()));
    }

    private void mapClinicalHistory(FichaOcupacional ficha, Step1Command cmd) {
        ficha.setAntClinicoQuir(cmd.antClinicoQuirurgico());
        ficha.setAntFamiliares(cmd.antFamiliares());
        ficha.setCondicionEspecial(cmd.condicionEspecial());
        ficha.setAutorizaTransfusion(cmd.autorizaTransfusion());
        ficha.setTratHormonal(cmd.tratamientoHormonal());
        ficha.setTratHormonalCual(cmd.tratamientoHormonalCual());
    }

    private void mapReproductiveHealth(FichaOcupacional ficha, Step1Command cmd) {
        ficha.setExamReproMasc(cmd.examenReproMasculino());
        ficha.setTiempoReproMasc(cmd.tiempoReproMasculino());
        ficha.setFum(cmd.fum());
        ficha.setGestas(cmd.gestas());
        ficha.setPartos(cmd.partos());
        ficha.setCesareas(cmd.cesareas());
        ficha.setAbortos(cmd.abortos());
        ficha.setPlanificacion(cmd.planificacion());
        ficha.setPlanificacionCual(cmd.planificacionCual());
    }

    private void normalizeCurrentDisease(FichaOcupacional ficha) {
        if (ficha.getEnfermedadProbActual() != null) {
            ficha.setEnfermedadProbActual(ficha.getEnfermedadProbActual().trim());
        }
    }

    private void mapDisabilityData(FichaOcupacional ficha, Step1Command cmd) {
        if (Boolean.TRUE.equals(cmd.apDiscapacidad())) {
            ficha.setDisTipo(trimToNull(cmd.discapTipo()));
            ficha.setDisDescripcion(trimToNull(cmd.discapDesc()));
            ficha.setDisPorcentaje(cmd.discapPorc());
        } else {
            ficha.setDisTipo(null);
            ficha.setDisDescripcion(null);
            ficha.setDisPorcentaje(null);
        }
    }

    private void mapCatastrophicData(FichaOcupacional ficha, Step1Command cmd) {
        if (Boolean.TRUE.equals(cmd.apCatastrofica())) {
            ficha.setCatDiagnostico(trimToNull(cmd.catasDiagnostico()));
            ficha.setCatCalificada(Boolean.TRUE.equals(cmd.catasCalificada()) ? "S" : "N");
        } else {
            ficha.setCatDiagnostico(null);
            ficha.setCatCalificada(null);
        }
    }

    private void mapRetiroData(FichaOcupacional ficha, Step1Command cmd) {
        String tipo = trimToNull(cmd.tipoEval());
        if ("RETIRO".equalsIgnoreCase(tipo)) {
            String realiza = trimToNull(cmd.nRealizaEvaluacion());
            String relTrab = trimToNull(cmd.nRelacionTrabajo());

            ficha.setnRetEval(realiza != null ? realiza : "N");
            ficha.setnRetRelTrab(relTrab != null ? relTrab : "N");
            ficha.setnRetObs(trimToNull(cmd.nObsRetiro()));
        } else {
            ficha.setnRetEval("N");
            ficha.setnRetRelTrab("N");
            ficha.setnRetObs(null);
        }
    }

    private void mapConsumoVidaCondToFicha(FichaOcupacional ficha, Step1Command cmd) {
        ConsumptionArrays arrays = buildConsumptionArrays(cmd);
        applyTabacoData(ficha, arrays);
        applyAlcoholData(ficha, arrays);
        applyOtrosData(ficha, cmd, arrays);
        applyActividadFisicaData(ficha, arrays);
        applyMedicacionData(ficha, arrays);
        ficha.setObsConsumoVidaCond(cmd.consumoVidaCondObs());
    }

    private ConsumptionArrays buildConsumptionArrays(Step1Command cmd) {
        return new ConsumptionArrays(
                ensureSize3(cmd.consTiempoConsumoMeses(), new Integer[3]),
                ensureSize3(cmd.consExConsumidor(), new Boolean[3]),
                ensureSize3(cmd.consTiempoAbstinenciaMeses(), new Integer[3]),
                ensureSize3(cmd.consNoConsume(), new Boolean[3]),
                ensureSize3(cmd.afCual(), new String[3]),
                ensureSize3(cmd.afTiempo(), new String[3]),
                ensureSize3(cmd.medCual(), new String[3]),
                ensureSize3(cmd.medCant(), new Integer[3]));
    }

    private void applyTabacoData(FichaOcupacional ficha, ConsumptionArrays arrays) {
        ficha.setTabConsMeses(arrays.consTiempoConsumoMeses()[0]);
        ficha.setTabExCons(SnUtils.fromBoolean(arrays.consExConsumidor()[0]));
        ficha.setTabAbsMeses(arrays.consTiempoAbstinenciaMeses()[0]);
        ficha.setTabNoCons(SnUtils.fromBoolean(arrays.consNoConsume()[0]));
    }

    private void applyAlcoholData(FichaOcupacional ficha, ConsumptionArrays arrays) {
        ficha.setAlcConsMeses(arrays.consTiempoConsumoMeses()[1]);
        ficha.setAlcExCons(SnUtils.fromBoolean(arrays.consExConsumidor()[1]));
        ficha.setAlcAbsMeses(arrays.consTiempoAbstinenciaMeses()[1]);
        ficha.setAlcNoCons(SnUtils.fromBoolean(arrays.consNoConsume()[1]));
    }

    private void applyOtrosData(FichaOcupacional ficha, Step1Command cmd, ConsumptionArrays arrays) {
        ficha.setOtrCual(cmd.consOtrasCual());
        ficha.setOtrConsMeses(arrays.consTiempoConsumoMeses()[2]);
        ficha.setOtrExCons(SnUtils.fromBoolean(arrays.consExConsumidor()[2]));
        ficha.setOtrAbsMeses(arrays.consTiempoAbstinenciaMeses()[2]);
        ficha.setOtrNoCons(SnUtils.fromBoolean(arrays.consNoConsume()[2]));
    }

    private void applyActividadFisicaData(FichaOcupacional ficha, ConsumptionArrays arrays) {
        ficha.setAfCual1(arrays.afCual()[0]);
        ficha.setAfTiempo1(arrays.afTiempo()[0]);
        ficha.setAfCual2(arrays.afCual()[1]);
        ficha.setAfTiempo2(arrays.afTiempo()[1]);
        ficha.setAfCual3(arrays.afCual()[2]);
        ficha.setAfTiempo3(arrays.afTiempo()[2]);
    }

    private void applyMedicacionData(FichaOcupacional ficha, ConsumptionArrays arrays) {
        ficha.setMedCual1(arrays.medCual()[0]);
        ficha.setMedCant1(arrays.medCant()[0]);
        ficha.setMedCual2(arrays.medCual()[1]);
        ficha.setMedCant2(arrays.medCant()[1]);
        ficha.setMedCual3(arrays.medCual()[2]);
        ficha.setMedCant3(arrays.medCant()[2]);
    }

    private SignosVitales upsertVitalSigns(FichaOcupacional ficha, Step1Command cmd, Date now, String user) {
        SignosVitales current = (ficha.getSignos() != null) ? ficha.getSignos() : cmd.signos();

        try {
            SignosVitales sv = step1VitalSignsManager.upsertVitalSigns(
                    current,
                    cmd.paStr(),
                    cmd.temp(),
                    cmd.fc(),
                    cmd.fr(),
                    cmd.satO2(),
                    cmd.peso(),
                    cmd.tallaCm(),
                    cmd.perimetroAbd(),
                    now,
                    user);

            ficha.setSignos(sv);
            return sv;
        } catch (IllegalArgumentException ex) {
            throw new Step1ValidationException(ex.getMessage());
        }
    }

    private FichaOcupacional saveDraftOccupationalRecord(FichaOcupacional ficha, PersonaAux personaAux, Date now, String user) {
        ficha.setEstado("BORRADOR");
        if (ficha.getFechaEmision() == null) {
            ficha.setFechaEmision(now);
        }

        stampAuditFieldsForFicha(ficha, now, user);
        asegurarPersonaAuxPersistida(ficha, personaAux, now, user);
        return fichaService.guardar(ficha);
    }

    private void asegurarPersonaAuxPersistida(FichaOcupacional ficha, PersonaAux personaAux, Date now, String user) {
        if (ficha == null || ficha.getEmpleado() != null || personaAux == null) {
            return;
        }

        PersonaAux p = ficha.getPersonaAux();
        if (p != null && p.getIdPersonaAux() == null) {
            p.setFechaCreacion(now);
            p.setUsrCreacion(user);
            p = personaAuxService.guardar(p);
            ficha.setPersonaAux(p);
        }
    }

    private void stampAuditFieldsForFicha(FichaOcupacional f, Date now, String user) {
        if (f.getIdFicha() == null) {
            f.setFechaCreacion(now);
            f.setUsrCreacion(user);
        } else {
            f.setFechaActualizacion(now);
            f.setUsrActualizacion(user);
        }
    }

    private void auditStep1(FichaOcupacional ficha, SignosVitales sv) {
        try {
            centroMedicoAuditService.registrar("GUARDAR_STEP1", "FICHA_OCUPACIONAL", "*",
                    "Step 1 guardado. ID_FICHA=" + ficha.getIdFicha());
            centroMedicoAuditService.registrar("GUARDAR_STEP1", "SIGNOS_VITALES", "*",
                    "Signos guardados. ID_SIGNOS=" + sv.getIdSignos());
        } catch (RuntimeException ignored) {
            // No bloquear guardado por error de auditoría
        }
    }

    private static <T> T[] ensureSize3(T[] input, T[] defaults) {
        if (input == null || input.length < 3) {
            return defaults;
        }
        return input;
    }

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void fail(String message) {
        throw new Step1ValidationException(message);
    }

    private record PatientAssignment(PersonaAux personaAux) {
    }

    private record ConsumptionArrays(
            Integer[] consTiempoConsumoMeses,
            Boolean[] consExConsumidor,
            Integer[] consTiempoAbstinenciaMeses,
            Boolean[] consNoConsume,
            String[] afCual,
            String[] afTiempo,
            String[] medCual,
            Integer[] medCant) {
    }

    public static class Step1ValidationException extends RuntimeException {
        public Step1ValidationException(String message) {
            super(message);
        }
    }

    public record Step1Result(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux, SignosVitales signos) {
    }

    public record Step1Command(
            FichaOcupacional ficha,
            DatEmpleado empleadoSel,
            PersonaAux personaAux,
            SignosVitales signos,
            Integer noPersonaSel,
            Date fechaAtencion,
            String tipoEval,
            String paStr,
            Double temp,
            Integer fc,
            Integer fr,
            Integer satO2,
            Double peso,
            Double tallaCm,
            Double perimetroAbd,
            Boolean apEmbarazada,
            Boolean apDiscapacidad,
            Boolean apCatastrofica,
            Boolean apLactancia,
            Boolean apAdultoMayor,
            String antClinicoQuirurgico,
            String antFamiliares,
            String condicionEspecial,
            String autorizaTransfusion,
            String tratamientoHormonal,
            String tratamientoHormonalCual,
            String examenReproMasculino,
            Integer tiempoReproMasculino,
            String ginecoExamen1,
            String ginecoTiempo1,
            String ginecoResultado1,
            String ginecoExamen2,
            String ginecoTiempo2,
            String ginecoResultado2,
            String ginecoObservacion,
            Date fum,
            Integer gestas,
            Integer partos,
            Integer cesareas,
            Integer abortos,
            String planificacion,
            String planificacionCual,
            String discapTipo,
            String discapDesc,
            Integer discapPorc,
            String catasDiagnostico,
            Boolean catasCalificada,
            String nRealizaEvaluacion,
            String nRelacionTrabajo,
            String nObsRetiro,
            Integer[] consTiempoConsumoMeses,
            Boolean[] consExConsumidor,
            Integer[] consTiempoAbstinenciaMeses,
            Boolean[] consNoConsume,
            String consOtrasCual,
            String[] afCual,
            String[] afTiempo,
            String[] medCual,
            Integer[] medCant,
            String consumoVidaCondObs,
            String usuario
    ) {
    }
}
