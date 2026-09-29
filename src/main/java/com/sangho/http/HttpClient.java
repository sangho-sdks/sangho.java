package com.sangho.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangho.exception.*;
import okhttp3.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Client HTTP de l'API Sangho : erreurs typées, retry avec backoff exponentiel sur 429 / 5xx / erreurs réseau
 * (miroir du {@code HttpClient} du SDK JS, {@code core/http.ts}).
 */
public class HttpClient {

    /** Attend avant un nouvel essai ; remplaçable dans les tests. */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(byte[] body) throws IOException;
    }

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    /** Le backend distingue les clés de production ("prod") des clés de test ("test") : il n'existe pas de préfixe "live". */
    private static final String[] VALID_PREFIXES = {"pk_prod_", "sk_prod_", "pk_test_", "sk_test_"};
    public static final int DEFAULT_MAX_RETRIES = 3;

    private final OkHttpClient okHttp;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final int maxRetries;
    private final long timeoutMillis;
    private final Sleeper sleeper;
    public final ApiKeyType keyType;
    public final boolean sandbox;

    public HttpClient(String apiKey, String baseUrl, Duration timeout) {
        this(apiKey, baseUrl, timeout, DEFAULT_MAX_RETRIES, Thread::sleep);
    }

    public HttpClient(String apiKey, String baseUrl, Duration timeout, int maxRetries, Sleeper sleeper) {
        validateApiKey(apiKey);
        validateBaseUrl(baseUrl);
        this.baseUrl       = baseUrl.replaceAll("/+$", "");
        this.keyType       = apiKey.startsWith("pk_") ? ApiKeyType.PUBLIC : ApiKeyType.SECRET;
        this.sandbox       = apiKey.startsWith("pk_test_") || apiKey.startsWith("sk_test_");
        this.maxRetries    = maxRetries;
        this.sleeper       = sleeper;
        this.timeoutMillis = timeout.toMillis();
        this.mapper        = new ObjectMapper();
        String environment = sandbox ? "sandbox" : "live";
        this.okHttp        = new OkHttpClient.Builder()
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .followRedirects(false)  // un endpoint d'API ne redirige pas : ne pas renvoyer la clé ailleurs
            .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "sangho-java/" + SdkVersion.VERSION)
                .header("X-Sangho-SDK", "java/" + SdkVersion.VERSION)
                .header("X-Sangho-Environment", environment)
                .build()))
            .build();
    }

    public void assertSecretKey(String method) {
        if (keyType == ApiKeyType.PUBLIC) {
            throw new SanghoPublicKeyException(
                "Method `" + method + "` requires a secret key (sk_…). You provided a public key (pk_…)."
            );
        }
    }

    // ── Verbes ────────────────────────────────────────────────────────────────

    public <T> T get(String path, Map<String, String> params, Class<T> type) {
        return execute(() -> getRequest(path, params), classParser(type));
    }

    public <T> T get(String path, Map<String, String> params, TypeReference<T> typeRef) {
        return execute(() -> getRequest(path, params), body -> mapper.readValue(body, typeRef));
    }

    public <T> T post(String path, Object body, Class<T> type) {
        String idempotencyKey = UUID.randomUUID().toString();
        return execute(() -> new Request.Builder().url(url(path)).header("Idempotency-Key", idempotencyKey)
            .post(toRequestBody(body)).build(), classParser(type));
    }

    public <T> T patch(String path, Object body, Class<T> type) {
        return execute(() -> new Request.Builder().url(url(path)).patch(toRequestBody(body)).build(), classParser(type));
    }

    public void delete(String path) {
        delete(path, Void.class);
    }

    /** DELETE qui retourne le corps de la réponse (ex. payment-intents : {@code DELETE} annule et renvoie l'objet). */
    public <T> T delete(String path, Class<T> type) {
        return execute(() -> new Request.Builder().url(url(path)).delete().build(), classParser(type));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> options(String path) {
        return execute(() -> new Request.Builder().url(url(path)).method("OPTIONS", null).build(), classParser(Map.class));
    }

    // ── Interne ───────────────────────────────────────────────────────────────

    private HttpUrl url(String path) {
        HttpUrl parsed = HttpUrl.parse(baseUrl + path);
        if (parsed == null) throw new SanghoException("Invalid URL: " + baseUrl + path);
        return parsed;
    }

    private Request getRequest(String path, Map<String, String> params) {
        HttpUrl.Builder builder = url(path).newBuilder();
        if (params != null) params.forEach((k, v) -> { if (v != null) builder.addQueryParameter(k, v); });
        return new Request.Builder().url(builder.build()).get().build();
    }

    private RequestBody toRequestBody(Object body) {
        try {
            // Les objets de paramètres (builders) n'ont pas de getters : ils se sérialisent via toMap().
            Object payload = body instanceof com.sangho.param.RequestParams p ? p.toMap() : body;
            return RequestBody.create(mapper.writeValueAsBytes(payload), JSON);
        } catch (IOException e) {
            throw new SanghoException("Failed to serialize request body: " + e.getMessage());
        }
    }

    private <T> Parser<T> classParser(Class<T> type) {
        return body -> type == Void.class || body.length == 0 ? null : mapper.readValue(body, type);
    }

    @FunctionalInterface
    private interface RequestFactory {
        Request build();
    }

    private <T> T execute(RequestFactory factory, Parser<T> parser) {
        int attempt = 0;
        while (true) {
            try (Response resp = okHttp.newCall(factory.build()).execute()) {
                byte[] body = resp.body() == null ? new byte[0] : resp.body().bytes();
                if (resp.isSuccessful()) return resp.code() == 204 ? null : parse(parser, body);

                SanghoException error = buildError(resp.code(), body, resp);
                if (!isRetryable(error) || attempt >= maxRetries) throw error;
                pause(error instanceof SanghoRateLimitException rl && rl.getRetryAfter() > 0
                    ? rl.getRetryAfter() * 1000L : backoff(attempt));
            } catch (IOException e) {
                // Aucune réponse du serveur (DNS, connexion refusée, délai dépassé…) : transitoire, on réessaie.
                if (attempt >= maxRetries) {
                    throw e instanceof InterruptedIOException
                        ? new SanghoTimeoutException(timeoutMillis)
                        : new SanghoNetworkException(e.getMessage());
                }
                pause(backoff(attempt));
            }
            attempt++;
        }
    }

    private <T> T parse(Parser<T> parser, byte[] body) {
        try {
            return parser.parse(body);
        } catch (IOException e) {
            throw new SanghoException("Invalid JSON in response: " + e.getMessage());
        }
    }

    private void pause(long millis) {
        try {
            sleeper.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SanghoException("HTTP request interrupted: " + e.getMessage());
        }
    }

    private static long backoff(int attempt) {
        return (1L << attempt) * 500L;
    }

    /** 429 et 5xx sont transitoires ; les autres 4xx (400/401/403/404/409/422) sont permanents : jamais de retry. */
    private static boolean isRetryable(SanghoException error) {
        return error instanceof SanghoRateLimitException || error.getStatusCode() >= 500;
    }

    @SuppressWarnings("unchecked")
    private SanghoException buildError(int status, byte[] body, Response resp) {
        Map<String, Object> data;
        try {
            Object parsed = body.length == 0 ? Map.of() : mapper.readValue(body, Object.class);
            data = parsed instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        } catch (IOException e) {
            data = Map.of();
        }
        Object rawMessage = data.get("message") != null ? data.get("message") : data.get("detail");
        String message = rawMessage instanceof String s ? s : rawMessage != null ? String.valueOf(rawMessage) : "API error";
        // Insensible à la casse : le backend envoie tantôt "PUBLIC_KEY_NOT_ALLOWED", tantôt "public_key_not_allowed".
        String code = data.get("code") instanceof String c ? c.toLowerCase() : "";

        return switch (status) {
            case 401 -> new SanghoAuthException(message, data);
            case 403 -> "public_key_not_allowed".equals(code)
                ? new SanghoPublicKeyException(message, data)
                : new SanghoPermissionException(message, data);
            case 404 -> new SanghoNotFoundException(message, data);
            case 409 -> new SanghoIdempotencyException(data);
            case 422 -> new SanghoValidationException(validationMessage(data, message), data);
            case 429 -> new SanghoRateLimitException(data.get("message") instanceof String s ? s : null,
                retryAfter(resp, data), data);
            default  -> new SanghoException(message, null, status, data);
        };
    }

    private static String validationMessage(Map<String, Object> data, String fallback) {
        Object fields = data.get("detail") instanceof Map<?, ?> ? data.get("detail") : data.get("errors");
        if (!(fields instanceof Map<?, ?> map) || map.isEmpty()) return fallback;
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(k).append(": ").append(v instanceof Iterable<?> it ? String.join(", ", stringify(it)) : v);
        });
        return sb.toString();
    }

    private static java.util.List<String> stringify(Iterable<?> items) {
        java.util.List<String> out = new java.util.ArrayList<>();
        items.forEach(i -> out.add(String.valueOf(i)));
        return out;
    }

    private static int retryAfter(Response resp, Map<String, Object> data) {
        Object value = data.get("retry_after");
        if (value instanceof Number n) return (int) Math.ceil(n.doubleValue());
        try {
            return (int) Math.ceil(Double.parseDouble(resp.header("Retry-After", "60")));
        } catch (NumberFormatException e) {
            return 60;
        }
    }

    private static void validateApiKey(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("apiKey must be a non-empty string.");
        for (String p : VALID_PREFIXES) {
            if (key.startsWith(p)) {
                if (key.length() < 20) throw new IllegalArgumentException("API key is too short.");
                return;
            }
        }
        throw new IllegalArgumentException(
            "Invalid API key format. Keys must start with one of: " + String.join(", ", VALID_PREFIXES) + "."
        );
    }

    private static void validateBaseUrl(String baseUrl) {
        HttpUrl parsed = baseUrl == null ? null : HttpUrl.parse(baseUrl);
        if (parsed == null) throw new IllegalArgumentException("Invalid baseUrl: \"" + baseUrl + "\".");
        boolean local = parsed.host().equals("localhost") || parsed.host().equals("127.0.0.1");
        if (!parsed.isHttps() && !local) {
            throw new IllegalArgumentException("Refusing to send API keys over a non-HTTPS baseUrl: \"" + baseUrl
                + "\". Use an https:// URL (localhost/127.0.0.1 are exempt for local development).");
        }
    }
}
