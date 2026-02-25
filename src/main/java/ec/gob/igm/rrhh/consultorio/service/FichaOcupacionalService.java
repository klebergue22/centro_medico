package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless

public class FichaOcupacionalService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public FichaOcupacional guardar(FichaOcupacional f) {
        normalizarNoNulos(f);
        if (f.getIdFicha() == null) {
            em.persist(f);
            return f;
        } else {
            return em.merge(f);
        }
    }

    public FichaOcupacional actualizar(FichaOcupacional f) {
        normalizarNoNulos(f);
        return em.merge(f);
    }

    /**
     * Oracle trata "" (cadena vacía) como NULL.
     * Varias columnas EXF_* están definidas NOT NULL en la tabla, por lo que si
     * la UI no marca nada o llega vacío, el merge/persist falla con ORA-01407.
     *
     * Por convención, guardamos "N" cuando no hay selección.
     */
    private void normalizarNoNulos(FichaOcupacional f) {
        if (f == null) return;

        f.setExfPielCicatrices(snNoNull(f.getExfPielCicatrices()));
        f.setExfOjosParpados(snNoNull(f.getExfOjosParpados()));
        f.setExfOjosConjuntivas(snNoNull(f.getExfOjosConjuntivas()));
        f.setExfOjosPupilas(snNoNull(f.getExfOjosPupilas()));
        f.setExfOjosCornea(snNoNull(f.getExfOjosCornea()));
        f.setExfOjosMotilidad(snNoNull(f.getExfOjosMotilidad()));
        f.setExfOidoConducto(snNoNull(f.getExfOidoConducto()));
        f.setExfOidoPabellon(snNoNull(f.getExfOidoPabellon()));
        f.setExfOidoTimpanos(snNoNull(f.getExfOidoTimpanos()));
        f.setExfOroLabios(snNoNull(f.getExfOroLabios()));
        f.setExfOroLengua(snNoNull(f.getExfOroLengua()));
        f.setExfOroFaringe(snNoNull(f.getExfOroFaringe()));
        f.setExfOroAmigdalas(snNoNull(f.getExfOroAmigdalas()));
        f.setExfOroDentadura(snNoNull(f.getExfOroDentadura()));
        f.setExfNarizTabique(snNoNull(f.getExfNarizTabique()));
        f.setExfNarizCornetes(snNoNull(f.getExfNarizCornetes()));
        f.setExfNarizMucosas(snNoNull(f.getExfNarizMucosas()));
        f.setExfNarizSenosParanasa(snNoNull(f.getExfNarizSenosParanasa()));
        f.setExfCuelloTiroidesMasas(snNoNull(f.getExfCuelloTiroidesMasas()));
        f.setExfCuelloMovilidad(snNoNull(f.getExfCuelloMovilidad()));
        f.setExfToraxMamas(snNoNull(f.getExfToraxMamas()));
        f.setExfToraxPulmones(snNoNull(f.getExfToraxPulmones()));
        f.setExfToraxCorazon(snNoNull(f.getExfToraxCorazon()));
        f.setExfToraxParrillaCostal(snNoNull(f.getExfToraxParrillaCostal()));
        f.setExfAbdVisceras(snNoNull(f.getExfAbdVisceras()));
        f.setExfAbdParedAbdominal(snNoNull(f.getExfAbdParedAbdominal()));
        f.setExfColFlexibilidad(snNoNull(f.getExfColFlexibilidad()));
        f.setExfColDesviacion(snNoNull(f.getExfColDesviacion()));
        f.setExfColDolor(snNoNull(f.getExfColDolor()));
        f.setExfPelvisPelvis(snNoNull(f.getExfPelvisPelvis()));
        f.setExfPelvisGenitales(snNoNull(f.getExfPelvisGenitales()));
        f.setExfExtVascular(snNoNull(f.getExfExtVascular()));
        f.setExfExtMiembrosSup(snNoNull(f.getExfExtMiembrosSup()));
        f.setExfExtMiembrosInf(snNoNull(f.getExfExtMiembrosInf()));
        f.setExfNeuroFuerza(snNoNull(f.getExfNeuroFuerza()));
        f.setExfNeuroSensibilidad(snNoNull(f.getExfNeuroSensibilidad()));
        f.setExfNeuroMarcha(snNoNull(f.getExfNeuroMarcha()));
        f.setExfNeuroReflejos(snNoNull(f.getExfNeuroReflejos()));

        // Regla: si PLANIFICACION != "SI", no debe guardar "PLANIFICACION_CUAL"
        if (!"SI".equalsIgnoreCase(safeTrim(f.getPlanificacion()))) {
            f.setPlanificacionCual(null);
        }
    }

    private String snNoNull(String v) {
        if (v == null) return "N";
        String t = v.trim();
        return t.isEmpty() ? "N" : t;
    }

    private String safeTrim(String v) {
        return v == null ? null : v.trim();
    }
}
