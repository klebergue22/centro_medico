/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.model;

/**
 *
 * @author GUERRA_KLEBER
 */


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

import java.util.Set;

@Entity
@Table(name = "CIE10", schema = "CONSULTORIO",
        uniqueConstraints = {
            @UniqueConstraint(name = "UK_CIE10_ID", columnNames = "ID")
        }
)
public class Cie10 implements Serializable {

    private static final long serialVersionUID = 1L;

    // =========================
    // PK (negocio) -> CODIGO
    // =========================
    @Id
    @Column(name = "CODIGO", length = 10, nullable = false)
    private String codigo;

    @Column(name = "DESCRIPCION", length = 500, nullable = false)
    private String descripcion;

    // =========================
    // ID (técnico) -> UNIQUE
    // La BD lo llena con trigger + secuencia
    // =========================
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    // Columna real (útil para queries rápidas)
    @Column(name = "ID_PARENT_ID")
    private Long idParentId;

    @Column(name = "ESTADO", length = 10, nullable = false)
    private String estado;

    // =========================
    // Relación recursiva por ID
    // =========================
    /**
     * Padre (join por ID_PARENT_ID -> ID). insertable=false/updatable=false
     * porque el campo real que se persiste es idParentId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PARENT_ID", referencedColumnName = "ID", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_CIE10_PARENT"))
    private Cie10 padre;

    /**
     * Hijos: registros cuyo ID_PARENT_ID = este.ID
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PARENT_ID", referencedColumnName = "ID")
    private Set<Cie10> hijos = new HashSet<>();

    // ==========================
    // Constructores
    // ==========================
    // Constructor vacío (requerido por JPA)
    // @Builder.Default se reemplaza inicializando 'hijos' aquí
    public Cie10() {
        this.hijos = new HashSet<>();
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public Cie10(String codigo, String descripcion, Long id, Long idParentId, String estado, Cie10 padre, Set<Cie10> hijos) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.id = id;
        this.idParentId = idParentId;
        this.estado = estado;
        this.padre = padre;
        // Evitar NPE si se pasa null en la lista
        this.hijos = (hijos != null) ? hijos : new HashSet<>();
    }

    // ==========================
    // Equals y HashCode
    // Se basa solo en 'codigo' (@EqualsAndHashCode.Include)
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cie10 cie10 = (Cie10) o;
        return Objects.equals(codigo, cie10.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    // ==========================
    // ToString
    // Incluye solo 'codigo' y 'descripcion' (@ToString.Include)
    // ==========================
    @Override
    public String toString() {
        return "Cie10{"
                + "codigo='" + codigo + '\''
                + ", descripcion='" + descripcion + '\''
                + '}';
    }

    // ==========================
    // Getters y Setters
    // ==========================
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdParentId() {
        return idParentId;
    }

    public void setIdParentId(Long idParentId) {
        this.idParentId = idParentId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cie10 getPadre() {
        return padre;
    }

    public void setPadre(Cie10 padre) {
        this.padre = padre;
    }

    public Set<Cie10> getHijos() {
        return hijos;
    }

    public void setHijos(Set<Cie10> hijos) {
        this.hijos = hijos;
    }
}
