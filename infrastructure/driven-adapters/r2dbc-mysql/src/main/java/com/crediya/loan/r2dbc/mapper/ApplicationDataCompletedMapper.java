package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.application.ApplicationDataCompleted;
import com.crediya.loan.r2dbc.dto.ApplicationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationDataCompletedMapper {

    // Domain -> DTO
    ApplicationDto toDto(ApplicationDataCompleted source);

    // DTO -> Domain
    ApplicationDataCompleted toDomain(ApplicationDto source);
}
