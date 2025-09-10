package com.crediya.loan.sqs.listener.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswersApplicationSqsDto {
    private Long id;
    private  String code;
}
