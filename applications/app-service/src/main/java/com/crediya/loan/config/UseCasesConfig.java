package com.crediya.loan.config;

import com.crediya.loan.model.states.gateways.StatesRepository;
import com.crediya.loan.security.JwtProperties;
import com.crediya.loan.security.JwtReactiveAuthenticationManager;
import com.crediya.loan.usecase.calculateborrowingcapacity.UpdateValidatedRequestUseCase;
import com.crediya.loan.usecase.requeststatuschange.RequestStatusChangeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "com.crediya.loan.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    // ===== JWT =====
    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(JwtProperties props) {
        return new JwtReactiveAuthenticationManager(props);
    }
    @Bean
    public UpdateValidatedRequestUseCase updateValidatedRequest(
            StatesRepository statesRepository,
            RequestStatusChangeUseCase requestStatusChangeUseCase
    ) {
        return new UpdateValidatedRequestUseCase(statesRepository, requestStatusChangeUseCase);
    }


}
