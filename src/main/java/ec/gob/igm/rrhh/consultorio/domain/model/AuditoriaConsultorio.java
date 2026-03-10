package ec.gob.igm.rrhh.consultorio.domain.model;





import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "AUDITORIA_CONSULTORIO", schema = "CONSULTORIO")
public class AuditoriaConsultorio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqAuditoriaConsultorio")
    @SequenceGenerator(
            name = "seqAuditoriaConsultorio",
            sequenceName = "CONSULTORIO.SQ_AUDITORIA_CONSULTORIO",
            allocationSize = 1
    )
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "MODULO", nullable = false, length = 100)
    private String modulo;

    @Column(name = "USUARIO", nullable = false, length = 50)
    private String usuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA", nullable = false)
    private Date fecha;

    @Column(name = "ACCION", nullable = false, length = 50)
    private String accion;

    @Column(name = "TABLA_AFECTA", nullable = false, length = 100)
    private String tablaAfecta;

    @Column(name = "CAMPO_AFECTA", length = 50)
    private String campoAfecta;

    @Column(name = "OBSERVACIONES", length = 1000)
    private String observaciones;

    @Column(name = "PK_VALOR", length = 100)
    private String pkValor;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTablaAfecta() {
        return tablaAfecta;
    }

    public void setTablaAfecta(String tablaAfecta) {
        this.tablaAfecta = tablaAfecta;
    }

    public String getCampoAfecta() {
        return campoAfecta;
    }

    public void setCampoAfecta(String campoAfecta) {
        this.campoAfecta = campoAfecta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getPkValor() {
        return pkValor;
    }

    public void setPkValor(String pkValor) {
        this.pkValor = pkValor;
    }
}
