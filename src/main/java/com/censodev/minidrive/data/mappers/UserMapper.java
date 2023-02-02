package com.censodev.minidrive.data.mappers;

import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.dto.user.UserRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class UserMapper extends Mapper<User, UserRes> {
    public UserMapper(ObjectMapper mapper) {
        super(mapper, User.class, UserRes.class);
    }
}
