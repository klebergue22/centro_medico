package ec.gob.igm.rrhh.consultorio.domain.enums;



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
