package ec.gob.igm.rrhh.consultorio.persistence.converter;






import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoContrato;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
/**
 * Class EstadoContratoConverter: convierte valores entre la base de datos y el modelo Java.
 */
public class EstadoContratoConverter implements AttributeConverter<EstadoContrato, String> {

    @Override
    public String convertToDatabaseColumn(EstadoContrato attribute) {
        return attribute == null ? null : attribute.getCodigo();
    }

    @Override
    public EstadoContrato convertToEntityAttribute(String dbData) {
        return EstadoContrato.fromCodigo(dbData);
    }
}
