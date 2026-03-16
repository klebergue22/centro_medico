package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.Date;

public class DiagnosticoFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date fechaEmision;
    private String aptitudSel;
    private String detalleObservaciones;
    private String recomendaciones;
    private String medicoNombre;
    private String medicoCodigo;
    private String nRealizaEvaluacion;
    private String nRelacionTrabajo;
    private String nObsRetiro;

    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }
    public String getAptitudSel() { return aptitudSel; }
    public void setAptitudSel(String aptitudSel) { this.aptitudSel = aptitudSel; }
    public String getDetalleObservaciones() { return detalleObservaciones; }
    public void setDetalleObservaciones(String detalleObservaciones) { this.detalleObservaciones = detalleObservaciones; }
    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }
    public String getMedicoNombre() { return medicoNombre; }
    public void setMedicoNombre(String medicoNombre) { this.medicoNombre = medicoNombre; }
    public String getMedicoCodigo() { return medicoCodigo; }
    public void setMedicoCodigo(String medicoCodigo) { this.medicoCodigo = medicoCodigo; }
    public String getnRealizaEvaluacion() { return nRealizaEvaluacion; }
    public void setnRealizaEvaluacion(String nRealizaEvaluacion) { this.nRealizaEvaluacion = nRealizaEvaluacion; }
    public String getnRelacionTrabajo() { return nRelacionTrabajo; }
    public void setnRelacionTrabajo(String nRelacionTrabajo) { this.nRelacionTrabajo = nRelacionTrabajo; }
    public String getnObsRetiro() { return nObsRetiro; }
    public void setnObsRetiro(String nObsRetiro) { this.nObsRetiro = nObsRetiro; }
}
