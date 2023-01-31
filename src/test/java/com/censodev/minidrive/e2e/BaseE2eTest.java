package com.censodev.minidrive.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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

public abstract class BaseE2eTest {
    protected final HttpClient http = HttpClient.newHttpClient();

    protected URI uri(int port, String path) {
        return URI.create("http://localhost:" + port + path);
    }

    protected <T> HttpResponse<T> get(int port,
                                      String path,
                                      String token,
                                      Map<String, String> extraHeaders,
                                      Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders)
                .GET();
        return http.send(reqBuilder.build(), getJsonBodyHandler(resType));
    }

    protected <T> HttpResponse<List<T>> getList(int port,
                                                String path,
                                                String token,
                                                Map<String, String> extraHeaders,
                                                Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders)
                .GET();
        return http.send(reqBuilder.build(), getJsonListBodyHandler(resType));
    }

    protected <T> HttpResponse<T> post(int port,
                                       String path,
                                       String token,
                                       Map<String, String> extraHeaders,
                                       Object reqBody,
                                       Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders)
                .POST(HttpRequest.BodyPublishers.ofString(getObjectMapper().writeValueAsString(reqBody)));
        return http.send(reqBuilder.build(), getJsonBodyHandler(resType));
    }

    protected <T> HttpResponse<T> put(int port,
                                      String path,
                                      String token,
                                      Map<String, String> extraHeaders,
                                      Object reqBody,
                                      Class<T> resType) throws IOException, InterruptedException {
        var reqBuilder = getRequestBuilder(port, path, token, extraHeaders)
                .PUT(HttpRequest.BodyPublishers.ofString(getObjectMapper().writeValueAsString(reqBody)));
        return http.send(reqBuilder.build(), getJsonBodyHandler(resType));
    }

    protected <T> HttpResponse.BodyHandler<T> getJsonBodyHandler(Class<T> resType) {
        return responseInfo -> {
            HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
            return HttpResponse.BodySubscribers.mapping(
                    upstream,
                    resBody -> {
                        try {
                            return getObjectMapper().readValue(resBody, resType);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        };
    }

    protected <T> HttpResponse.BodyHandler<List<T>> getJsonListBodyHandler(Class<T> resType) {
        return responseInfo -> {
            CollectionType collectionType = getObjectMapper().getTypeFactory()
                    .constructCollectionType(List.class, resType);
            HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
            return HttpResponse.BodySubscribers.mapping(
                    upstream,
                    resBody -> {
                        try {
                            return getObjectMapper().readValue(resBody, collectionType);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        };
    }

    protected HttpRequest.Builder getRequestBuilder(int port,
                                                    String path,
                                                    String token,
                                                    Map<String, String> extraHeaders) {
        var reqBuilder = HttpRequest.newBuilder(uri(port, path))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        Optional.ofNullable(extraHeaders)
                .orElseGet(Collections::emptyMap)
                .forEach(reqBuilder::header);
        return reqBuilder;
    }

    protected abstract ObjectMapper getObjectMapper();

    protected abstract void e2e();
}
