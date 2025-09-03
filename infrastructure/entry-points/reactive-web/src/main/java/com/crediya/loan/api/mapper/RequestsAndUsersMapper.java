package com.crediya.loan.api.mapper;

import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.RequestsAndUsersResponseDto;
import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.requestsandusers.RequestsAndUsers;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface RequestsAndUsersMapper {
    RequestsAndUsersResponseDto toResponseDto(RequestsAndUsers model);

}
