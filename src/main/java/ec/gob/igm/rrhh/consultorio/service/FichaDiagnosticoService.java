package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
/**
 * Class FichaDiagnosticoService: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class FichaDiagnosticoService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    @EJB
    private ClientIdentifierService clientIdentifierService;

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

        eliminarPorFicha(idFicha);
        if (diagnosticos == null || diagnosticos.isEmpty()) {
            return;
        }

        String usuarioResuelto = resolveUsuario(usuario);
        clientIdentifierService.apply(usuarioResuelto);
        persistDiagnosticos(idFicha, diagnosticos, resolveFecha(fecha), usuarioResuelto);
        em.flush();
    }

    private void persistDiagnosticos(Long idFicha, List<ConsultaDiagnostico> diagnosticos, Date fecha, String usuario) {
        FichaOcupacional fichaRef = em.getReference(FichaOcupacional.class, idFicha);
        Set<String> codigosInsertados = new HashSet<>();
        int orden = 1;

        for (ConsultaDiagnostico cd : diagnosticos) {
            FichaDiagnostico diagnostico = buildDiagnostico(fichaRef, cd, codigosInsertados, fecha, usuario, orden);
            if (diagnostico == null) {
                continue;
            }
            em.persist(diagnostico);
            orden++;
        }
    }

    private FichaDiagnostico buildDiagnostico(FichaOcupacional fichaRef, ConsultaDiagnostico cd,
            Set<String> codigosInsertados, Date fecha, String usuario, int orden) {
        if (cd == null) {
            return null;
        }
        String cod = extraerCodigoCie10(cd);
        if (cod == null || !codigosInsertados.add(cod)) {
            return null;
        }

        FichaDiagnostico fd = new FichaDiagnostico();
        fd.setFicha(fichaRef);
        fd.setCodCie10(cod);
        fd.setDescripcion(extraerDescripcionCie10(cd));
        fd.setTipoDiag(normalizarTipo(cd.getTipoDiag()));
        fd.setOrden(orden);
        fd.setEstado("A");
        fd.setFechaCreacion(fecha);
        fd.setUsrCreacion(usuario);
        fd.setFechaActualizacion(null);
        fd.setUsrActualizacion(null);
        return fd;
    }

    private Date resolveFecha(Date fecha) {
        return fecha != null ? fecha : new Date();
    }

    private String resolveUsuario(String usuario) {
        return (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
    }

    private String extraerCodigoCie10(ConsultaDiagnostico cd) {
        if (cd.getCie10() == null) {
            return null;
        }

        String cod = cd.getCie10().getCodigo();
        return safe(cod);
    }

    private String extraerDescripcionCie10(ConsultaDiagnostico cd) {
        if (cd.getCie10() == null) {
            return null;
        }

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
