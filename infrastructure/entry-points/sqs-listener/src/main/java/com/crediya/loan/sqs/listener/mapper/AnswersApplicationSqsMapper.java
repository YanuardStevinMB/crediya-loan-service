package com.crediya.loan.sqs.listener.mapper;

import com.crediya.loan.model.calculateborrowingcapacity.AnswersApplicationSqs;
import com.crediya.loan.model.calculateborrowingcapacity.Installment;
import com.crediya.loan.model.calculateborrowingcapacity.Totals;
import com.crediya.loan.sqs.listener.dto.*;

import java.util.List;
import java.util.Objects;

public final class AnswersApplicationSqsMapper {

    private AnswersApplicationSqsMapper() {}

    public static AnswersApplicationSqs toDomain(AnswersApplicationSqsDto dto) {
        if (dto == null) return null;

        List<Installment> plan = dto.getPaymentPlan() == null ? List.of()
                : dto.getPaymentPlan().stream()
                .filter(Objects::nonNull)
                .map(AnswersApplicationSqsMapper::toDomain)
                .toList();

        return AnswersApplicationSqs.builder()
                .id(dto.getId())
                .identityDocument(dto.getIdentityDocument())
                .status(dto.getStatus())
                .statusCode(dto.getStatusCode())
                .requestId(dto.getRequestId())
                .clientEmail(dto.getEmailClient())
                .cuotaNueva(dto.getCuotaNueva())
                .capacidadDisponible(dto.getCapacidadDisponible())
                .interesMensual(dto.getInteresMensual())
                .plazoMeses(dto.getPlazoMeses())
                .paymentPlan(plan)
                .totales(dto.getTotales() == null ? null : toDomain(dto.getTotales()))
                .decidedAt(dto.getDecidedAt())
                .build();
    }

    private static Installment toDomain(InstallmentDto dto) {
        return Installment.builder()
                .n(dto.getN())
                .cuota(dto.getCuota())
                .interes(dto.getInteres())
                .abonoCapital(dto.getAbonoCapital())
                .saldo(dto.getSaldo())
                .build();
    }

    private static Totals toDomain(TotalsDto dto) {
        return Totals.builder()
                .totalIntereses(dto.getTotalIntereses())
                .totalPagado(dto.getTotalPagado())
                .build();
    }
}
