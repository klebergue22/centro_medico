package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfControllerSupport;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;

@Stateless
public class PdfFichaInputAssembler {

    public CentroMedicoPdfControllerSupport.CapturePdfFichaInput capture(CentroMedicoCtrl source,
            Logger logger,
            Runnable asegurarPersonaAuxPersistida,
            Runnable syncCamposDesdeObjetos,
            Runnable recalcularIMC,
            CentroMedicoPdfFacade centroMedicoPdfFacade,
            PdfResourceResolver pdfResourceResolver,
            int hRows) {
        CentroMedicoPdfControllerSupport.CapturePdfFichaInput input = new CentroMedicoPdfControllerSupport.CapturePdfFichaInput();
        populateFichaInputCore(input, source, logger, asegurarPersonaAuxPersistida, syncCamposDesdeObjetos, recalcularIMC);
        populateFichaInputFlags(input, source, centroMedicoPdfFacade, pdfResourceResolver, hRows);
        populateActividadLaboralInput(input, source);
        return input;
    }

    public CentroMedicoPdfControllerSupport.SyncCamposDesdeObjetosInput buildSyncCamposDesdeObjetosInput(
            CentroMedicoCtrl source,
            ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler fichaPdfContextAssembler,
            ec.gob.igm.rrhh.consultorio.web.service.FichaPdfDataMapper fichaPdfDataMapper) {
        CentroMedicoPdfControllerSupport.SyncCamposDesdeObjetosInput input = new CentroMedicoPdfControllerSupport.SyncCamposDesdeObjetosInput();
        input.fichaPdfContextAssembler = fichaPdfContextAssembler;
        input.fichaPdfDataMapper = fichaPdfDataMapper;
        input.ficha = source.getFicha();
        input.empleadoSel = source.getEmpleadoSel();
        input.personaAux = source.getPersonaAux();
        input.fechaNacimiento = source.getFechaNacimiento();
        populateSyncCommonSetters(input, source);
        populateSyncPatientSetters(input, source);
        return input;
    }

    private void populateFichaInputCore(CentroMedicoPdfControllerSupport.CapturePdfFichaInput input, CentroMedicoCtrl source,
            Logger logger, Runnable asegurarPersonaAuxPersistida, Runnable syncCamposDesdeObjetos,
            Runnable recalcularIMC) {
        input.source = source;
        input.log = logger;
        input.ficha = source.getFicha();
        input.empleadoSel = source.getEmpleadoSel();
        input.personaAux = source.getPersonaAux();
        input.permitirIngresoManual = source.isPermitirIngresoManual();
        input.asegurarPersonaAuxPersistida = asegurarPersonaAuxPersistida;
        input.syncCamposDesdeObjetos = syncCamposDesdeObjetos;
        input.tipoEval = source.getTipoEval();
        input.tipoEvaluacion = source.getTipoEvaluacion();
        input.recalcularIMC = recalcularIMC;
    }

    private void populateFichaInputFlags(CentroMedicoPdfControllerSupport.CapturePdfFichaInput input, CentroMedicoCtrl source,
            CentroMedicoPdfFacade centroMedicoPdfFacade, PdfResourceResolver pdfResourceResolver, int hRows) {
        input.centroMedicoPdfFacade = centroMedicoPdfFacade;
        input.pdfResourceResolver = pdfResourceResolver;
        input.apDiscapacidad = source.isApDiscapacidad();
        input.apCatastrofica = source.isApCatastrofica();
        input.apEmbarazada = source.isApEmbarazada();
        input.apLactancia = source.isApLactancia();
        input.apAdultoMayor = source.isApAdultoMayor();
        input.hRows = hRows;
    }

    private void populateActividadLaboralInput(CentroMedicoPdfControllerSupport.CapturePdfFichaInput input,
            CentroMedicoCtrl source) {
        input.actLabCentroTrabajo = source.getActLabCentroTrabajo();
        input.actLabActividad = source.getActLabActividad();
        input.actLabTiempo = source.getActLabTiempo();
        input.actLabTrabajoAnterior = source.getActLabTrabajoAnterior();
        input.actLabTrabajoActual = source.getActLabTrabajoActual();
        input.actLabIncidenteChk = source.getActLabIncidenteChk();
        input.actLabAccidenteChk = source.getActLabAccidenteChk();
        input.actLabEnfermedadChk = source.getActLabEnfermedadChk();
        input.iessSi = source.getIessSi();
        input.iessNo = source.getIessNo();
        input.iessFecha = source.getIessFecha();
        input.iessEspecificar = source.getIessEspecificar();
        input.actLabObservaciones = source.getActLabObservaciones();
    }

    private void populateSyncCommonSetters(CentroMedicoPdfControllerSupport.SyncCamposDesdeObjetosInput input,
            CentroMedicoCtrl source) {
        input.institucionSetter = source::setInstitucion;
        input.rucSetter = source::setRuc;
        input.centroTrabajoSetter = source::setCentroTrabajo;
        input.ciiuSetter = source::setCiiu;
        input.noHistoriaSetter = source::setNoHistoria;
        input.noArchivoSetter = source::setNoArchivo;
        input.motivoObsSetter = source::setMotivoObs;
        input.ginecoExamen1Setter = source::setGinecoExamen1;
        input.ginecoTiempo1Setter = source::setGinecoTiempo1;
        input.ginecoResultado1Setter = source::setGinecoResultado1;
        input.ginecoExamen2Setter = source::setGinecoExamen2;
        input.ginecoTiempo2Setter = source::setGinecoTiempo2;
        input.ginecoResultado2Setter = source::setGinecoResultado2;
        input.ginecoObservacionSetter = source::setGinecoObservacion;
        input.enfermedadActualSetter = source::setEnfermedadActual;
    }

    private void populateSyncPatientSetters(CentroMedicoPdfControllerSupport.SyncCamposDesdeObjetosInput input,
            CentroMedicoCtrl source) {
        input.apellido1Setter = source::setApellido1;
        input.apellido2Setter = source::setApellido2;
        input.nombre1Setter = source::setNombre1;
        input.nombre2Setter = source::setNombre2;
        input.sexoSetter = source::setSexo;
        input.grupoSanguineoSetter = source::setGrupoSanguineo;
        input.fechaNacimientoSetter = source::setFechaNacimiento;
        input.edadSetter = source::setEdad;
        input.personaAuxSetter = source::setPersonaAux;
    }
}
