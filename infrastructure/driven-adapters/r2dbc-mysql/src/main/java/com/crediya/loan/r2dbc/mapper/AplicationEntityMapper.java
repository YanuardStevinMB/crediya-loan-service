package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AplicationEntityMapper {

    default ApplicationEntity toEntity(Application domain) {
        if (domain == null) return null;
        return ApplicationEntity.builder()
                .id(domain.getId())
                .amount(domain.getAmount())
                .term(domain.getTerm())
                .email(domain.getEmail())
                .identityDocument(domain.getIdentityDocument())
                .stateId(domain.getStateId())
                .loanTypeId(domain.getLoanTypeId())
                .build();
    }

    default Application toDomain(ApplicationEntity entity) {
        if (entity == null) return null;

        return Application.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .term(entity.getTerm())
                .email(entity.getEmail())
                .identityDocument(entity.getIdentityDocument())
                .stateId(entity.getStateId())
                .loanTypeId(entity.getLoanTypeId())
                .build();

    }
}
