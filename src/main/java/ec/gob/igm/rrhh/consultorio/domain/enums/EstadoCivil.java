package ec.gob.igm.rrhh.consultorio.domain.enums;





import java.util.Arrays;

/**
 * Enum EstadoCivil: contiene la lógica de la aplicación.
 */
public enum EstadoCivil {

    SOLTERO("S", "Soltero"),
    CASADO("C", "Casado"),
    DIVORCIADO("D", "Divorciado"),
    VIUDO("V", "Viudo"),
    UNION_LIBRE("U", "Unión libre"),
    NO_DEFINIDO("N", "No definido"); // ✅ tu constraint permite 'N'

    private final String codigo;
    private final String descripcion;

    EstadoCivil(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoCivil fromCodigo(String codigo) {
        if (codigo == null) return null;
        final String c = codigo.trim().toUpperCase();
        if (c.isEmpty()) return null;

        return Arrays.stream(values())
                .filter(e -> e.codigo.equals(c))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("EstadoCivil inválido: " + codigo));
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
