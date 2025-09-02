package com.crediya.loan.config;

import com.crediya.loan.shared.security.JwtProperties;
import com.crediya.loan.shared.security.JwtReactiveAuthenticationManager;
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

    // ===== JWT (ya lo tenías) =====
    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(JwtProperties props) {
        return new JwtReactiveAuthenticationManager(props);
    }
}
