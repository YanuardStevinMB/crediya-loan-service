package com.crediya.loan.model.user;
import lombok.*;
//import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String firstName;
    private String lastName;
    private String identityDocument;
    private BigDecimal baseSalary;
}
