package ec.gob.igm.rrhh.consultorio.persistence.converter;





import ec.gob.igm.rrhh.consultorio.domain.enums.TipoContrato;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
/**
 * Class TipoContratoConverter: convierte valores entre la base de datos y el modelo Java.
 */
public class TipoContratoConverter implements AttributeConverter<TipoContrato, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TipoContrato attribute) {
        return attribute != null ? attribute.getCodigo() : null;
    }

    @Override
    public TipoContrato convertToEntityAttribute(Integer dbData) {
        return TipoContrato.fromCodigo(dbData);
    }
}

