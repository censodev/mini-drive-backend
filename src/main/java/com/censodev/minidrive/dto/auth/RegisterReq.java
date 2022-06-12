package com.censodev.minidrive.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {
    private String username;
    private String password;
    private String fullName;
}
