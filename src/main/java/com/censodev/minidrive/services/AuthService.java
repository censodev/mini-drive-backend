package com.censodev.minidrive.services;

import com.censodev.minidrive.dto.auth.LoginReq;
import com.censodev.minidrive.dto.auth.TokenRes;
import com.censodev.minidrive.dto.auth.RegisterReq;

public interface AuthService {
    TokenRes login(LoginReq req);

    TokenRes register(RegisterReq req);
}
