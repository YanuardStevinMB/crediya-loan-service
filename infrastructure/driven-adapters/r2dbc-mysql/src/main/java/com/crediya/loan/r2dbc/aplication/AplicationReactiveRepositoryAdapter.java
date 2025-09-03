package com.crediya.loan.r2dbc.aplication;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.requestsandusers.RequestsAndUsers;
import com.crediya.loan.model.shared.Page;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import com.crediya.loan.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.loan.r2dbc.mapper.AplicationEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class AplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        Long,
        AplicationReactiveRepository
        > implements ApplicationRepository {

    private final AplicationEntityMapper aplicationEntityMapper;
    private final AplicationReactiveRepository repository;
    private final DatabaseClient db;

    private  final Long PENDING_STATE_ID = 1L;


    public AplicationReactiveRepositoryAdapter(AplicationReactiveRepository repository,
                                               AplicationEntityMapper aplicationEntityMapper,
                                               ObjectMapper mapper,
                                               DatabaseClient db) {
        super(repository, mapper, entity -> mapper.map(entity, Application.class));
        this.aplicationEntityMapper = aplicationEntityMapper;
        this.repository = repository;
        this.db = db;
    }

    @Override
    public Mono<Application> save(Application application) {
        // 1) Mapear el dominio a entidad
        ApplicationEntity entity = aplicationEntityMapper.toEntity(application);

        // 2) Guardar en BD
        return repository.save(entity)
                // 3) Mapear de vuelta a dominio
                .map(aplicationEntityMapper::toDomain)
                .doOnSuccess(saved -> log.info("[application.save] id={} email={} stateId={}",
                        saved.getId(), saved.getEmail(), saved.getStateId()))
                .doOnError(err -> log.warn("[application.save] failed: {}", err.toString()));
    }

    @Override
    public Mono<com.crediya.loan.model.shared.Page<ApplicationPagined>> findPending(PendingApplicationsCriteria c) {

        // 1) FROM + JOINs (en COUNT y DATA)
        String fromJoins = """
        FROM solicitud s
        INNER JOIN tipo_prestamo tp ON tp.id_tipo_prestamo = s.id_tipo_prestamo
        INNER JOIN estados       st ON st.id_estado        = s.id_estado
    """;

        // 2) WHERE con alias SIEMPRE
        var where  = new StringBuilder(" WHERE 1=1 ");
        var params = new java.util.LinkedHashMap<String, Object>();

        // stateId: si viene en criterio, úsalo; si no, usa PENDING_STATE_ID
        Long stateIdToUse = (c.stateId() != null) ? c.stateId() : PENDING_STATE_ID;
        where.append(" AND s.id_estado = :stateId ");
        params.put("stateId", stateIdToUse);

        if (c.filter() != null && !c.filter().isBlank()) {
            where.append(" AND (LOWER(s.email) LIKE :f OR s.documento_identidad LIKE :f) ");
            params.put("f", "%" + c.filter().toLowerCase() + "%");
        }
        if (c.loanTypeId() != null) {
            where.append(" AND s.id_tipo_prestamo = :loanTypeId ");
            params.put("loanTypeId", c.loanTypeId());
        }

        // 3) SQL COUNT + DATA (mismos JOINs y WHERE)
        String countSql = "SELECT COUNT(*) AS total " + fromJoins + where;

        String dataSql  = """
        SELECT
            s.id_solicitud         AS id_solicitud,
            s.monto                AS monto,
            s.plazo                AS plazo,
            s.email                AS email,
            s.documento_identidad  AS documento_identidad,
            s.id_estado            AS id_estado,
            s.id_tipo_prestamo     AS id_tipo_prestamo,
            tp.nombre              AS loan_type_name,
            st.nombre              AS state_name
    """ + fromJoins + where + " ORDER BY s.id_solicitud DESC LIMIT :limit OFFSET :offset";

        // 4) Paginación
        int page = Math.max(0, c.page());
        int size = Math.max(1, c.size());
        params.put("limit",  Integer.valueOf(size));
        params.put("offset", Integer.valueOf(page * size));

        // 5) COUNT (sin limit/offset)
        var countSpec = db.sql(countSql);
        for (var e : params.entrySet()) {
            if ("limit".equals(e.getKey()) || "offset".equals(e.getKey())) continue;
            countSpec = countSpec.bind(e.getKey(), e.getValue());
        }
        Mono<Long> total = countSpec
                .map((row, md) -> {
                    Long l = row.get("total", Long.class);
                    if (l != null) return l;
                    Integer i = row.get("total", Integer.class);
                    if (i != null) return i.longValue();
                    java.math.BigInteger bi = row.get("total", java.math.BigInteger.class);
                    if (bi != null) return bi.longValue();
                    return 0L;
                })
                .one();

        // 6) PAGE
        var dataSpec = db.sql(dataSql);
        for (var e : params.entrySet()) dataSpec = dataSpec.bind(e.getKey(), e.getValue());

        Mono<java.util.List<ApplicationPagined>> items = dataSpec
                .map((row, md) -> mapRow(row))   // mapea por alias
                .all()
                .collectList();

        return Mono.zip(total, items)
                .map(t -> new com.crediya.loan.model.shared.Page<>(
                        t.getT2(), t.getT1(), page, size
                ));
    }

    // 7) Mapper correcto: usa los ALIAS del SELECT, no "s.columna"
    private ApplicationPagined mapRow(io.r2dbc.spi.Row row) {
        var e = new ApplicationPagined();
        e.setId(row.get("id_solicitud", Long.class));
        e.setAmount(row.get("monto", java.math.BigDecimal.class));
        e.setTerm(row.get("plazo", java.time.LocalDate.class)); // ajusta si no es DATE
        e.setEmail(row.get("email", String.class));
        e.setIdentityDocument(row.get("documento_identidad", String.class));
        e.setStateId(row.get("id_estado", Long.class));
        e.setLoanTypeId(row.get("id_tipo_prestamo", Long.class));
        e.setStateName(row.get("state_name", String.class));
        // OJO: tu modelo usa 'typeLoad' (no 'typeLoan'); respeta el nombre real:
        e.setTypeLoan(row.get("loan_type_name", String.class));
        return e;
    }



}
