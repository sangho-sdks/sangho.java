package com.sangho.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangho.exception.*;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class HttpClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int[] RETRY_DELAYS_MS = {500, 1000, 2000};
    private static final int[] RETRYABLE_CODES = {429, 500, 502, 503, 504};

    private final OkHttpClient okHttp;
    private final ObjectMapper mapper;
    private final String baseUrl;
    public final ApiKeyType keyType;

    public HttpClient(String apiKey, String baseUrl, Duration timeout) {
        validateApiKey(apiKey);
        this.baseUrl  = baseUrl.replaceAll("/+$", "");
        this.keyType  = apiKey.startsWith("pk_") ? ApiKeyType.PUBLIC : ApiKeyType.SECRET;
        this.mapper   = new ObjectMapper();
        this.okHttp   = new OkHttpClient.Builder()
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .addInterceptor(chain -> {
                Request req = chain.request().newBuilder()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "sangho-java/1.0.0")
                    .build();
                return chain.proceed(req);
            })
            .build();
    }

    public void assertSecretKey(String method) {
        if (keyType == ApiKeyType.PUBLIC) {
            throw new SanghoPublicKeyException(
                "Method `" + method + "` requires a secret key (sk_…). You provided a public key (pk_…).",
                "public_key_not_allowed", 403, Map.of()
            );
        }
    }

    public <T> T get(String path, Map<String, String> params, Class<T> type) {
        HttpUrl parsed = HttpUrl.parse(baseUrl + path);
        if (parsed == null) throw new SanghoException("Invalid URL: " + baseUrl + path);
        HttpUrl.Builder urlBuilder = parsed.newBuilder();
        if (params != null) params.forEach(urlBuilder::addQueryParameter);
        return execute(new Request.Builder().url(urlBuilder.build()).get().build(), type);
    }

    public <T> T get(String path, Map<String, String> params, TypeReference<T> typeRef) {
        HttpUrl parsed = HttpUrl.parse(baseUrl + path);
        if (parsed == null) throw new SanghoException("Invalid URL: " + baseUrl + path);
        HttpUrl.Builder urlBuilder = parsed.newBuilder();
        if (params != null) params.forEach(urlBuilder::addQueryParameter);
        return execute(new Request.Builder().url(urlBuilder.build()).get().build(), typeRef);
    }

    public <T> T post(String path, Object body, Class<T> type) {
        String idempotencyKey = UUID.randomUUID().toString();
        RequestBody rb = toRequestBody(body);
        Request req = new Request.Builder()
            .url(baseUrl + path)
            .header("Idempotency-Key", idempotencyKey)
            .post(rb).build();
        return execute(req, type);
    }

    public <T> T patch(String path, Object body, Class<T> type) {
        Request req = new Request.Builder().url(baseUrl + path).patch(toRequestBody(body)).build();
        return execute(req, type);
    }

    public void delete(String path) {
        execute(new Request.Builder().url(baseUrl + path).delete().build(), Void.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> options(String path) {
        Request req = new Request.Builder().url(baseUrl + path).method("OPTIONS", null).build();
        return execute(req, Map.class);
    }

    private RequestBody toRequestBody(Object body) {
        try {
            return RequestBody.create(mapper.writeValueAsBytes(body), JSON);
        } catch (IOException e) {
            throw new SanghoException("Failed to serialize request body: " + e.getMessage());
        }
    }

    private <T> T execute(Request req, Class<T> type) {
        for (int delay : RETRY_DELAYS_MS) {
            try (Response resp = okHttp.newCall(req).execute()) {
                if (isRetryable(resp.code())) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SanghoException("HTTP request interrupted: " + e.getMessage());
                    }
                    continue;
                }
                return deserialize(resp, type);
            } catch (IOException e) {
                throw new SanghoException("HTTP request failed: " + e.getMessage());
            }
        }
        try (Response resp = okHttp.newCall(req).execute()) {
            return deserialize(resp, type);
        } catch (IOException e) {
            throw new SanghoException("HTTP request failed: " + e.getMessage());
        }
    }

    private <T> T execute(Request req, TypeReference<T> typeRef) {
        try (Response resp = okHttp.newCall(req).execute()) {
            return deserializeRef(resp, typeRef);
        } catch (IOException e) {
            throw new SanghoException("HTTP request failed: " + e.getMessage());
        }
    }

    private <T> T deserialize(Response resp, Class<T> type) throws IOException {
        if (resp.code() == 204 || type == Void.class) return null;
        ResponseBody responseBody = resp.body();
        if (responseBody == null) throw new SanghoException("Response body is null");
        byte[] body = responseBody.bytes();
        if (resp.isSuccessful()) return mapper.readValue(body, type);
        raiseForStatus(resp.code(), body, resp);
        return null;
    }

    private <T> T deserializeRef(Response resp, TypeReference<T> typeRef) throws IOException {
        if (resp.code() == 204) return null;
        ResponseBody responseBody = resp.body();
        if (responseBody == null) throw new SanghoException("Response body is null");
        byte[] body = responseBody.bytes();
        if (resp.isSuccessful()) return mapper.readValue(body, typeRef);
        raiseForStatus(resp.code(), body, resp);
        return null;
    }

    @SuppressWarnings("unchecked")
    private void raiseForStatus(int status, byte[] body, Response resp) throws IOException {
        Map<String, Object> data = mapper.readValue(body, Map.class);
        String message = (String) data.getOrDefault("message", data.getOrDefault("detail", "API error"));
        String code    = (String) data.get("code");

        switch (status) {
            case 401 -> throw new SanghoAuthException(message, "authentication_error", 401, data);
            case 403 -> {
                if ("public_key_not_allowed".equals(code))
                    throw new SanghoPublicKeyException(message, code, 403, data);
                throw new SanghoPermissionException(message, "permission_denied", 403, data);
            }
            case 404 -> throw new SanghoNotFoundException(message, "not_found", 404, data);
            case 409 -> throw new SanghoIdempotencyException("Idempotency key conflict.", "idempotency_conflict", 409, data);
            case 422 -> throw new SanghoValidationException(message, data);
            case 429 -> {
                int retry = ((Number) data.getOrDefault("retry_later",
                    parseInt(resp.header("Retry-After", "60")))).intValue();
                throw new SanghoRateLimitException(retry);
            }
            default  -> throw new SanghoException(message, "api_error", status, data);
        }
    }

    private boolean isRetryable(int code) {
        for (int c : RETRYABLE_CODES) if (c == code) return true;
        return false;
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 60; }
    }

    private static void validateApiKey(String key) {
        String[] prefixes = {"pk_live_", "sk_live_", "pk_test_", "sk_test_"};
        for (String p : prefixes) if (key.startsWith(p)) return;
        throw new IllegalArgumentException("Invalid API key format. Expected prefix: pk_live_, sk_live_, pk_test_, sk_test_");
    }
}
