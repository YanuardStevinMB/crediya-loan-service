package com.crediya.loan.api.controller;

import com.crediya.loan.api.applicationMapper.ApplicationMapper;
import com.crediya.loan.api.applicationMapper.ApplicationPaginedMapper;
import com.crediya.loan.api.dto.ApplicationPaginedDto;
import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.model.application.PendingApplicationsCriteria;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.usecase.generaterequest.GenerateRequestUseCase;
import com.crediya.loan.usecase.getpendingapplications.GetPendingApplicationsUseCase;
import com.crediya.loan.usecase.requeststatuschange.RequestStatusChangeUseCase;
import com.crediya.loan.usecase.shared.Messages;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@ExtendWith(MockitoExtension.class)
class ApplicationHandlerTest {

    @Mock
    private GenerateRequestUseCase generateRequestUseCase;

    @Mock
    private GetPendingApplicationsUseCase getPendingApplicationsUseCase;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ApplicationPaginedMapper applicationPaginedMapper;

    @Mock
    private RequestStatusChangeUseCase requestStatusChangeUseCase;

    @Mock
    private Validator validator;

    private WebTestClient client;

    @BeforeEach
    void setup() {
        var handler = new ApplicationHandler(
                generateRequestUseCase,
                getPendingApplicationsUseCase,
                applicationMapper,
                applicationPaginedMapper,
                requestStatusChangeUseCase,
                validator
        );

        RouterFunction<ServerResponse> router = route(
                POST("/api/v1/applications"), handler::createApplication
        ).andRoute(
                GET("/api/v1/application/pending"), handler::findApplications
        ).andRoute(
                PUT("/api/v1/applications/status"), handler::updateRequestStatus
        );

        client = WebTestClient.bindToRouterFunction(router).build();
    }

    // ---------- createApplication ----------

    @Test
    void createApplication_shouldReturn200_andIncludePath_whenValid() {
        String futureDate = LocalDate.now().plusDays(7).toString();
        String json = """
                {
                  "Id": null,
                  "amount": 1000.00,
                  "term": "%s",
                  "email": "user@mail.com",
                  "identityDocument": "123456",
                  "loanTypeId": 1
                }
                """.formatted(futureDate);

        when(validator.validate(any(ApplicationSaveDto.class))).thenReturn(Set.of());

        Application inModel = Application.builder()
                .amount(new BigDecimal("1000.00"))
                .term(LocalDate.parse(futureDate))
                .email("user@mail.com")
                .identityDocument("123456")
                .loanTypeId(1L)
                .build();
        when(applicationMapper.toModel(any(ApplicationSaveDto.class))).thenReturn(inModel);
        when(generateRequestUseCase.execute(inModel)).thenReturn(Mono.just(inModel));

        ApplicationResponseDto outDto = mock(ApplicationResponseDto.class);
        when(applicationMapper.toResponseDto(inModel)).thenReturn(outDto);

        client.post()
                .uri("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("/api/v1/applications")));

        verify(validator).validate(any(ApplicationSaveDto.class));
        verify(applicationMapper).toModel(any(ApplicationSaveDto.class));
        verify(generateRequestUseCase).execute(inModel);
        verify(applicationMapper).toResponseDto(inModel);
        verifyNoMoreInteractions(
                validator, applicationMapper, applicationPaginedMapper,
                generateRequestUseCase, getPendingApplicationsUseCase, requestStatusChangeUseCase
        );
    }

    // ---------- findApplications ----------

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findApplications_shouldReturnPagedResponse_withMappedRecords() {
        var app1 = ApplicationPagined.builder()
                .id(10L).email("a@a.com").identityDocument("CC1")
                .amount(new BigDecimal("1500.00"))
                .stateId(1L).loanTypeId(2L).fullName("Ana Diaz")
                .build();

        var app2 = ApplicationPagined.builder()
                .id(11L).email("b@b.com").identityDocument("CC2")
                .amount(new BigDecimal("2000.00"))
                .stateId(1L).loanTypeId(2L).fullName("Luis Vega")
                .build();

        com.crediya.loan.model.shared.Page<ApplicationPagined> page = mock(com.crediya.loan.model.shared.Page.class);
        when(page.page()).thenReturn(2);
        when(page.size()).thenReturn(10);
        when(page.totalElements()).thenReturn(25L);
        when(page.content()).thenReturn(List.of(app1, app2));

        when(getPendingApplicationsUseCase.execute(any(PendingApplicationsCriteria.class)))
                .thenReturn((Mono) Mono.just(page));

        var dto1 = new ApplicationPaginedDto(app1.getId(), app1.getAmount(), app1.getTerm(),
                app1.getEmail(), app1.getIdentityDocument(), app1.getState(), app1.getLoan(),
                app1.getStateId(), app1.getLoanTypeId(), app1.getFullName(), app1.getBaseSalary());
        var dto2 = new ApplicationPaginedDto(app2.getId(), app2.getAmount(), app2.getTerm(),
                app2.getEmail(), app2.getIdentityDocument(), app2.getState(), app2.getLoan(),
                app2.getStateId(), app2.getLoanTypeId(), app2.getFullName(), app2.getBaseSalary());

        when(applicationPaginedMapper.toResponseDto(app1)).thenReturn(dto1);
        when(applicationPaginedMapper.toResponseDto(app2)).thenReturn(dto2);

        client.get()
                .uri("/api/v1/application/pending")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page_number").isEqualTo(2)
                .jsonPath("$.page_size").isEqualTo(10)
                .jsonPath("$.total_record_count").isEqualTo(25)
                .jsonPath("$.records.length()").isEqualTo(2)
                .jsonPath("$.records[0].identityDocument").isEqualTo("CC1")
                .jsonPath("$.records[1].identityDocument").isEqualTo("CC2");

        ArgumentCaptor<PendingApplicationsCriteria> cap = ArgumentCaptor.forClass(PendingApplicationsCriteria.class);
        verify(getPendingApplicationsUseCase).execute(cap.capture());
        assertNotNull(cap.getValue());
        verify(applicationPaginedMapper, times(2)).toResponseDto(any(ApplicationPagined.class));
        verifyNoMoreInteractions(
                applicationPaginedMapper, getPendingApplicationsUseCase,
                applicationMapper, validator, generateRequestUseCase, requestStatusChangeUseCase
        );
    }

    // ---------- updateRequestStatus ----------
    @Test
    void updateRequestStatus_shouldReturn200_whenUseCaseSucceeds() {
        when(requestStatusChangeUseCase.execute(any(RequestStatusUpdate.class)))
                .thenReturn(Mono.just(Messages.APPLICATION_UPDATED));

        client.put()
                .uri("/api/v1/applications/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"id":1,"stateId":10}
                    """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains(Messages.APPLICATION_UPDATED));
                    assertTrue(body.contains("/api/v1/applications/status"));
                });

        verify(requestStatusChangeUseCase).execute(any(RequestStatusUpdate.class));
    }

    @Test
    void updateRequestStatus_shouldReturn400_whenUseCaseFails() {
        when(requestStatusChangeUseCase.execute(any(RequestStatusUpdate.class)))
                .thenReturn(Mono.error(new RuntimeException("invalid state")));

        client.put()
                .uri("/api/v1/applications/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"id":2,"stateId":99}
                    """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains(Messages.APPLICATION_UPDATE_ERROR));
                    assertTrue(body.contains("invalid state"));
                });

        verify(requestStatusChangeUseCase).execute(any(RequestStatusUpdate.class));
    }

}
