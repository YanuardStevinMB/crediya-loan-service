package com.crediya.loan.model.calculateborrowingcapacity;

import com.crediya.loan.model.calculateborrowingcapacity.Installment;
import com.crediya.loan.model.calculateborrowingcapacity.Totals;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswersApplicationSqs {
    private Long id;
    private String identityDocument;
    private String status;
    private String statusCode;
    private String requestId;
    private String clientEmail;

    private BigDecimal cuotaNueva;
    private BigDecimal capacidadDisponible;
    private BigDecimal interesMensual;
    private Integer plazoMeses;

    private List<Installment> paymentPlan;
    private Totals totales;

    private Instant decidedAt;
}
