package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

public class ConsumoHabitosFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer[] consTiempoConsumoMeses;
    private Boolean[] consExConsumidor;
    private Integer[] consTiempoAbstinenciaMeses;
    private Boolean[] consNoConsume;
    private String consOtrasCual;
    private String[] afCual;
    private String[] afTiempo;
    private String[] medCual;
    private Integer[] medCant;
    private String consumoObservacion;
    private String consumoVidaCondObs;
    private String obsJ;

    public Integer[] getConsTiempoConsumoMeses() { return consTiempoConsumoMeses; }
    public void setConsTiempoConsumoMeses(Integer[] v) { this.consTiempoConsumoMeses = v; }
    public Boolean[] getConsExConsumidor() { return consExConsumidor; }
    public void setConsExConsumidor(Boolean[] v) { this.consExConsumidor = v; }
    public Integer[] getConsTiempoAbstinenciaMeses() { return consTiempoAbstinenciaMeses; }
    public void setConsTiempoAbstinenciaMeses(Integer[] v) { this.consTiempoAbstinenciaMeses = v; }
    public Boolean[] getConsNoConsume() { return consNoConsume; }
    public void setConsNoConsume(Boolean[] v) { this.consNoConsume = v; }
    public String getConsOtrasCual() { return consOtrasCual; }
    public void setConsOtrasCual(String consOtrasCual) { this.consOtrasCual = consOtrasCual; }
    public String[] getAfCual() { return afCual; }
    public void setAfCual(String[] afCual) { this.afCual = afCual; }
    public String[] getAfTiempo() { return afTiempo; }
    public void setAfTiempo(String[] afTiempo) { this.afTiempo = afTiempo; }
    public String[] getMedCual() { return medCual; }
    public void setMedCual(String[] medCual) { this.medCual = medCual; }
    public Integer[] getMedCant() { return medCant; }
    public void setMedCant(Integer[] medCant) { this.medCant = medCant; }
    public String getConsumoObservacion() { return consumoObservacion; }
    public void setConsumoObservacion(String consumoObservacion) { this.consumoObservacion = consumoObservacion; }
    public String getConsumoVidaCondObs() { return consumoVidaCondObs; }
    public void setConsumoVidaCondObs(String consumoVidaCondObs) { this.consumoVidaCondObs = consumoVidaCondObs; }
    public String getObsJ() { return obsJ; }
    public void setObsJ(String obsJ) { this.obsJ = obsJ; }
}
