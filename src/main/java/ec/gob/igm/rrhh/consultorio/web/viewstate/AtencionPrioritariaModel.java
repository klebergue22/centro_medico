package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

public class AtencionPrioritariaModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean embarazada;
    private boolean discapacidad;
    private boolean catastrofica;
    private boolean lactancia;
    private boolean adultoMayor;
    private String discapTipo;
    private String discapDesc;
    private Integer discapPorc;
    private String catasDiagnostico;
    private Boolean catasCalificada;

    public boolean isEmbarazada() { return embarazada; }
    public void setEmbarazada(boolean embarazada) { this.embarazada = embarazada; }
    public boolean isDiscapacidad() { return discapacidad; }
    public void setDiscapacidad(boolean discapacidad) { this.discapacidad = discapacidad; }
    public boolean isCatastrofica() { return catastrofica; }
    public void setCatastrofica(boolean catastrofica) { this.catastrofica = catastrofica; }
    public boolean isLactancia() { return lactancia; }
    public void setLactancia(boolean lactancia) { this.lactancia = lactancia; }
    public boolean isAdultoMayor() { return adultoMayor; }
    public void setAdultoMayor(boolean adultoMayor) { this.adultoMayor = adultoMayor; }
    public String getDiscapTipo() { return discapTipo; }
    public void setDiscapTipo(String discapTipo) { this.discapTipo = discapTipo; }
    public String getDiscapDesc() { return discapDesc; }
    public void setDiscapDesc(String discapDesc) { this.discapDesc = discapDesc; }
    public Integer getDiscapPorc() { return discapPorc; }
    public void setDiscapPorc(Integer discapPorc) { this.discapPorc = discapPorc; }
    public String getCatasDiagnostico() { return catasDiagnostico; }
    public void setCatasDiagnostico(String catasDiagnostico) { this.catasDiagnostico = catasDiagnostico; }
    public Boolean getCatasCalificada() { return catasCalificada; }
    public void setCatasCalificada(Boolean catasCalificada) { this.catasCalificada = catasCalificada; }
}
