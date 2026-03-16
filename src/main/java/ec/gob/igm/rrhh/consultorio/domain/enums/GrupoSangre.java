package ec.gob.igm.rrhh.consultorio.domain.enums;





import java.util.Arrays;

/**
 * Enum GrupoSangre: contiene la lógica de la aplicación.
 */
public enum GrupoSangre {

    O_POS("O+", "O positivo"),
    O_NEG("O-", "O negativo"),
    A_POS("A+", "A positivo"),
    A_NEG("A-", "A negativo"),
    B_POS("B+", "B positivo"),
    B_NEG("B-", "B negativo"),
    AB_POS("AB+", "AB positivo"),
    AB_NEG("AB-", "AB negativo");

    private final String codigo;
    private final String descripcion;

    GrupoSangre(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static GrupoSangre fromCodigo(String codigo) {
        if (codigo == null) return null;
        final String c = codigo.trim().toUpperCase();
        if (c.isEmpty()) return null;

        return Arrays.stream(values())
                .filter(e -> e.codigo.equals(c))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("GrupoSangre inválido: " + codigo));
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
