package com.censodev.minidrive.http.controllers;

import com.censodev.minidrive.data.dto.Res;
import com.censodev.minidrive.data.dto.auth.LoginReq;
import com.censodev.minidrive.data.dto.auth.RegisterReq;
import com.censodev.minidrive.data.dto.auth.TokenRes;
import com.censodev.minidrive.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    private final MessageSource messageSource;

    @PostMapping("login")
    public Res<TokenRes> login(@RequestBody LoginReq req, Locale locale) {
        var data = service.login(req);
        return new Res<>(data, messageSource.getMessage("auth.login-success", null, locale));
    }

    @PostMapping("register")
    public Res<TokenRes> registerMember(@RequestBody RegisterReq req, Locale locale) {
        var data = service.register(req);
        return new Res<>(data, messageSource.getMessage("auth.register-success", null, locale));
    }
}
