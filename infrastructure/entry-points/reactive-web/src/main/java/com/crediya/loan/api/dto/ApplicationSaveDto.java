package com.crediya.loan.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApplicationSaveDto")
public record ApplicationSaveDto(
        Long Id,
        @NotNull(message = "El campo 'amount' es obligatorio.")
        BigDecimal amount,

        @NotNull(message = "El campo 'term' es obligatorio.")
        LocalDate term,

        @NotBlank(message = "El campo 'email' es obligatorio.")
        @Size(max = 150, message = "email no debe exceder 150 caracteres.")
        String email,

        @NotBlank(message = "El campo 'identityDocument' es obligatorio.")
        @Size(max = 20, message = "identityDocument no debe exceder 20 caracteres.")
        String identityDocument,

        @NotNull(message = "El campo 'loanTypeId' es obligatorio.")
        Long loanTypeId



) {}
