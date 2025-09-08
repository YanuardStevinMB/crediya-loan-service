package com.crediya.loan.api.dto;

import com.crediya.loan.usecase.shared.Messages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "ApplicationUpdateStateDto")
public record ApplicationUpdateStateDto(
        @NotNull(message = Messages.ID_REQUIRED_APPLICATION)
        Long id,
        @NotNull(message = Messages.ID_REQUIRED_STATE)
        Long stateId


) { }
