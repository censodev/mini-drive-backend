package com.censodev.minidrive.services;

import com.censodev.minidrive.data.dto.auth.LoginReq;
import com.censodev.minidrive.data.dto.auth.TokenRes;
import com.censodev.minidrive.data.dto.auth.RegisterReq;

public interface AuthService {
    TokenRes login(LoginReq req);

    TokenRes register(RegisterReq req);
}
