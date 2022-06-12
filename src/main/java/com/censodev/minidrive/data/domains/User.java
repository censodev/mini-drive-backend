package com.censodev.minidrive.data.domains;

import censodev.lib.auth.utils.jwt.Credentials;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User implements Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String fullName;

    @Override
    public Object getSubject() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<String> getAuthorities() {
        return new ArrayList<>(Collections.singletonList("ROLE_USER"));
    }
}
