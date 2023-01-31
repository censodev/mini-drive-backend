package com.censodev.minidrive.http.controllers;

import com.censodev.minidrive.dto.Res;
import com.censodev.minidrive.dto.auth.LoginReq;
import com.censodev.minidrive.dto.auth.TokenRes;
import com.censodev.minidrive.dto.auth.RegisterReq;
import com.censodev.minidrive.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("login")
    public ResponseEntity<Res<TokenRes>> login(@RequestBody LoginReq req) {
        var data = service.login(req);
        return ResponseEntity.ok(new Res<>(data, "Đăng nhập thành công"));
    }

    @PostMapping("register")
    public ResponseEntity<Res<TokenRes>> registerMember(@RequestBody RegisterReq req) {
        var data = service.register(req);
        return ResponseEntity.ok(new Res<>(data, "Đăng ký thành công"));
    }
}
