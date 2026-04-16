package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;

import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;

/**
 * Class FichaPdfMappedData: orquesta la lógica de presentación y flujo web.
 */
public class FichaPdfMappedData implements Serializable {
    public String institucion;
    public String ruc;
    public String centroTrabajo;
    public String ciiu;
    public String noHistoria;
    public String noArchivo;
    public String motivoObs;
    public String ginecoExamen1;
    public String ginecoTiempo1;
    public String ginecoResultado1;
    public String ginecoExamen2;
    public String ginecoTiempo2;
    public String ginecoResultado2;
    public String ginecoObservacion;
    public String enfermedadActual;
    public String apellido1;
    public String apellido2;
    public String nombre1;
    public String nombre2;
    public String sexo;
    public String grupoSanguineo;
    public Integer edad;
    public Date fechaNacimiento;
    public PersonaAux personaAux;
}
