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
    private Date fechaAtencion;
    private String tipoEvaluacion;
    private PacienteFormData paciente = new PacienteFormData();

    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public Date getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(Date fechaAtencion) { this.fechaAtencion = fechaAtencion; }
    public String getTipoEvaluacion() { return tipoEvaluacion; }
    public void setTipoEvaluacion(String tipoEvaluacion) { this.tipoEvaluacion = tipoEvaluacion; }
    public PacienteFormData getPaciente() { return paciente; }
    public void setPaciente(PacienteFormData paciente) { this.paciente = paciente; }
}
