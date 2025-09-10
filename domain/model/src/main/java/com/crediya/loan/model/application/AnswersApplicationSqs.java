package com.crediya.loan.model.application;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AnswersApplicationSqs {
    private Long id;
    private  String statusCode;

}
