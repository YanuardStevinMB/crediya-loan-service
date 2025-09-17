package com.crediya.loan.usecase.requeststatuschange;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.model.application.RequestStatusUpdate;
import com.crediya.loan.model.application.gateways.ApplicationRepository;
import com.crediya.loan.model.application.gateways.ApplicationSenderSqs;
import com.crediya.loan.usecase.applicationupdate.ApplicationUpdateUseCase;
import com.crediya.loan.usecase.requeststatuschange.requeststatus.ValidateRequestStatusUseCase;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestStatusChangeUseCase Tests")
class RequestStatusChangeUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ValidateRequestStatusUseCase validateRequestStatusUseCase;

    @Mock
    private ApplicationSenderSqs sqsSender;

    @Mock
    private ApplicationUpdateUseCase applicationUpdateUseCase;

    private RequestStatusChangeUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RequestStatusChangeUseCase(
                applicationRepository,
                validateRequestStatusUseCase,
                sqsSender,
                applicationUpdateUseCase
        );
    }

    private RequestStatusUpdate buildRequestStatusUpdate() {
        // Assuming RequestStatusUpdate has these basic properties
        return RequestStatusUpdate.builder()
                .id(1L)
                .stateId(10L)
                .build();
    }

    private ApplicationDataCompleted buildApplicationDataCompleted() {
        // Assuming ApplicationDataCompleted has these basic properties
        return ApplicationDataCompleted.builder()
                .id(1L)
                .stateId(10L)
                .build();
    }

    @Nested
    @DisplayName("Successful Execution Tests")
    class SuccessfulExecutionTests {

        @Test
        @DisplayName("Should successfully execute status change with SQS notification")
        void shouldSuccessfullyExecuteStatusChangeWithSqsNotification() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();
            String expectedSqsMessageId = "sqs-msg-123";

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just(expectedSqsMessageId));

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(request);
            verify(sqsSender).sendStatusChange(entity);
        }

        @Test
        @DisplayName("Should execute successfully even when SQS notification fails")
        void shouldExecuteSuccessfullyEvenWhenSqsNotificationFails() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();
            RuntimeException sqsError = new RuntimeException("SQS connection failed");

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.error(sqsError));

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(request);
            verify(sqsSender).sendStatusChange(entity);
        }

        @Test
        @DisplayName("Should return APPLICATION_UPDATED message on success")
        void shouldReturnApplicationUpdatedMessageOnSuccess() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just("msg-id"));

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate error when application update fails")
        void shouldPropagateErrorWhenApplicationUpdateFails() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            RuntimeException updateError = new RuntimeException("Database connection failed");

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.error(updateError));

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectErrorMatches(throwable ->
                            throwable instanceof RuntimeException &&
                                    throwable.getMessage().equals("Database connection failed")
                    )
                    .verify();

            verify(applicationUpdateUseCase).execute(request);
            verifyNoInteractions(sqsSender);
        }

        @Test
        @DisplayName("Should handle SQS error gracefully and continue execution")
        void shouldHandleSqsErrorGracefullyAndContinueExecution() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();
            RuntimeException sqsError = new RuntimeException("SQS queue not available");

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.error(sqsError));

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(request);
            verify(sqsSender).sendStatusChange(entity);
        }

        @Test
        @DisplayName("Should handle different types of SQS errors")
        void shouldHandleDifferentTypesOfSqsErrors() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));

            // Test with different exception types
            Exception[] sqsErrors = {
                    new RuntimeException("Connection timeout"),
                    new IllegalStateException("Invalid queue state"),
                    new NullPointerException("Queue URL is null")
            };

            for (Exception sqsError : sqsErrors) {
                reset(sqsSender);
                when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.error(sqsError));

                // When & Then
                StepVerifier.create(useCase.execute(request))
                        .expectNext(Messages.APPLICATION_UPDATED)
                        .verifyComplete();

                verify(sqsSender).sendStatusChange(entity);
            }
        }
    }

    @Nested
    @DisplayName("Integration Flow Tests")
    class IntegrationFlowTests {

        @Test
        @DisplayName("Should execute complete flow in correct order")
        void shouldExecuteCompleteFlowInCorrectOrder() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();
            String sqsMessageId = "sqs-123";

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just(sqsMessageId));

            // When
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            // Then - Verify order of execution
            var inOrder = inOrder(applicationUpdateUseCase, sqsSender);
            inOrder.verify(applicationUpdateUseCase).execute(request);
            inOrder.verify(sqsSender).sendStatusChange(entity);
        }

        @Test
        @DisplayName("Should pass correct entities between use cases")
        void shouldPassCorrectEntitiesBetweenUseCases() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just("msg-id"));

            // When
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            // Then - Verify correct entities are passed
            verify(applicationUpdateUseCase).execute(eq(request));
            verify(sqsSender).sendStatusChange(eq(entity));
        }

        @Test
        @DisplayName("Should handle reactive chain correctly")
        void shouldHandleReactiveChainCorrectly() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just("msg-id"));

            // When & Then - Test reactive chain
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            // Verify both operations were called exactly once
            verify(applicationUpdateUseCase, times(1)).execute(request);
            verify(sqsSender, times(1)).sendStatusChange(entity);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null request gracefully")
        void shouldHandleNullRequestGracefully() {
            // Given
            when(applicationUpdateUseCase.execute(null)).thenReturn(Mono.error(new NullPointerException("Request cannot be null")));

            // When & Then
            StepVerifier.create(useCase.execute(null))
                    .expectError(NullPointerException.class)
                    .verify();

            verify(applicationUpdateUseCase).execute(null);
            verifyNoInteractions(sqsSender);
        }



        @Test
        @DisplayName("Should handle SQS returning empty Mono")
        void shouldHandleSqsReturningEmptyMono() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.empty());

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(request);
            verify(sqsSender).sendStatusChange(entity);
        }

        @Test
        @DisplayName("Should handle multiple successive calls")
        void shouldHandleMultipleSuccessiveCalls() {
            // Given
            RequestStatusUpdate request1 = RequestStatusUpdate.builder().id(1L).stateId(10L).build();
            RequestStatusUpdate request2 = RequestStatusUpdate.builder().id(2L).stateId(20L).build();
            ApplicationDataCompleted entity1 = ApplicationDataCompleted.builder().id(1L).stateId(10L).build();
            ApplicationDataCompleted entity2 = ApplicationDataCompleted.builder().id(2L).stateId(20L).build();

            when(applicationUpdateUseCase.execute(request1)).thenReturn(Mono.just(entity1));
            when(applicationUpdateUseCase.execute(request2)).thenReturn(Mono.just(entity2));
            when(sqsSender.sendStatusChange(entity1)).thenReturn(Mono.just("msg-1"));
            when(sqsSender.sendStatusChange(entity2)).thenReturn(Mono.just("msg-2"));

            // When & Then - First call
            StepVerifier.create(useCase.execute(request1))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            // When & Then - Second call
            StepVerifier.create(useCase.execute(request2))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(request1);
            verify(applicationUpdateUseCase).execute(request2);
            verify(sqsSender).sendStatusChange(entity1);
            verify(sqsSender).sendStatusChange(entity2);
        }
    }

    @Nested
    @DisplayName("Dependency Interaction Tests")
    class DependencyInteractionTests {

        @Test
        @DisplayName("Should not call SQS when application update fails")
        void shouldNotCallSqsWhenApplicationUpdateFails() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            RuntimeException updateError = new RuntimeException("Update failed");

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.error(updateError));

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(applicationUpdateUseCase).execute(request);
            verifyNoInteractions(sqsSender);
        }

        @Test
        @DisplayName("Should call application update exactly once")
        void shouldCallApplicationUpdateExactlyOnce() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(Mono.just("msg-id"));

            // When
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            // Then
            verify(applicationUpdateUseCase, times(1)).execute(request);
            verify(sqsSender, times(1)).sendStatusChange(entity);
        }

        @Test
        @DisplayName("Should handle delayed SQS response")
        void shouldHandleDelayedSqsResponse() {
            // Given
            RequestStatusUpdate request = buildRequestStatusUpdate();
            ApplicationDataCompleted entity = buildApplicationDataCompleted();

            when(applicationUpdateUseCase.execute(request)).thenReturn(Mono.just(entity));
            when(sqsSender.sendStatusChange(entity)).thenReturn(
                    Mono.just("delayed-msg-id").delayElement(java.time.Duration.ofMillis(100))
            );

            // When & Then
            StepVerifier.create(useCase.execute(request))
                    .expectNext(Messages.APPLICATION_UPDATED)
                    .verifyComplete();

            verify(applicationUpdateUseCase).execute(request);
            verify(sqsSender).sendStatusChange(entity);
        }
    }
}