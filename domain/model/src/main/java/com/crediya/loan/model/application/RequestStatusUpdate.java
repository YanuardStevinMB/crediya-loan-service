package com.crediya.loan.model.application;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RequestStatusUpdate {
    private Long id;
    private Long stateId;
}
