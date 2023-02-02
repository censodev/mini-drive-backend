package com.censodev.minidrive.e2e;

import com.censodev.minidrive.data.dto.Res;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseE2eTest {
    protected final HttpClient http = HttpClient.newHttpClient();

    protected URI uri(int port, String path) {
        return URI.create("http://localhost:" + port + path);
    }

    protected <T> HttpResponse<T> get(int port,
                                      String path,
                                      String token,
                                      Map<String, String> extraHeaders,
                                      Map<String, String> params,
                                      Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders, params)
                .GET();
        return http.send(reqBuilder.build(), getJsonBodyHandler(Res.class, resType));
    }

    protected <T> HttpResponse<List<T>> getAsList(int port,
                                                  String path,
                                                  String token,
                                                  Map<String, String> extraHeaders,
                                                  Map<String, String> params,
                                                  Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders, params)
                .GET();
        return http.send(reqBuilder.build(), getJsonBodyHandler(Res.class, List.class, resType));
    }

    protected <T> HttpResponse<Res<T>> post(int port,
                                            String path,
                                            String token,
                                            Map<String, String> extraHeaders,
                                            Map<String, String> params,
                                            Object reqBody,
                                            Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders, params)
                .POST(HttpRequest.BodyPublishers.ofString(getObjectMapper().writeValueAsString(reqBody)));
        return http.send(reqBuilder.build(), getJsonBodyHandler(Res.class, resType));
    }

    protected <T> HttpResponse<Res<T>> put(int port,
                                           String path,
                                           String token,
                                           Map<String, String> extraHeaders,
                                           Map<String, String> params,
                                           Object reqBody,
                                           Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders, params)
                .PUT(HttpRequest.BodyPublishers.ofString(getObjectMapper().writeValueAsString(reqBody)));
        return http.send(reqBuilder.build(), getJsonBodyHandler(Res.class, resType));
    }

    protected <T> HttpResponse.BodyHandler<T> getJsonBodyHandler(Class<?> resType, Class<?>... paramResTypes) {
        JavaType javaType = getObjectMapper().getTypeFactory().constructParametricType(resType, paramResTypes);
        return responseInfo -> {
            HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
            return HttpResponse.BodySubscribers.mapping(
                    upstream,
                    resBody -> {
                        try {
                            log.info("Response body: {}", resBody);
                            return getObjectMapper().readValue(resBody, javaType);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        };
    }

    protected HttpRequest.Builder getRequestBuilder(int port,
                                                    String path,
                                                    String token,
                                                    Map<String, String> extraHeaders,
                                                    Map<String, String> params) {
        var strParams = Optional.ofNullable(params)
                .orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&", "?", ""));
        var reqBuilder = HttpRequest.newBuilder(uri(port, path + strParams))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        Optional.ofNullable(extraHeaders)
                .orElseGet(Collections::emptyMap)
                .forEach(reqBuilder::header);
        return reqBuilder;
    }

    protected abstract ObjectMapper getObjectMapper();
}
