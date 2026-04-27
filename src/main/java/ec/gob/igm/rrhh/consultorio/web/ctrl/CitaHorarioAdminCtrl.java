package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
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
    private Integer diaSemanaSel;
    private Integer duracionMinSel = 20;
    private Date fechaGeneracion = new Date();

    private List<CitProfesional> profesionales;

    @PostConstruct
    public void init() {
        profesionales = citaHorarioAdminService.listarProfesionalesActivos();
    }

    public void guardarHorario() {
        try {
            citaHorarioAdminService.guardarHorarioBase(
                    idProfesionalSel,
                    diaSemanaSel,
                    duracionMinSel,
                    userContextService.resolveCurrentUser()
            );
            addInfo("Horario guardado correctamente.");
        } catch (Exception e) {
            addWarn(e.getMessage());
        }
    }

    public void generarSlots() {
        try {
            LocalDate fecha = fechaGeneracion.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int creados = citaHorarioAdminService.generarSlotsParaFecha(
                    idProfesionalSel,
                    fecha,
                    userContextService.resolveCurrentUser()
            );
            addInfo("Slots generados: " + creados + ".");
        } catch (Exception e) {
            addWarn(e.getMessage());
        }
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

    public Integer getDiaSemanaSel() {
        return diaSemanaSel;
    }

    public void setDiaSemanaSel(Integer diaSemanaSel) {
        this.diaSemanaSel = diaSemanaSel;
    }

    public Integer getDuracionMinSel() {
        return duracionMinSel;
    }

    public void setDuracionMinSel(Integer duracionMinSel) {
        this.duracionMinSel = duracionMinSel;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public List<CitProfesional> getProfesionales() {
        return profesionales;
    }
}
