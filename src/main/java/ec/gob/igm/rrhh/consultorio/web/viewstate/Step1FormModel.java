package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.Date;

/**
 * Class Step1FormModel: contiene la lógica de la aplicación.
 */
public class Step1FormModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String institucion;
    private String ruc;
    private String ciiu;
    private String centroTrabajo;
    private String noHistoria;
    private String noArchivo;
    private Date fechaAtencion;
    private String tipoEval;
    private String tipoEvaluacion;
    private Date fecIngreso;
    private Date fecReintegro;
    private Date fecRetiro;
    private String grupoSanguineo;
    private String lateralidad;
    private String motivoObs;
    private PacienteFormData paciente = new PacienteFormData();

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getCiiu() { return ciiu; }
    public void setCiiu(String ciiu) { this.ciiu = ciiu; }
    public String getCentroTrabajo() { return centroTrabajo; }
    public void setCentroTrabajo(String centroTrabajo) { this.centroTrabajo = centroTrabajo; }
    public String getNoHistoria() { return noHistoria; }
    public void setNoHistoria(String noHistoria) { this.noHistoria = noHistoria; }
    public String getNoArchivo() { return noArchivo; }
    public void setNoArchivo(String noArchivo) { this.noArchivo = noArchivo; }
    public Date getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(Date fechaAtencion) { this.fechaAtencion = fechaAtencion; }
    public String getTipoEval() { return tipoEval; }
    public void setTipoEval(String tipoEval) { this.tipoEval = tipoEval; }
    public String getTipoEvaluacion() { return tipoEvaluacion; }
    public void setTipoEvaluacion(String tipoEvaluacion) { this.tipoEvaluacion = tipoEvaluacion; }
    public Date getFecIngreso() { return fecIngreso; }
    public void setFecIngreso(Date fecIngreso) { this.fecIngreso = fecIngreso; }
    public Date getFecReintegro() { return fecReintegro; }
    public void setFecReintegro(Date fecReintegro) { this.fecReintegro = fecReintegro; }
    public Date getFecRetiro() { return fecRetiro; }
    public void setFecRetiro(Date fecRetiro) { this.fecRetiro = fecRetiro; }
    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }
    public String getLateralidad() { return lateralidad; }
    public void setLateralidad(String lateralidad) { this.lateralidad = lateralidad; }
    public String getMotivoObs() { return motivoObs; }
    public void setMotivoObs(String motivoObs) { this.motivoObs = motivoObs; }
    public PacienteFormData getPaciente() { return paciente; }
    public void setPaciente(PacienteFormData paciente) { this.paciente = paciente; }
}
