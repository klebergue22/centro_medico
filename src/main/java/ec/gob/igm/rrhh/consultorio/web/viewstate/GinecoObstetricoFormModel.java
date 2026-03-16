package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.Date;

public class GinecoObstetricoFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String examenReproMasculino;
    private Integer tiempoReproMasculino;
    private String ginecoExamen1;
    private String ginecoTiempo1;
    private String ginecoResultado1;
    private String ginecoExamen2;
    private String ginecoTiempo2;
    private String ginecoResultado2;
    private String ginecoObservacion;
    private Date fum;
    private Integer gestas;
    private Integer partos;
    private Integer cesareas;
    private Integer abortos;
    private String planificacion;
    private String planificacionCual;

    public String getExamenReproMasculino() { return examenReproMasculino; }
    public void setExamenReproMasculino(String examenReproMasculino) { this.examenReproMasculino = examenReproMasculino; }
    public Integer getTiempoReproMasculino() { return tiempoReproMasculino; }
    public void setTiempoReproMasculino(Integer tiempoReproMasculino) { this.tiempoReproMasculino = tiempoReproMasculino; }
    public String getGinecoExamen1() { return ginecoExamen1; }
    public void setGinecoExamen1(String ginecoExamen1) { this.ginecoExamen1 = ginecoExamen1; }
    public String getGinecoTiempo1() { return ginecoTiempo1; }
    public void setGinecoTiempo1(String ginecoTiempo1) { this.ginecoTiempo1 = ginecoTiempo1; }
    public String getGinecoResultado1() { return ginecoResultado1; }
    public void setGinecoResultado1(String ginecoResultado1) { this.ginecoResultado1 = ginecoResultado1; }
    public String getGinecoExamen2() { return ginecoExamen2; }
    public void setGinecoExamen2(String ginecoExamen2) { this.ginecoExamen2 = ginecoExamen2; }
    public String getGinecoTiempo2() { return ginecoTiempo2; }
    public void setGinecoTiempo2(String ginecoTiempo2) { this.ginecoTiempo2 = ginecoTiempo2; }
    public String getGinecoResultado2() { return ginecoResultado2; }
    public void setGinecoResultado2(String ginecoResultado2) { this.ginecoResultado2 = ginecoResultado2; }
    public String getGinecoObservacion() { return ginecoObservacion; }
    public void setGinecoObservacion(String ginecoObservacion) { this.ginecoObservacion = ginecoObservacion; }
    public Date getFum() { return fum; }
    public void setFum(Date fum) { this.fum = fum; }
    public Integer getGestas() { return gestas; }
    public void setGestas(Integer gestas) { this.gestas = gestas; }
    public Integer getPartos() { return partos; }
    public void setPartos(Integer partos) { this.partos = partos; }
    public Integer getCesareas() { return cesareas; }
    public void setCesareas(Integer cesareas) { this.cesareas = cesareas; }
    public Integer getAbortos() { return abortos; }
    public void setAbortos(Integer abortos) { this.abortos = abortos; }
    public String getPlanificacion() { return planificacion; }
    public void setPlanificacion(String planificacion) { this.planificacion = planificacion; }
    public String getPlanificacionCual() { return planificacionCual; }
    public void setPlanificacionCual(String planificacionCual) { this.planificacionCual = planificacionCual; }
}
