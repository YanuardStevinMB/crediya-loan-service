package com.crediya.loan.consumer.mapper;
import com.crediya.loan.consumer.dto.UserDto;
import com.crediya.loan.model.user.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserLoadMapper {
    User toDomain(UserDto dto);
}
