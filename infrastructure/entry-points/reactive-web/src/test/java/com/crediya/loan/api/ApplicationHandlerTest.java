package com.crediya.loan.api;

import com.crediya.loan.api.applicationMapper.ApplicationMapper;
import com.crediya.loan.api.controller.ApplicationHandler;
import com.crediya.loan.api.dto.ApiResponse;
import com.crediya.loan.model.application.Application;
import com.crediya.loan.usecase.generaterequest.GenerateRequestUseCase;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

class ApplicationHandlerTest {

    private WebTestClient client;
    private GenerateRequestUseCase generateRequestUseCase;

    @BeforeEach
    void setup() {
        generateRequestUseCase = Mockito.mock(GenerateRequestUseCase.class);
        ApplicationMapper mapper = Mappers.getMapper(ApplicationMapper.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ApplicationHandler handler = new ApplicationHandler(
                generateRequestUseCase,
                null, // GetPendingApplicationsUseCase no lo usamos aquí
                mapper,
                null, // RequestsAndUsersMapper no lo usamos aquí
                validator
        );

        RouterFunction<ServerResponse> routes = route(POST("/api/v1/solicitud"), handler::createApplication)
                .filter((req, next) -> next.handle(req).onErrorResume(ConstraintViolationException.class, e ->
                        ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(ApiResponse.fail("Validación", e.getConstraintViolations(), req.path()))
                ));

        client = WebTestClient.bindToRouterFunction(routes).build();
    }

    @Test
    void createApplication_ok() {
        Mockito.when(generateRequestUseCase.execute(any(Application.class))).thenAnswer(inv -> {
            Application in = inv.getArgument(0);
            return Mono.just(Application.builder()
                    .id(1L) // 👈 simulamos que el servidor genera el ID
                    .amount(in.getAmount())
                    .term(in.getTerm())
                    .email(in.getEmail())
                    .identityDocument(in.getIdentityDocument())
                    .stateId(7L)
                    .loanTypeId(in.getLoanTypeId())
                    .build());
        });

        // 👈 JSON de creación sin "id"
        String json = """
        {
          "amount": 5000.00,
          "term": "2026-01-31",
          "email": "john@example.com",
          "identityDocument": "123456789",
          "loanTypeId": 3
        }
        """;

        client.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Usuario creado correctamente")
                .jsonPath("$.path").isEqualTo("/api/v1/solicitud")
                .jsonPath("$.data.id").isEqualTo(1) // 👈 esperamos el id generado en el servidor
                .jsonPath("$.data.amount").isEqualTo(5000.00)
                .jsonPath("$.data.term").isArray()
                .jsonPath("$.data.term[0]").isEqualTo(2026)
                .jsonPath("$.data.term[1]").isEqualTo(1)
                .jsonPath("$.data.term[2]").isEqualTo(31)
                .jsonPath("$.data.email").isEqualTo("john@example.com")
                .jsonPath("$.data.identityDocument").isEqualTo("123456789")
                .jsonPath("$.data.stateId").isEqualTo(7)
                .jsonPath("$.data.loanTypeId").isEqualTo(3);
    }
}
