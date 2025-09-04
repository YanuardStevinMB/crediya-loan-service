package com.crediya.loan.api.applicationMapper;
import com.crediya.loan.api.dto.ApplicationPaginedDto;
import com.crediya.loan.model.application.ApplicationPagined;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ApplicationPaginedMapper {
    ApplicationPaginedDto toResponseDto(ApplicationPagined model);
}
