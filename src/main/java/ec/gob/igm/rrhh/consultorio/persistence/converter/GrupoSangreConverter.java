package ec.gob.igm.rrhh.consultorio.persistence.converter;



import ec.gob.igm.rrhh.consultorio.domain.enums.GrupoSangre;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
/**
 * Class GrupoSangreConverter: convierte valores entre la base de datos y el modelo Java.
 */
public class GrupoSangreConverter implements AttributeConverter<GrupoSangre, String> {

    @Override
    public String convertToDatabaseColumn(GrupoSangre attribute) {
        return attribute != null ? attribute.getCodigo() : null;
    }

    @Override
    public GrupoSangre convertToEntityAttribute(String dbData) {
        return dbData != null ? GrupoSangre.fromCodigo(dbData) : null;
    }
}

