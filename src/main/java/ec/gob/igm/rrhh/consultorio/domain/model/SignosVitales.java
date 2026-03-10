package ec.gob.igm.rrhh.consultorio.domain.model;





import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "SIGNOS_VITALES", schema = "CONSULTORIO")
public class SignosVitales implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
        name = "SIGNOS_GEN",
        sequenceName = "CONSULTORIO.SQ_SIGNOS",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SIGNOS_GEN")
    @Column(name = "ID_SIGNOS", nullable = false)
    private Long idSignos;

    @Column(name = "TEMPERATURA_C", precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "PA_SISTOLICA")
    private Integer paSistolica;

    @Column(name = "PA_DIASTOLICA")
    private Integer paDiastolica;

    @Column(name = "FRECUENCIA_CARD")
    private Integer frecuenciaCard;

    @Column(name = "FRECUENCIA_RESP")
    private Integer frecuenciaResp;

    @Column(name = "SAT_O2")
    private Integer satO2;

    @Column(name = "PESO_KG", precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "TALLA_M", precision = 4, scale = 2)
    private BigDecimal tallaM;

    @Column(name = "IMC", precision = 6, scale = 2, insertable = false, updatable = false)
    private BigDecimal imc; // virtual

    @Column(name = "PERIMETRO_ABD_CM", precision = 5, scale = 1)
    private BigDecimal perimetroAbdCm;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fechaActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;


    // Constructor vacío (requerido por JPA)
    public SignosVitales() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public SignosVitales(Long idSignos, BigDecimal temperaturaC, Integer paSistolica,
                           Integer paDiastolica, Integer frecuenciaCard, Integer frecuenciaResp,
                           Integer satO2, BigDecimal pesoKg, BigDecimal tallaM, BigDecimal imc,
                           BigDecimal perimetroAbdCm, Date fechaCreacion, String usrCreacion,
                           Date fechaActualizacion, String usrActualizacion) {
        this.idSignos = idSignos;
        this.temperaturaC = temperaturaC;
        this.paSistolica = paSistolica;
        this.paDiastolica = paDiastolica;
        this.frecuenciaCard = frecuenciaCard;
        this.frecuenciaResp = frecuenciaResp;
        this.satO2 = satO2;
        this.pesoKg = pesoKg;
        this.tallaM = tallaM;
        this.imc = imc;
        this.perimetroAbdCm = perimetroAbdCm;
        this.fechaCreacion = fechaCreacion;
        this.usrCreacion = usrCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignosVitales that = (SignosVitales) o;
        return Objects.equals(idSignos, that.idSignos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSignos);
    }

    @Override
    public String toString() {
        return "SignosVitales{" +
                "idSignos=" + idSignos +
                ", temperaturaC=" + temperaturaC +
                ", paSistolica=" + paSistolica +
                ", paDiastolica=" + paDiastolica +
                ", frecuenciaCard=" + frecuenciaCard +
                ", frecuenciaResp=" + frecuenciaResp +
                ", satO2=" + satO2 +
                ", pesoKg=" + pesoKg +
                ", tallaM=" + tallaM +
                ", imc=" + imc +
                ", perimetroAbdCm=" + perimetroAbdCm +
                ", fechaCreacion=" + fechaCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    public Long getIdSignos() {
        return idSignos;
    }

    public void setIdSignos(Long idSignos) {
        this.idSignos = idSignos;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(BigDecimal temperaturaC) {
        this.temperaturaC = temperaturaC;
    }

    public Integer getPaSistolica() {
        return paSistolica;
    }

    public void setPaSistolica(Integer paSistolica) {
        this.paSistolica = paSistolica;
    }

    public Integer getPaDiastolica() {
        return paDiastolica;
    }

    public void setPaDiastolica(Integer paDiastolica) {
        this.paDiastolica = paDiastolica;
    }

    public Integer getFrecuenciaCard() {
        return frecuenciaCard;
    }

    public void setFrecuenciaCard(Integer frecuenciaCard) {
        this.frecuenciaCard = frecuenciaCard;
    }

    public Integer getFrecuenciaResp() {
        return frecuenciaResp;
    }

    public void setFrecuenciaResp(Integer frecuenciaResp) {
        this.frecuenciaResp = frecuenciaResp;
    }

    public Integer getSatO2() {
        return satO2;
    }

    public void setSatO2(Integer satO2) {
        this.satO2 = satO2;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getTallaM() {
        return tallaM;
    }

    public void setTallaM(BigDecimal tallaM) {
        this.tallaM = tallaM;
    }

    public BigDecimal getImc() {
        return imc;
    }

    public void setImc(BigDecimal imc) {
        this.imc = imc;
    }

    public BigDecimal getPerimetroAbdCm() {
        return perimetroAbdCm;
    }

    public void setPerimetroAbdCm(BigDecimal perimetroAbdCm) {
        this.perimetroAbdCm = perimetroAbdCm;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsrCreacion() {
        return usrCreacion;
    }

    public void setUsrCreacion(String usrCreacion) {
        this.usrCreacion = usrCreacion;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getUsrActualizacion() {
        return usrActualizacion;
    }

    public void setUsrActualizacion(String usrActualizacion) {
        this.usrActualizacion = usrActualizacion;
    }
}
