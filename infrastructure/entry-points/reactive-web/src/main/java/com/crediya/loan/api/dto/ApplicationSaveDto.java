package com.crediya.loan.api.dto;

import com.crediya.loan.usecase.generaterequest.shared.Messages;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApplicationSaveDto")
public record ApplicationSaveDto(
        Long Id,
        @NotNull(message = Messages.AMOUNT_REQUIRED)
        BigDecimal amount,

        @NotNull(message = Messages. TERM_REQUIRED)
        LocalDate term,

        @NotBlank(message =  Messages.EMAIL_REQUIRED )
        @Email
        String email,


        @NotBlank(message = Messages.DOC_REQUIRED)
        @Pattern(regexp = "^[0-9]+$", message = Messages.DOC_NUMERIC)
        @Size(min = 6, max = 20, message = Messages.DOC_LENGTH)
        String identityDocument,

        @NotNull(message = Messages.LOAN_TYPE_REQUIRED)
        Long loanTypeId



) {}
