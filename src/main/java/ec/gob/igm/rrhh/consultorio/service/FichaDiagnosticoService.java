package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class FichaDiagnosticoService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void eliminarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) {
            return;
        }

        em.createQuery("DELETE FROM FichaDiagnostico d WHERE d.ficha.idFicha = :id")
                .setParameter("id", idFicha)
                .executeUpdate();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public FichaDiagnostico guardar(FichaDiagnostico d) {
        assertEm();
        if (d == null) {
            return null;
        }

        // Ejemplo si tu PK es Long idFichaDiag:
        // if (d.getIdFichaDiag() == null) { em.persist(d); return d; }
        //
        // Ejemplo si tu PK es Long id:
        // if (d.getId() == null) { em.persist(d); return d; }
        //
        // Ejemplo si tu PK es @EmbeddedId:
        // if (d.getId() == null) { em.persist(d); return d; }
        // Como fallback seguro (no ideal): si NO sabes el ID, usa merge siempre
        // (esto evita error de compilación, pero lo óptimo es ajustar el ID real).
        return em.merge(d);
    }

    public List<FichaDiagnostico> listarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) {
            return Collections.emptyList();
        }

        return em.createQuery(
                "SELECT d FROM FichaDiagnostico d "
                + "WHERE d.ficha.idFicha = :id "
                + "ORDER BY d.orden",
                FichaDiagnostico.class)
                .setParameter("id", idFicha)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void guardarDiagnosticosDeFicha(Long idFicha,
            List<ConsultaDiagnostico> diagnosticos,
            Date fecha,
            String usuario) {

        assertEm();

        if (idFicha == null) {
            throw new IllegalArgumentException("idFicha no puede ser null");
        }

        // 1) Eliminar actuales
        eliminarPorFicha(idFicha);

        if (diagnosticos == null || diagnosticos.isEmpty()) {
            return;
        }

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date now = (fecha != null) ? fecha : new Date();

        // 2) Referencia a la ficha (sin cargar todo)
        FichaOcupacional fichaRef = em.getReference(FichaOcupacional.class, idFicha);

        // 3) Evitar violación de unique(ID_FICHA, COD_CIE10)
        Set<String> codigosInsertados = new HashSet<>();
        int orden = 1;

        for (ConsultaDiagnostico cd : diagnosticos) {
            if (cd == null) {
                continue;
            }

            // ✅ CORRECTO: el código viene de la relación CIE10, no de cd.getCodigo()
            String cod = extraerCodigoCie10(cd);
            if (cod == null) {
                continue;
            }

            if (!codigosInsertados.add(cod)) {
                continue; // repetido
            }

            FichaDiagnostico fd = new FichaDiagnostico();
            fd.setFicha(fichaRef);

            // Ajusta según tu entidad:
            fd.setCodCie10(cod);
            fd.setDescripcion(extraerDescripcionCie10(cd)); // si tu Cie10 tiene otra propiedad, ajusta aquí
            fd.setTipoDiag(normalizarTipo(cd.getTipoDiag())); // 'P' / 'S'
            fd.setOrden(orden++);
            fd.setEstado("A");

            fd.setFechaCreacion(now);
            fd.setUsrCreacion(usr);
            fd.setFechaActualizacion(null);
            fd.setUsrActualizacion(null);

            em.persist(fd);
        }

        em.flush();
    }

    private String extraerCodigoCie10(ConsultaDiagnostico cd) {
        if (cd.getCie10() == null) {
            return null;
        }

        // ✅ Lo más común: Cie10 tiene getCodigo() o getCodCie10()
        // Cambia SOLO esta línea si tu entidad Cie10 se llama distinto.
        String cod = cd.getCie10().getCodigo();

        return safe(cod);
    }

    private String extraerDescripcionCie10(ConsultaDiagnostico cd) {
        if (cd.getCie10() == null) {
            return null;
        }

        // ✅ Lo más común: Cie10 tiene getDescripcion()
        // Si tu entidad usa getNombre()/getDetalle(), ajusta aquí.
        String desc = cd.getCie10().getDescripcion();

        return safe(desc);
    }

    private String safe(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String normalizarTipo(String tipo) {
        String t = safe(tipo);
        if (t == null) {
            return "S";
        }
        t = t.toUpperCase();
        return ("P".equals(t) || "S".equals(t)) ? t : "S";
    }
}
