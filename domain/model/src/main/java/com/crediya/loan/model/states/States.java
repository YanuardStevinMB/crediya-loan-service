package com.crediya.loan.model.states;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class States {
    private Long id;
    private String name;
    private String description;
    private String code;


}
