package com.censodev.minidrive.utils;

import com.censodev.minidrive.data.domains.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionUtil {
    public static User getAuthUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }
}
