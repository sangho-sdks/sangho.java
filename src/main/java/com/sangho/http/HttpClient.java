package com.sangho.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangho.exception.*;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HttpClient {

    public static final String SDK_VERSION = "1.1.0";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Set<Integer> RETRYABLE_CODES = Set.of(429, 500, 502, 503, 504);

    // Le backend distingue les clés de production ("prod") des clés de test
    // ("test") — il n'existe pas de préfixe "live" côté API Sangho.
    private static final String[] VALID_PREFIXES = {"pk_prod_", "sk_prod_", "pk_test_", "sk_test_"};

    private final OkHttpClient okHttp;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    public final ApiKeyType keyType;
    public final boolean sandbox;

    public HttpClient(String apiKey, String baseUrl, Duration timeout) {
        this(apiKey, baseUrl, timeout, 3);
    }

    public HttpClient(String apiKey, String baseUrl, Duration timeout, int maxRetries) {
        validateApiKey(apiKey);
        validateBaseUrl(baseUrl);

        this.baseUrl    = baseUrl.replaceAll("/+$", "");
        this.timeout    = timeout;
        this.maxRetries = maxRetries;
        this.keyType    = apiKey.startsWith("pk_") ? ApiKeyType.PUBLIC : ApiKeyType.SECRET;
        this.sandbox    = apiKey.startsWith("pk_test_") || apiKey.startsWith("sk_test_");
        this.mapper     = new ObjectMapper();
        this.okHttp     = new OkHttpClient.Builder()
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .addInterceptor(chain -> {
                Request req = chain.request().newBuilder()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "sangho-java/" + SDK_VERSION)
                    .header("X-Sangho-SDK", "java/" + SDK_VERSION)
                    .header("X-Sangho-Environment", sandbox ? "sandbox" : "live")
                    .build();
                return chain.proceed(req);
            })
            .build();
    }

    private static void validateApiKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("apiKey must be a non-empty string.");
        }
        boolean valid = false;
        for (String p : VALID_PREFIXES) {
            if (key.startsWith(p)) { valid = true; break; }
        }
        if (!valid) {
            throw new IllegalArgumentException(
                "Invalid API key format. Expected prefix: " + String.join(", ", VALID_PREFIXES) + ".");
        }
        if (key.length() < 20) {
            throw new IllegalArgumentException("API key is too short.");
        }
    }

    private static void validateBaseUrl(String baseUrl) {
        URI uri;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid baseUrl: \"" + baseUrl + "\".");
        }
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalArgumentException("Invalid baseUrl: \"" + baseUrl + "\".");
        }
        boolean isLocal = "localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost());
        if (!"https".equals(uri.getScheme()) && !isLocal) {
            throw new IllegalArgumentException(
                "Refusing to send API keys over a non-HTTPS baseUrl: \"" + baseUrl + "\". " +
                "Use an https:// URL (localhost/127.0.0.1 are exempt for local development).");
        }
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
        return execute(buildGetRequest(path, params), type);
    }

    public <T> T get(String path, Map<String, String> params, TypeReference<T> typeRef) {
        return execute(buildGetRequest(path, params), typeRef);
    }

    private Request buildGetRequest(String path, Map<String, String> params) {
        HttpUrl parsed = HttpUrl.parse(baseUrl + path);
        if (parsed == null) throw new SanghoException("Invalid URL: " + baseUrl + path);
        HttpUrl.Builder urlBuilder = parsed.newBuilder();
        if (params != null) params.forEach(urlBuilder::addQueryParameter);
        return new Request.Builder().url(urlBuilder.build()).get().build();
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
        return doExecute(req, resp -> deserialize(resp, type));
    }

    private <T> T execute(Request req, TypeReference<T> typeRef) {
        return doExecute(req, resp -> deserializeRef(resp, typeRef));
    }

    private interface Deserializer<T> {
        T apply(Response resp) throws IOException;
    }

    private <T> T doExecute(Request req, Deserializer<T> deserializer) {
        int attempt = 0;
        while (true) {
            Response resp;
            try {
                resp = okHttp.newCall(req).execute();
            } catch (SocketTimeoutException e) {
                if (attempt < maxRetries) {
                    sleepBackoff(attempt);
                    attempt++;
                    continue;
                }
                throw new SanghoTimeoutException(timeout.toSeconds());
            } catch (IOException e) {
                if (attempt < maxRetries) {
                    sleepBackoff(attempt);
                    attempt++;
                    continue;
                }
                throw new SanghoNetworkException(e.getMessage());
            }

            try (Response r = resp) {
                if (RETRYABLE_CODES.contains(r.code()) && attempt < maxRetries) {
                    double delay = r.code() == 429 ? retryAfterFromResponse(r) : backoff(attempt);
                    sleepSeconds(delay);
                    attempt++;
                    continue;
                }
                return deserializer.apply(r);
            } catch (IOException e) {
                throw new SanghoException("Failed to read response: " + e.getMessage());
            }
        }
    }

    private double backoff(int attempt) {
        return Math.pow(2, attempt) * 0.5;
    }

    @SuppressWarnings("unchecked")
    private double retryAfterFromResponse(Response resp) {
        try {
            ResponseBody body = resp.peekBody(Long.MAX_VALUE);
            Map<String, Object> data = mapper.readValue(body.bytes(), Map.class);
            Object retryAfter = data.get("retry_after");
            if (retryAfter instanceof Number n) return n.doubleValue();
        } catch (IOException ignored) {
            // fall through to header/default
        }
        String header = resp.header("Retry-After");
        if (header != null) {
            try { return Double.parseDouble(header); } catch (NumberFormatException ignored) { }
        }
        return 60.0;
    }

    private void sleepBackoff(int attempt) {
        sleepSeconds(backoff(attempt));
    }

    private void sleepSeconds(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SanghoException("HTTP request interrupted: " + e.getMessage());
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
        // Insensible à la casse : le backend envoie tantôt "PUBLIC_KEY_NOT_ALLOWED",
        // tantôt "public_key_not_allowed" selon le chemin qui a rejeté la requête.
        Object rawCode = data.get("code");
        String code = (rawCode instanceof String s) ? s.toLowerCase() : null;

        switch (status) {
            case 401 -> throw new SanghoAuthException(message, "authentication_error", 401, data);
            case 403 -> {
                if ("public_key_not_allowed".equals(code))
                    throw new SanghoPublicKeyException(message, (String) rawCode, 403, data);
                throw new SanghoPermissionException(message, "permission_denied", 403, data);
            }
            case 404 -> throw new SanghoNotFoundException(message, "not_found", 404, data);
            case 409 -> throw new SanghoIdempotencyException(
                "Idempotency key reused with different request parameters.", "idempotency_conflict", 409, data);
            case 422 -> throw new SanghoValidationException(data);
            case 429 -> {
                int retry = (int) retryAfterFromData(data, resp);
                throw new SanghoRateLimitException(retry, data);
            }
            default -> throw new SanghoException(message, "api_error", status, data);
        }
    }

    private double retryAfterFromData(Map<String, Object> data, Response resp) {
        Object retryAfter = data.get("retry_after");
        if (retryAfter instanceof Number n) return n.doubleValue();
        String header = resp.header("Retry-After");
        if (header != null) {
            try { return Double.parseDouble(header); } catch (NumberFormatException ignored) { }
        }
        return 60.0;
    }
}
