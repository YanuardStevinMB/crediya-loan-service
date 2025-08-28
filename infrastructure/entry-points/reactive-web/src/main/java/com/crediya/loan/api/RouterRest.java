package com.crediya.loan.api;

import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import  com.crediya.loan.api.ApiErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody; // ✅ correcto
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    method = RequestMethod.POST,
                    beanClass = ApplicationHandler.class,
                    beanMethod = "createApplication",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Generar solicitud",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = ApplicationSaveDto.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "OK",
                                            content = @Content(schema = @Schema(implementation = ApplicationResponseDto.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error de validación",
                                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno",
                                            content = @Content(schema = @Schema(implementation = com.crediya.loan.api.ApiErrorResponse.class))
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(
            ApplicationHandler handler,
            ApiErrorFilter errorFilter
    ) {
        return route(POST("/api/v1/solicitud"), handler::createApplication)
                .filter(errorFilter);
    }
}
