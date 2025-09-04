package com.crediya.loan.api;

import com.crediya.loan.api.controller.ApplicationHandler;
import com.crediya.loan.api.dto.ApplicationPaginedDto;
import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.usecase.shared.PagindData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
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
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud/pending",
                    method = RequestMethod.GET,
                    beanClass = ApplicationHandler.class,
                    beanMethod = "findApplications",
                    operation = @Operation(
                            operationId = "listPendingApplications",
                            summary = "Listar solicitudes paginadas",
                            parameters = {
                                    @Parameter(name = PagindData.PAGINED_PAGE, description = "Número de página (1-based)", example = "1"),
                                    @Parameter(name = PagindData.PAGINED_SIZE, description = "Tamaño de página", example = "10"),
                                    @Parameter(name = PagindData.PAGINED_STATE, description = "Estado de la solicitud", example = "APROBADO"),
                                    @Parameter(name = PagindData.PAGINED_DOCUMENT, description = "Documento del solicitante", example = "12345678"),
                                    @Parameter(name = PagindData.PAGINED_EMAIL, description = "Email o búsqueda parcial", example = "gmail")
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "OK",
                                            content = @Content(schema = @Schema(implementation = ApplicationPaginedDto.class))
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
                .andRoute(GET("/api/v1/solicitud/pending"), handler::findApplications)
                .filter(errorFilter);
    }
}
