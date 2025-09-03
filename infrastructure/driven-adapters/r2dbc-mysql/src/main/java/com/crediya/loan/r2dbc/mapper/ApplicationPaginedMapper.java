package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.application.Application;
import com.crediya.loan.model.application.ApplicationPagined;
import com.crediya.loan.r2dbc.entity.ApplicationEntity;

public interface ApplicationPaginedMapper {

    default ApplicationPagined toDomain(ApplicationPagined entity) {
        if (entity == null) return null;

        return ApplicationPagined.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .term(entity.getTerm())
                .email(entity.getEmail())
                .identityDocument(entity.getIdentityDocument())
                .stateId(entity.getStateId())
                .loanTypeId(entity.getLoanTypeId())
                .stateName(entity.getStateName())

                .build();

    }

}
