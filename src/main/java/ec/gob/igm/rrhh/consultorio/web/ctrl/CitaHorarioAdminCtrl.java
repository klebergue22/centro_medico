package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.CitEspecialidad;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.citas.CitaHorarioAdminService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("citaHorarioAdminCtrl")
@ViewScoped
public class CitaHorarioAdminCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CitaHorarioAdminService citaHorarioAdminService;

    @Inject
    private UserContextService userContextService;

    private Long idProfesionalSel;
    private List<Long> diasSemanaSel = new ArrayList<>();
    private Long duracionMinSel = 30L;
    private Date fechaGeneracion = new Date();
    private String periodicidadSel = "SEMANAL";
    private Long ciclosGeneracion = 1L;

    private List<CitProfesional> profesionales;
    private List<CitProfesional> profesionalesGestion;
    private List<UsuarioAuth> usuariosProfesionales;
    private List<CitEspecialidad> especialidades;

    private Long idProfesionalEdit;
    private Long idUsuarioEdit;
    private boolean ingresoManualProfesional;
    private String nombreProfesionalEdit;
    private String codigoProfesionalEdit;
    private String emailProfesionalEdit;
    private Long idEspecialidadEdit;
    private String activoEdit = "S";

    @PostConstruct
    public void init() {
        profesionales = citaHorarioAdminService.listarProfesionalesActivos();
        profesionalesGestion = citaHorarioAdminService.listarProfesionalesGestion();
        usuariosProfesionales = citaHorarioAdminService.listarUsuariosProfesionales();
        especialidades = citaHorarioAdminService.listarEspecialidadesActivas();
        if (profesionales == null || profesionales.isEmpty()) {
            boolean enviado = citaHorarioAdminService.notificarAdministradorSinProfesionales(
                    userContextService.resolveCurrentUser()
            );
            if (enviado) {
                addWarn("No hay profesionales activos. Se notificó por correo al administrador.");
            } else {
                addWarn("No hay profesionales activos. No se pudo notificar por correo al administrador.");
            }
        }
    }

    public void guardarHorario() {
        try {
            if (diasSemanaSel == null || diasSemanaSel.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un día de lunes a viernes.");
            }
            for (Long diaSemana : diasSemanaSel) {
                citaHorarioAdminService.guardarHorarioBase(
                        idProfesionalSel,
                        diaSemana == null ? null : diaSemana.intValue(),
                        duracionMinSel == null ? null : duracionMinSel.intValue(),
                        userContextService.resolveCurrentUser()
                );
            }
            addInfo("Horario guardado correctamente para " + diasSemanaSel.size() + " día(s).");
        } catch (Exception e) {
            addWarn(e.getMessage());
        }
    }

    public void generarSlots() {
        try {
            LocalDate fecha = fechaGeneracion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int creados = citaHorarioAdminService.generarSlotsPeriodicos(
                    idProfesionalSel,
                    fecha,
                    periodicidadSel,
                    ciclosGeneracion == null ? null : ciclosGeneracion.intValue(),
                    userContextService.resolveCurrentUser()
            );
            addInfo("Slots generados: " + creados + ".");
        } catch (IllegalArgumentException e) {
            String detalle = e.getMessage();
            if (detalle != null && detalle.contains("No existe horario activo")) {
                detalle = detalle + " Verifique que la fecha base pertenezca a un día configurado y guardado en horario base.";
            }
            addWarn(detalle);
        } catch (Exception e) {
            addWarn(e.getMessage());
        }
    }

    public void prepararNuevoProfesional() {
        idProfesionalEdit = null;
        idUsuarioEdit = null;
        ingresoManualProfesional = false;
        nombreProfesionalEdit = null;
        codigoProfesionalEdit = null;
        emailProfesionalEdit = null;
        idEspecialidadEdit = null;
        activoEdit = "S";
    }

    public void seleccionarProfesional(CitProfesional profesional) {
        if (profesional == null) {
            return;
        }
        idProfesionalEdit = profesional.getIdProfesional();
        idUsuarioEdit = profesional.getUsuario() != null ? profesional.getUsuario().getIdUsuario() : null;
        ingresoManualProfesional = idUsuarioEdit == null;
        nombreProfesionalEdit = profesional.getNombreProfesional();
        codigoProfesionalEdit = profesional.getCodigoProfesional();
        emailProfesionalEdit = profesional.getEmail();
        idEspecialidadEdit = profesional.getEspecialidad() != null ? profesional.getEspecialidad().getIdEspecialidad() : null;
        activoEdit = profesional.getActivo();
    }

    public void onUsuarioProfesionalChange() {
        if (idUsuarioEdit == null || usuariosProfesionales == null) {
            if (!ingresoManualProfesional) {
                nombreProfesionalEdit = null;
                emailProfesionalEdit = null;
            }
            return;
        }
        for (UsuarioAuth usuario : usuariosProfesionales) {
            if (usuario != null && idUsuarioEdit.equals(usuario.getIdUsuario())) {
                nombreProfesionalEdit = usuario.getNombreVisible();
                emailProfesionalEdit = usuario.getEmail();
                break;
            }
        }
    }

    public void onIngresoManualChange() {
        if (ingresoManualProfesional) {
            idUsuarioEdit = null;
            nombreProfesionalEdit = null;
            emailProfesionalEdit = null;
            return;
        }
        onUsuarioProfesionalChange();
    }

    public void guardarProfesional() {
        try {
            String usuario = userContextService.resolveCurrentUser();
            if (idProfesionalEdit == null) {
                citaHorarioAdminService.crearProfesional(
                        idUsuarioEdit,
                        nombreProfesionalEdit,
                        codigoProfesionalEdit,
                        emailProfesionalEdit,
                        idEspecialidadEdit,
                        activoEdit,
                        usuario
                );
                addInfo("Profesional registrado correctamente.");
            } else {
                citaHorarioAdminService.actualizarProfesional(
                        idProfesionalEdit,
                        idUsuarioEdit,
                        nombreProfesionalEdit,
                        codigoProfesionalEdit,
                        emailProfesionalEdit,
                        idEspecialidadEdit,
                        activoEdit,
                        usuario
                );
                addInfo("Profesional actualizado correctamente.");
            }
            refrescarCatalogos();
            prepararNuevoProfesional();
        } catch (Exception e) {
            addWarn(e.getMessage());
        }
    }

    public void eliminarProfesional() {
        try {
            citaHorarioAdminService.eliminarProfesional(idProfesionalEdit, userContextService.resolveCurrentUser());
            addInfo("Profesional desactivado correctamente.");
            refrescarCatalogos();
            prepararNuevoProfesional();
        } catch (Exception e) {
            addWarn(e.getMessage());
        }
    }

    private void refrescarCatalogos() {
        profesionales = citaHorarioAdminService.listarProfesionalesActivos();
        profesionalesGestion = citaHorarioAdminService.listarProfesionalesGestion();
        usuariosProfesionales = citaHorarioAdminService.listarUsuariosProfesionales();
    }

    private void addInfo(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    private void addWarn(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, summary, null));
    }

    public Long getIdProfesionalSel() {
        return idProfesionalSel;
    }

    public void setIdProfesionalSel(Long idProfesionalSel) {
        this.idProfesionalSel = idProfesionalSel;
    }

    public List<Long> getDiasSemanaSel() {
        return diasSemanaSel;
    }

    public void setDiasSemanaSel(List<Long> diasSemanaSel) {
        this.diasSemanaSel = diasSemanaSel;
    }

    public Long getDuracionMinSel() {
        return duracionMinSel;
    }

    public void setDuracionMinSel(Long duracionMinSel) {
        this.duracionMinSel = duracionMinSel;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }



    public String getPeriodicidadSel() {
        return periodicidadSel;
    }

    public void setPeriodicidadSel(String periodicidadSel) {
        this.periodicidadSel = periodicidadSel;
    }

    public Long getCiclosGeneracion() {
        return ciclosGeneracion;
    }

    public void setCiclosGeneracion(Long ciclosGeneracion) {
        this.ciclosGeneracion = ciclosGeneracion;
    }
    public List<CitProfesional> getProfesionales() {
        return profesionales;
    }

    public List<CitProfesional> getProfesionalesGestion() {
        return profesionalesGestion;
    }

    public List<CitEspecialidad> getEspecialidades() {
        return especialidades;
    }

    public Long getIdProfesionalEdit() {
        return idProfesionalEdit;
    }

    public void setIdProfesionalEdit(Long idProfesionalEdit) {
        this.idProfesionalEdit = idProfesionalEdit;
    }

    public String getNombreProfesionalEdit() {
        return nombreProfesionalEdit;
    }

    public void setNombreProfesionalEdit(String nombreProfesionalEdit) {
        this.nombreProfesionalEdit = nombreProfesionalEdit;
    }

    public String getCodigoProfesionalEdit() {
        return codigoProfesionalEdit;
    }

    public List<UsuarioAuth> getUsuariosProfesionales() {
        return usuariosProfesionales;
    }

    public Long getIdUsuarioEdit() {
        return idUsuarioEdit;
    }

    public void setIdUsuarioEdit(Long idUsuarioEdit) {
        this.idUsuarioEdit = idUsuarioEdit;
    }

    public boolean isIngresoManualProfesional() {
        return ingresoManualProfesional;
    }

    public void setIngresoManualProfesional(boolean ingresoManualProfesional) {
        this.ingresoManualProfesional = ingresoManualProfesional;
    }

    public void setCodigoProfesionalEdit(String codigoProfesionalEdit) {
        this.codigoProfesionalEdit = codigoProfesionalEdit;
    }

    public String getEmailProfesionalEdit() {
        return emailProfesionalEdit;
    }

    public void setEmailProfesionalEdit(String emailProfesionalEdit) {
        this.emailProfesionalEdit = emailProfesionalEdit;
    }

    public Long getIdEspecialidadEdit() {
        return idEspecialidadEdit;
    }

    public void setIdEspecialidadEdit(Long idEspecialidadEdit) {
        this.idEspecialidadEdit = idEspecialidadEdit;
    }

    public String getActivoEdit() {
        return activoEdit;
    }

    public void setActivoEdit(String activoEdit) {
        this.activoEdit = activoEdit;
    }
}
