package com.censodev.minidrive.utils;

import com.censodev.minidrive.data.domains.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class SessionUtil {
    public User getAuthUser() {
        return Optional
                .ofNullable((User) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getCredentials())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Phiên đăng nhập đã hết hạn"));
    }
}
