package com.censodev.minidrive.e2e;

import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.dto.Res;
import com.censodev.minidrive.data.dto.auth.LoginReq;
import com.censodev.minidrive.data.dto.auth.RegisterReq;
import com.censodev.minidrive.data.dto.auth.TokenRes;
import com.censodev.minidrive.data.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.censodev.jwtprovider.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class AuthE2eTest extends BaseE2eTest {
    @LocalServerPort
    private int port;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Test
    void register_HappyCase() throws IOException, InterruptedException {
        var req = Instancio.create(RegisterReq.class);
        var registerRes = register(req);
        assertEquals(200, registerRes.statusCode());
        log.info("{}", registerRes.body());
        var token = registerRes.body().getData().getToken();
        assertEquals(tokenProvider.getDefaultExpireInMs(), registerRes.body().getData().getExpires());
        assertDoesNotThrow(() -> tokenProvider.verify(token));
    }

    @Test
    void register_DuplicateAccount() throws IOException, InterruptedException {
        var user = seedUser();
        var req = Instancio.create(RegisterReq.class);
        req.setUsername(user.getUsername());
        var registerRes = register(req);
        assertEquals(401, registerRes.statusCode());
        log.info("{}", registerRes.body());
    }

    @Test
    void login_HappyCase() throws IOException, InterruptedException {
        var user = seedUser();
        var loginRes = login(LoginReq.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build());
        log.info("{}", loginRes.body());
        assertEquals(200, loginRes.statusCode());
        assertEquals(tokenProvider.getDefaultExpireInMs(), loginRes.body().getData().getExpires());
        var token = loginRes.body().getData().getToken();
        assertDoesNotThrow(() -> tokenProvider.verify(token));
    }

    @Test
    void login_AccountNotFound() throws IOException, InterruptedException {
        var loginRes = login(Instancio.create(LoginReq.class));
        log.info("{}", loginRes.body());
        assertEquals(401, loginRes.statusCode());
    }

    @Test
    void login_WrongPassword() throws IOException, InterruptedException {
        var user = seedUser();
        var req = Instancio.create(LoginReq.class);
        req.setUsername(user.getUsername());
        var loginRes = login(req);
        log.info("{}", loginRes.body());
        assertEquals(401, loginRes.statusCode());
    }

    private HttpResponse<Res<TokenRes>> login(LoginReq reqBody) throws IOException, InterruptedException {
        return post(port, "/api/auth/login", null, null, null, reqBody, TokenRes.class);
    }

    private HttpResponse<Res<TokenRes>> register(RegisterReq reqBody) throws IOException, InterruptedException {
        return post(port, "/api/auth/register", null, null, null, reqBody, TokenRes.class);
    }

    private User seedUser() {
        var mock = Instancio.create(User.class);
        return userRepository.save(mock.toBuilder()
                        .password(passwordEncoder.encode(mock.getPassword()))
                        .build())
                .toBuilder()
                .password(mock.getPassword())
                .build();
    }
}
