package com.crediya.loan.sqs.listener.mapper;
import com.crediya.loan.model.application.AnswersApplicationSqs;
import com.crediya.loan.sqs.listener.dto.AnswersApplicationSqsDto;

public interface AnswersApplicationSqsMapper {
    default AnswersApplicationSqs toModel(AnswersApplicationSqsDto dto) {
        if (dto == null) return null;
        return AnswersApplicationSqs.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .build();
    }

}
