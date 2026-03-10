package ec.gob.igm.rrhh.consultorio.domain.enums;



import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Códigos de RH.T_TIPOS_CONTRATO (NUMBER(3))
 *
 * @author GUERRA_KLEBER
 */
public enum TipoContrato {
    NOMBRAMIENTO(2, "Nombramiento"),
    NOMB_PROVISIONAL(12, "NOMB. PROVISIONAL"),
    OCASIONAL(20, "OCASIONAL"),
    CONTRATO(34, "PLAZO FIJO CT."),
    HONORARIOS(1, "CONTRATO"),
    CONTRATO_TAREA(40, "CONTRATO TAREA"),
    DESCONOCIDO(-1, "(desconocido)");

    private final int codigo;
    private final String descripcion;

    TipoContrato(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    private static final Map<Integer, TipoContrato> LUT = new ConcurrentHashMap<>();

    static {
        Arrays.stream(values()).forEach(e -> LUT.put(e.codigo, e));
    }

    public static TipoContrato fromCodigo(int codigo) {
        return LUT.getOrDefault(codigo, DESCONOCIDO);
    }
}
