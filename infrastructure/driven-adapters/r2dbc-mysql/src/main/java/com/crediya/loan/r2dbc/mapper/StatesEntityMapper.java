package com.crediya.loan.r2dbc.mapper;

import com.crediya.loan.model.states.States;
import com.crediya.loan.r2dbc.entity.StatesEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatesEntityMapper {

    default StatesEntity toEntity(States domain) {
        if (domain == null) return null;
        return StatesEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }

    default States toDomain(StatesEntity entity) {
        if (entity == null) return null;
        return States.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
