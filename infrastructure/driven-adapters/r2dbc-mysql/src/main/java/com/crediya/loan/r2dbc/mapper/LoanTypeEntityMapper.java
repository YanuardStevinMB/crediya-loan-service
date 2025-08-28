package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.loantype.LoanType;
import com.crediya.loan.r2dbc.entity.LoanTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanTypeEntityMapper {

    default LoanType toDomain(LoanTypeEntity entity) {
        if (entity == null) return null;
        return LoanType.builder()
                .id(entity.getId())
                .name(entity.getName())
                .amountMin(entity.getAmountMin())
                .amountMax(entity.getAmountMax())
                .interestRate(entity.getInterestRate())
                .automaticValidation(entity.getAutomaticValidation())
                .build();
    }

    default LoanTypeEntity toEntity(LoanType domain) {
        if (domain == null) return null;
        return LoanTypeEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .amountMin(domain.getAmountMin())
                .amountMax(domain.getAmountMax())
                .interestRate(domain.getInterestRate())
                .automaticValidation(domain.getAutomaticValidation())
                .build();
    }
}
