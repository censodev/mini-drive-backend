package com.censodev.minidrive.services;

import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.dto.auth.LoginReq;
import com.censodev.minidrive.data.dto.auth.RegisterReq;
import com.censodev.minidrive.data.dto.auth.TokenRes;
import com.censodev.minidrive.data.repositories.UserRepository;
import com.censodev.minidrive.exceptions.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.censodev.jwtprovider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider tokenProvider;
    private final ObjectMapper mapper;
    private final MessageSource messageSource;

    @Override
    public TokenRes login(LoginReq req) {
        var locale = LocaleContextHolder.getLocale();
        var u = userRepository
                .findByUsername(req.getUsername())
                .orElseThrow(() -> new UnauthorizedException(messageSource.getMessage("auth.account-not-found", null, locale)));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new UnauthorizedException(messageSource.getMessage("auth.wrong-password", null, locale));
        }
        var token = tokenProvider.generate(u.toBuilder().password(null).build());
        return TokenRes.builder()
                .token(token)
                .expires(tokenProvider.getDefaultExpireInMs())
                .build();
    }

    @Override
    public TokenRes register(RegisterReq req) {
        var locale = LocaleContextHolder.getLocale();
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new UnauthorizedException(messageSource.getMessage("auth.duplicate-account", null, locale));
        }
        var u = mapper.convertValue(req, User.class);
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u = userRepository.save(u);
        var token = tokenProvider.generate(u.toBuilder().password(null).build());
        return TokenRes.builder()
                .token(token)
                .expires(tokenProvider.getDefaultExpireInMs())
                .build();
    }
}
