package com.crediya.loan.api;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse")
public record ApiErrorResponse(
        @Schema(example = "2025-08-28T05:18:04Z") String timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Datos de entrada inválidos") String message,
        @Schema(example = "/api/v1/usuarios") String path,
        @Schema(example = "POST") String method
) {}
