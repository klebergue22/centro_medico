package ec.gob.igm.rrhh.consultorio.infra.persistence;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.pagination.LegacyOracleLimitHandler;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.query.sqm.FetchClauseType;

/**
 * Forces Hibernate to use ROWNUM pagination for Oracle versions that do not
 * support the SQL standard FETCH FIRST syntax.
 */
public class OracleLegacyDialect extends OracleDialect {

    private static final DatabaseVersion ORACLE_11G = DatabaseVersion.make(11);
    private static final LimitHandler LEGACY_LIMIT_HANDLER = new LegacyOracleLimitHandler(ORACLE_11G);

    public OracleLegacyDialect() {
        super(ORACLE_11G);
    }

    @Override
    public LimitHandler getLimitHandler() {
        return LEGACY_LIMIT_HANDLER;
    }

    @Override
    public boolean supportsFetchClause(FetchClauseType type) {
        return false;
    }
}
