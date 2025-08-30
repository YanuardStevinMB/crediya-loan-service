package com.crediya.loan.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.crediya.loan.model.loantype.gateways.LoanTypeRepository;
import  com.crediya.loan.model.application.gateways.ApplicationRepository;
import   com.crediya.loan.model.states.gateways.StatesRepository;
import static org.junit.jupiter.api.Assertions.*;

public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            // Mejor: afirmar por tipo concreto (ejemplo con GenerateRequestUseCase)
            // Si tienes varios, puedes repetir o verificar sufijo del nombre.
            Object useCase = context.getBean("generateRequestUseCase"); // o por clase
            assertNotNull(useCase, "No se creó el bean generateRequestUseCase");

            // Alternativamente, tu verificación por sufijo:
            boolean found = false;
            for (String name : context.getBeanDefinitionNames()) {
                if (name.endsWith("UseCase")) { found = true; break; }
            }
            assertTrue(found, "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class) // tu configuración real que crea los use cases
    static class TestConfig {

        // === Mocks requeridos por tus use cases ===
        @Bean
        ApplicationRepository applicationRepository() {
            return Mockito.mock(ApplicationRepository.class);
        }

        @Bean
        StatesRepository stateRepository() {
            return Mockito.mock(StatesRepository.class);
        }

        @Bean
        LoanTypeRepository loanTypeRepository() {
            return Mockito.mock(LoanTypeRepository.class);
        }

        // Si hay más gateways en el constructor de tus UseCase, móquealos aquí igual.
    }
}
