/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.enums;

/**
 *
 * @author GUERRA_KLEBER
 */


public enum EstadoContrato {

    VIGENTE("V", "Vigente"),
    CESADO("C", "Cesado"),
    PENDIENTE("P", "Pendiente"),
    SUSPENDIDO("S", "Suspendido");

    private final String codigo;
    private final String descripcion;

    EstadoContrato(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoContrato fromCodigo(String codigo) {
        if (codigo == null) return null;
        String c = codigo.trim().toUpperCase();
        for (EstadoContrato e : values()) {
            if (e.codigo.equals(c)) return e;
        }
        throw new IllegalArgumentException("Código de EstadoContrato desconocido: " + codigo);
    }
}
