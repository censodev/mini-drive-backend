package com.censodev.minidrive.services;

import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.repositories.UserRepository;
import com.censodev.minidrive.dto.auth.LoginReq;
import com.censodev.minidrive.dto.auth.LoginRes;
import com.censodev.minidrive.dto.auth.RegisterReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.censodev.jwtprovider.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider tokenProvider;
    private final ObjectMapper mapper;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtProvider tokenProvider,
                           ObjectMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.mapper = mapper;
    }

    @Override
    public LoginRes login(LoginReq req) {
        var u = userRepository
                .findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Mật khẩu không chính xác");
        }
        var token = tokenProvider.generate(u.toBuilder().password(null).build());
        return LoginRes.builder()
                .token(token)
                .expires(tokenProvider.getDefaultExpireInMs())
                .build();
    }

    @Override
    public LoginRes register(RegisterReq req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Tài khoản đã có người sử dụng");
        }
        var u = mapper.convertValue(req, User.class);
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u = userRepository.save(u);
        var token = tokenProvider.generate(u.toBuilder().password(null).build());
        return LoginRes.builder()
                .token(token)
                .expires(tokenProvider.getDefaultExpireInMs())
                .build();
    }
}
