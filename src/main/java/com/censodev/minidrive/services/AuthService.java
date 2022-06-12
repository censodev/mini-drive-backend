package com.censodev.minidrive.services;

import com.censodev.minidrive.dto.auth.LoginReq;
import com.censodev.minidrive.dto.auth.LoginRes;
import com.censodev.minidrive.dto.auth.RegisterReq;

public interface AuthService {
    LoginRes login(LoginReq req);

    LoginRes register(RegisterReq req);
}
