package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.dto.CitaPacienteDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.CitCita;
import ec.gob.igm.rrhh.consultorio.domain.model.CitEspecialidad;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.citas.CitaCatalogoService;
import ec.gob.igm.rrhh.consultorio.service.citas.CitaCommandService;
import ec.gob.igm.rrhh.consultorio.service.citas.CitaPacienteLookupService;
import ec.gob.igm.rrhh.consultorio.service.UsuarioAuthService;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named("citaMedicaCtrl")
@ViewScoped
public class CitaMedicaCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CitaPacienteLookupService citaPacienteLookupService;

    @Inject
    private CitaCatalogoService citaCatalogoService;

    @Inject
    private CitaCommandService citaCommandService;

    @Inject
    private CentroMedicoAuditService auditService;

    @Inject
    private UserContextService userContextService;
    @Inject
    private UsuarioAuthService usuarioAuthService;

    private String cedulaBusqueda;
    private CitaPacienteDTO pacienteEncontrado;

    private Long idEspecialidadSel;
    private Long idProfesionalSel;
    private Long idSlotSel;
    private Date fechaAgenda;
    private String correoPaciente;
    private String telefonoPaciente;
    private String motivoAtencion;

    private Long idCitaSel;
    private Long idNuevoSlotSel;
    private String motivoCancelacion;
    private String observacionReprogramacion;

    private List<CitEspecialidad> especialidades;
    private List<CitProfesional> profesionales;
    private List<CitSlotAgenda> slotsDisponibles;
    private List<CitCita> misCitas;

    @PostConstruct
    public void init() {
        fechaAgenda = new Date();
        especialidades = citaCatalogoService.listarEspecialidadesActivas();
        profesionales = List.of();
        slotsDisponibles = List.of();
        recargarMisCitas();
    }

    public void buscarPaciente() {
        try {
            pacienteEncontrado = citaPacienteLookupService.buscarPorCedula(cedulaBusqueda);
            if (pacienteEncontrado.isRequiereCrearFicha()) {
                addWarn("Paciente sin ficha activa", "Debe crear/activar ficha antes de agendar cita.");
            } else {
                addInfo("Paciente encontrado", pacienteEncontrado.getNombreCompleto());
            }
        } catch (Exception e) {
            addError("No se pudo buscar paciente", e.getMessage());
        }
    }

    public void onEspecialidadChange() {
        idProfesionalSel = null;
        idSlotSel = null;
        profesionales = citaCatalogoService.listarProfesionalesActivos(idEspecialidadSel);
        slotsDisponibles = List.of();
    }

    public void buscarSlotsDisponibles() {
        slotsDisponibles = citaCatalogoService.listarSlotsDisponibles(idEspecialidadSel, idProfesionalSel, fechaAgenda);
        if (slotsDisponibles.isEmpty()) {
            addWarn("Sin horarios", "No existen slots disponibles para el filtro seleccionado.");
        }
    }

    public void agendarCita() {
        if (!validarPrecondicionesAgendar()) {
            return;
        }

        try {
            Long idUsuario = resolveUsuarioIdSesion();
            CitCita cita = citaCommandService.agendar(
                    idSlotSel,
                    pacienteEncontrado.getIdFichaActiva(),
                    pacienteEncontrado.getNoPersona(),
                    pacienteEncontrado.getIdPersonaAux(),
                    idUsuario,
                    correoPaciente,
                    telefonoPaciente,
                    motivoAtencion,
                    userContextService.resolveCurrentUser()
            );

            auditService.registrar("INSERT", "CIT_CITA", "ID_CITA",
                    "Cita agendada ID=" + cita.getIdCita() + ", CEDULA=" + pacienteEncontrado.getCedula());

            addInfo("Cita agendada", "La cita fue creada correctamente.");
            limpiarFormularioAgendamiento();
            recargarMisCitas();
        } catch (Exception e) {
            addError("No se pudo agendar", e.getMessage());
        }
    }

    public void cancelarCita() {
        if (idCitaSel == null) {
            addWarn("Cita requerida", "Seleccione una cita para cancelar.");
            return;
        }

        try {
            CitCita cita = citaCommandService.cancelar(idCitaSel, motivoCancelacion, userContextService.resolveCurrentUser());
            auditService.registrar("UPDATE", "CIT_CITA", "ESTADO", "Cita cancelada ID=" + cita.getIdCita());
            addInfo("Cita cancelada", "Se canceló correctamente la cita.");
            motivoCancelacion = null;
            recargarMisCitas();
        } catch (Exception e) {
            addError("No se pudo cancelar", e.getMessage());
        }
    }

    public void reprogramarCita() {
        if (idCitaSel == null || idNuevoSlotSel == null) {
            addWarn("Datos incompletos", "Seleccione cita y nuevo horario.");
            return;
        }

        try {
            CitCita cita = citaCommandService.reprogramar(idCitaSel, idNuevoSlotSel, observacionReprogramacion,
                    userContextService.resolveCurrentUser());
            auditService.registrar("UPDATE", "CIT_CITA", "ID_SLOT", "Cita reprogramada ID=" + cita.getIdCita());
            addInfo("Cita reprogramada", "La cita fue reagendada correctamente.");
            observacionReprogramacion = null;
            recargarMisCitas();
        } catch (Exception e) {
            addError("No se pudo reprogramar", e.getMessage());
        }
    }

    public void seleccionarCita(Long idCita) {
        this.idCitaSel = idCita;
    }

    public void recargarMisCitas() {
        try {
            Long idUsuario = resolveUsuarioIdSesion();
            misCitas = citaCommandService.listarCitasPorUsuario(idUsuario);
        } catch (Exception e) {
            misCitas = List.of();
        }
    }

    private boolean validarPrecondicionesAgendar() {
        if (pacienteEncontrado == null) {
            addWarn("Paciente requerido", "Busque y seleccione un paciente primero.");
            return false;
        }
        if (pacienteEncontrado.isRequiereCrearFicha()) {
            addWarn("Ficha requerida", "No se puede agendar sin ficha activa.");
            return false;
        }
        if (idSlotSel == null) {
            addWarn("Horario requerido", "Seleccione un slot disponible.");
            return false;
        }
        return true;
    }

    private void limpiarFormularioAgendamiento() {
        idSlotSel = null;
        motivoAtencion = null;
        telefonoPaciente = null;
        slotsDisponibles = List.of();
    }

    private Long resolveUsuarioIdSesion() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        Object authUser = sessionMap.get("AUTH_USER");
        if (authUser == null) {
            throw new IllegalStateException("Sesión inválida: AUTH_USER no disponible.");
        }
        String cedula = authUser.toString();
        UsuarioAuth usuario = usuarioAuthService.findByUsernameOrCedula(cedula);
        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new IllegalStateException("No se pudo resolver el usuario autenticado por cédula.");
        }
        return usuario.getIdUsuario();
    }

    private void addInfo(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void addWarn(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail));
    }

    private void addError(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }

    public String getCedulaBusqueda() { return cedulaBusqueda; }
    public void setCedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; }
    public CitaPacienteDTO getPacienteEncontrado() { return pacienteEncontrado; }
    public Long getIdEspecialidadSel() { return idEspecialidadSel; }
    public void setIdEspecialidadSel(Long idEspecialidadSel) { this.idEspecialidadSel = idEspecialidadSel; }
    public Long getIdProfesionalSel() { return idProfesionalSel; }
    public void setIdProfesionalSel(Long idProfesionalSel) { this.idProfesionalSel = idProfesionalSel; }
    public Long getIdSlotSel() { return idSlotSel; }
    public void setIdSlotSel(Long idSlotSel) { this.idSlotSel = idSlotSel; }
    public Date getFechaAgenda() { return fechaAgenda; }
    public void setFechaAgenda(Date fechaAgenda) { this.fechaAgenda = fechaAgenda; }
    public String getCorreoPaciente() { return correoPaciente; }
    public void setCorreoPaciente(String correoPaciente) { this.correoPaciente = correoPaciente; }
    public String getTelefonoPaciente() { return telefonoPaciente; }
    public void setTelefonoPaciente(String telefonoPaciente) { this.telefonoPaciente = telefonoPaciente; }
    public String getMotivoAtencion() { return motivoAtencion; }
    public void setMotivoAtencion(String motivoAtencion) { this.motivoAtencion = motivoAtencion; }
    public Long getIdCitaSel() { return idCitaSel; }
    public void setIdCitaSel(Long idCitaSel) { this.idCitaSel = idCitaSel; }
    public Long getIdNuevoSlotSel() { return idNuevoSlotSel; }
    public void setIdNuevoSlotSel(Long idNuevoSlotSel) { this.idNuevoSlotSel = idNuevoSlotSel; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public String getObservacionReprogramacion() { return observacionReprogramacion; }
    public void setObservacionReprogramacion(String observacionReprogramacion) { this.observacionReprogramacion = observacionReprogramacion; }
    public List<CitEspecialidad> getEspecialidades() { return especialidades; }
    public List<CitProfesional> getProfesionales() { return profesionales; }
    public List<CitSlotAgenda> getSlotsDisponibles() { return slotsDisponibles; }
    public List<CitCita> getMisCitas() { return misCitas; }
}
