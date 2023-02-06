package com.censodev.minidrive.data.mappers;

import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.dto.user.UserRes;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper extends BaseMapper<User, UserRes> {
}
