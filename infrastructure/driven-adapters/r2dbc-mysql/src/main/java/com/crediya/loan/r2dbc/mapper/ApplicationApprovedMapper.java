package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.application.ApplicationApproved;
import com.crediya.loan.r2dbc.dto.ApplicationApprovedDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface ApplicationApprovedMapper {

    // Domain -> DTO
    ApplicationApprovedDto toDto(ApplicationApproved source);

    // DTO -> Domain
    ApplicationApproved toDomain(ApplicationApprovedDto source);
}
