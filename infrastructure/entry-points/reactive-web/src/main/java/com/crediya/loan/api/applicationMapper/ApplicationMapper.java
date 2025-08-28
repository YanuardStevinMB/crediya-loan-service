package com.crediya.loan.api.applicationMapper;

import com.crediya.loan.api.dto.ApplicationResponseDto;
import com.crediya.loan.api.dto.ApplicationSaveDto;
import com.crediya.loan.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ApplicationMapper {
    default Application toModel(ApplicationSaveDto dto) {
        if (dto == null) return null;
        return Application.builder()
                .id(dto.Id())
                .amount(dto.amount())
                .term(dto.term())
                .email(dto.email())
                .identityDocument(dto.identityDocument())

                .loanTypeId(dto.loanTypeId())
                .build();


    }

    ApplicationResponseDto toResponseDto(Application model);

}
