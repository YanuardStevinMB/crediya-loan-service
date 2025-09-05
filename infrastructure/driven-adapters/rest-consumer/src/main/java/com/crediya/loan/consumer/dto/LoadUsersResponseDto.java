package com.crediya.loan.consumer.dto;
import lombok.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LoadUsersResponseDto {

    private boolean success;
    private List<UserDto> data;

}

