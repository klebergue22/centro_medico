/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.persistence.converter;

/**
 *
 * @author GUERRA_KLEBER
 */


import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SexoConverterJPA implements AttributeConverter<Sexo, String> {

    @Override
    public String convertToDatabaseColumn(Sexo attribute) {
        return attribute != null ? attribute.getCodigo() : null;
    }

    @Override
    public Sexo convertToEntityAttribute(String dbData) {
        return dbData != null ? Sexo.fromCodigo(dbData) : null;
    }
}
