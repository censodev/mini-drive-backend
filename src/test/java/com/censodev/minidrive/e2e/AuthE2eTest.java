package com.censodev.minidrive.e2e;

import com.censodev.minidrive.dto.auth.LoginReq;
import com.censodev.minidrive.dto.auth.TokenRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthE2eTest extends BaseE2eTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @SneakyThrows
    @Override
    @Test
    protected void e2e() {
        var loginRes = login(LoginReq.builder().build());
        assertEquals(200, loginRes.statusCode());

    }

    private HttpResponse<TokenRes> login(LoginReq reqBody) throws IOException, InterruptedException {
        return post(port, "/api/auth/login", null, null, reqBody, TokenRes.class);
    }
}
