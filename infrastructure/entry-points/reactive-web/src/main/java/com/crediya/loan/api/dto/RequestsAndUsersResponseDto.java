package com.crediya.loan.api.dto;


import com.crediya.loan.model.requestsandusers.RequestsAndUsers;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

public class RequestsAndUsersResponseDto extends ApplicationResponseDto{
    private String fullName;
    private BigDecimal baseSalary;
    private String stateName;
    private String typeLoan;




}
