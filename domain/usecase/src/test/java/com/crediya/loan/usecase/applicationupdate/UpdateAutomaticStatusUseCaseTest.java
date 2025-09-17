package com.crediya.loan.usecase.applicationupdate;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.BorrowingCapacitySender;
import com.crediya.loan.model.calculateborrowingcapacity.AnswersApplicationSqs;
import com.crediya.loan.model.states.States;
import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.usecase.applicationupdate.ApplicationUpdateUseCase;
import com.crediya.loan.usecase.calculateborrowingcapacity.UpdateAutomaticStatusUseCase;
import com.crediya.loan.usecase.shared.ConfigurationException;
import com.crediya.loan.usecase.shared.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateAutomaticStatusUseCase Tests")
class UpdateAutomaticStatusUseCaseTest {

    @Mock
    private StatesRepository statesRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private BorrowingCapacitySender answerSender;

    @Mock
    private ApplicationUpdateUseCase applicationUpdateUseCase;

    private UpdateAutomaticStatusUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new UpdateAutomaticStatusUseCase(
                statesRepository,
                applicationRepository,
                answerSender,
                applicationUpdateUseCase
        );
    }

    private AnswersApplicationSqs buildAnswersApplicationSqs() {
        return AnswersApplicationSqs.builder()
                .id(1L)
                .statusCode("APROB")
                .build();
    }

    private States buildState() {
        return States.builder()
                .id(10L)
                .code("APROB")
                .description("APROBADO")
                .build();
    }

    private ApplicationDataCompleted buildApplicationDataCompleted() {
        return ApplicationDataCompleted.builder()
                .id(1L)
                .stateId(10L)
                .amount(BigDecimal.valueOf(5000))
                .email("test@example.com")
                .identityDocument("12345678")
                .build();
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when message ID is null")
        void shouldThrowIllegalArgumentExceptionWhenMessageIdIsNull() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(null)
                    .statusCode("APROB")
                    .build();

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectErrorMatches(throwable ->
                            throwable instanceof IllegalArgumentException &&
                                    throwable.getMessage().equals(Messages.ID_REQUIRED_APPLICATION_AND_ID_REQUIRED_STATE)
                    )
                    .verify();

            verifyNoInteractions(statesRepository);
            verifyNoInteractions(applicationUpdateUseCase);
            verifyNoInteractions(answerSender);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when status code is null")
        void shouldThrowIllegalArgumentExceptionWhenStatusCodeIsNull() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(1L)
                    .statusCode(null)
                    .build();

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectErrorMatches(throwable ->
                            throwable instanceof IllegalArgumentException &&
                                    throwable.getMessage().equals(Messages.ID_REQUIRED_APPLICATION_AND_ID_REQUIRED_STATE)
                    )
                    .verify();

            verifyNoInteractions(statesRepository);
            verifyNoInteractions(applicationUpdateUseCase);
            verifyNoInteractions(answerSender);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when both ID and status code are null")
        void shouldThrowIllegalArgumentExceptionWhenBothIdAndStatusCodeAreNull() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(null)
                    .statusCode(null)
                    .build();

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectErrorMatches(throwable ->
                            throwable instanceof IllegalArgumentException &&
                                    throwable.getMessage().equals(Messages.ID_REQUIRED_APPLICATION_AND_ID_REQUIRED_STATE)
                    )
                    .verify();

            verifyNoInteractions(statesRepository);
            verifyNoInteractions(applicationUpdateUseCase);
            verifyNoInteractions(answerSender);
        }

        @Test
        @DisplayName("Should accept valid message with all required fields")
        void shouldAcceptValidMessageWithAllRequiredFields() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg-id"));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            verify(statesRepository).findByCode("APROB");
            verify(applicationUpdateUseCase).execute(any(RequestStatusUpdate.class));
            verify(answerSender).sendRequestNotification(msg);
        }
    }

    @Nested
    @DisplayName("Successful Execution Tests")
    class SuccessfulExecutionTests {

        @Test
        @DisplayName("Should successfully execute complete flow")
        void shouldSuccessfullyExecuteCompleteFlow() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg-123"));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            verify(statesRepository).findByCode("APROB");
            verify(applicationUpdateUseCase).execute(argThat(update ->
                    update.getId().equals(1L) && update.getStateId().equals(10L)
            ));
            verify(answerSender).sendRequestNotification(msg);
        }

        @Test
        @DisplayName("Should create correct RequestStatusUpdate object")
        void shouldCreateCorrectRequestStatusUpdateObject() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(123L)
                    .statusCode("RECH")
                    .build();

            States state = States.builder()
                    .id(20L)
                    .code("RECH")
                    .description("RECHAZADO")
                    .build();

            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("RECH")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg-456"));

            // When
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            // Then
            verify(applicationUpdateUseCase).execute(argThat(update ->
                    update.getId().equals(123L) && update.getStateId().equals(20L)
            ));
        }

        @Test
        @DisplayName("Should handle different status codes correctly")
        void shouldHandleDifferentStatusCodesCorrectly() {
            // Given
            String[] statusCodes = {"APROB", "RECH", "PEN"};
            Long[] stateIds = {10L, 20L, 30L};

            for (int i = 0; i < statusCodes.length; i++) {
                String statusCode = statusCodes[i];
                Long stateId = stateIds[i];

                AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                        .id(1L)
                        .statusCode(statusCode)
                        .build();

                States state = States.builder()
                        .id(stateId)
                        .code(statusCode)
                        .build();

                ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

                when(statesRepository.findByCode(statusCode)).thenReturn(Mono.just(state));
                when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
                when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg"));

                // When & Then
                StepVerifier.create(useCase.execute(msg))
                        .verifyComplete();

                verify(statesRepository).findByCode(statusCode);
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw ConfigurationException when state not found")
        void shouldThrowConfigurationExceptionWhenStateNotFound() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectErrorMatches(throwable ->
                            throwable instanceof ConfigurationException &&
                                    throwable.getMessage().contains("APROB")
                    )
                    .verify();

            verify(statesRepository).findByCode("APROB");
            verifyNoInteractions(applicationUpdateUseCase);
            verifyNoInteractions(answerSender);
        }

        @Test
        @DisplayName("Should propagate error when application update fails")
        void shouldPropagateErrorWhenApplicationUpdateFails() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            States state = buildState();
            RuntimeException updateError = new RuntimeException("Database connection failed");

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.error(updateError));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                                    throwable.getMessage().equals("Database connection failed")
                    )
                    .verify();

            verify(statesRepository).findByCode("APROB");
            verify(applicationUpdateUseCase).execute(any(RequestStatusUpdate.class));
            verifyNoInteractions(answerSender);
        }

        @Test
        @DisplayName("Should continue execution when SQS notification fails")
        void shouldContinueExecutionWhenSqsNotificationFails() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();
            RuntimeException sqsError = new RuntimeException("SQS connection failed");

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.error(sqsError));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete(); // Should complete successfully despite SQS error

            verify(statesRepository).findByCode("APROB");
            verify(applicationUpdateUseCase).execute(any(RequestStatusUpdate.class));
            verify(answerSender).sendRequestNotification(msg);
        }

        @Test
        @DisplayName("Should handle states repository error")
        void shouldHandleStatesRepositoryError() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            RuntimeException repoError = new RuntimeException("States repository connection failed");

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.error(repoError));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                                    throwable.getMessage().equals("States repository connection failed")
                    )
                    .verify();

            verify(statesRepository).findByCode("APROB");
            verifyNoInteractions(applicationUpdateUseCase);
            verifyNoInteractions(answerSender);
        }
    }

    @Nested
    @DisplayName("Integration Flow Tests")
    class IntegrationFlowTests {

        @Test
        @DisplayName("Should execute operations in correct order")
        void shouldExecuteOperationsInCorrectOrder() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg"));

            // When
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            // Then - Verify execution order
            var inOrder = inOrder(statesRepository, applicationUpdateUseCase, answerSender);
            inOrder.verify(statesRepository).findByCode("APROB");
            inOrder.verify(applicationUpdateUseCase).execute(any(RequestStatusUpdate.class));
            inOrder.verify(answerSender).sendRequestNotification(msg);
        }

        @Test
        @DisplayName("Should not proceed to next step when previous step fails")
        void shouldNotProceedToNextStepWhenPreviousStepFails() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.empty()); // This will cause ConfigurationException

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectError(ConfigurationException.class)
                    .verify();

            verify(statesRepository).findByCode("APROB");
            // These should not be called due to early failure
            verifyNoInteractions(applicationUpdateUseCase);
            verifyNoInteractions(answerSender);
        }

        @Test
        @DisplayName("Should pass correct data between operations")
        void shouldPassCorrectDataBetweenOperations() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(999L)
                    .statusCode("APROB")
                    .build();

            States state = States.builder()
                    .id(100L)
                    .code("APROB")
                    .build();

            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg"));

            // When
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            // Then - Verify correct data is passed
            verify(statesRepository).findByCode("APROB");
            verify(applicationUpdateUseCase).execute(argThat(update ->
                    update.getId().equals(999L) && update.getStateId().equals(100L)
            ));
            verify(answerSender).sendRequestNotification(eq(msg));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string status code")
        void shouldHandleEmptyStringStatusCode() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(1L)
                    .statusCode("")
                    .build();

            when(statesRepository.findByCode("")).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectError(ConfigurationException.class)
                    .verify();

            verify(statesRepository).findByCode("");
        }

        @Test
        @DisplayName("Should handle zero ID")
        void shouldHandleZeroId() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(0L)
                    .statusCode("APROB")
                    .build();

            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg"));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(argThat(update ->
                    update.getId().equals(0L)
            ));
        }

        @Test
        @DisplayName("Should handle negative ID")
        void shouldHandleNegativeId() {
            // Given
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(-1L)
                    .statusCode("APROB")
                    .build();

            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.just("sqs-msg"));

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(argThat(update ->
                    update.getId().equals(-1L)
            ));
        }

        @Test
        @DisplayName("Should handle SQS returning empty Mono")
        void shouldHandleSqsReturningEmptyMono() {
            // Given
            AnswersApplicationSqs msg = buildAnswersApplicationSqs();
            States state = buildState();
            ApplicationDataCompleted completedApp = buildApplicationDataCompleted();

            when(statesRepository.findByCode("APROB")).thenReturn(Mono.just(state));
            when(applicationUpdateUseCase.execute(any(RequestStatusUpdate.class))).thenReturn(Mono.just(completedApp));
            when(answerSender.sendRequestNotification(msg)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .verifyComplete();

            verify(statesRepository).findByCode("APROB");
            verify(applicationUpdateUseCase).execute(any(RequestStatusUpdate.class));
            verify(answerSender).sendRequestNotification(msg);
        }

        @Test
        @DisplayName("Should handle very long status codes")
        void shouldHandleVeryLongStatusCodes() {
            // Given
            String longStatusCode = "A".repeat(1000);
            AnswersApplicationSqs msg = AnswersApplicationSqs.builder()
                    .id(1L)
                    .statusCode(longStatusCode)
                    .build();

            when(statesRepository.findByCode(longStatusCode)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(msg))
                    .expectError(ConfigurationException.class)
                    .verify();

            verify(statesRepository).findByCode(longStatusCode);
        }
    }
}