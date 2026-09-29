package com.sangho.exception;

import java.util.Map;
import java.util.Set;

/**
 * Erreur de base : toutes les erreurs Sangho en héritent (miroir de {@code SanghoError} du SDK JS).
 *
 * <ul>
 *   <li>{@link #getType()} : catégorie large ({@code VALIDATION_ERROR}, {@code RATE_LIMIT_ERROR},
 *       {@code NETWORK_ERROR}…), pratique pour un {@code switch}.</li>
 *   <li>{@link #getCode()} : code métier précis renvoyé par le backend ({@code AMOUNT_TOO_SMALL},
 *       {@code CURRENCY_NOT_IN_PLAN}…). Le catalogue appartient au backend et grandit : c'est une chaîne, pas une
 *       énumération. Sans réponse du serveur (réseau, délai), il vaut {@link #getType()}.</li>
 * </ul>
 */
public class SanghoException extends RuntimeException {

    /** Les 7 premières valeurs sont la taxonomie du backend ; les 2 dernières sont propres au SDK. */
    public static final Set<String> KNOWN_TYPES = Set.of(
        "AUTHENTICATION_ERROR", "PERMISSION_ERROR", "NOT_FOUND_ERROR", "CONFLICT_ERROR", "VALIDATION_ERROR",
        "RATE_LIMIT_ERROR", "API_ERROR", "NETWORK_ERROR", "TIMEOUT_ERROR"
    );

    private final String type;
    private final String code;
    private final int statusCode;
    private final Map<String, Object> raw;

    protected SanghoException(String message, String type, String defaultCode, int statusCode, Map<String, Object> raw) {
        super(message);
        this.raw        = raw == null ? Map.of() : raw;
        this.type       = resolveType(type, this.raw);
        Object rawCode  = this.raw.get("code");
        this.code       = rawCode instanceof String s && !s.isEmpty() ? s : (defaultCode != null ? defaultCode : this.type);
        this.statusCode = statusCode;
    }

    public SanghoException(String message, String code, int statusCode, Map<String, Object> raw) {
        this(message, "API_ERROR", code, statusCode, raw);
    }

    public SanghoException(String message) {
        this(message, "API_ERROR", null, 0, Map.of());
    }

    /** Un {@code type} reconnu renvoyé par le backend prime (correspondance 1:1) ; sinon la catégorie de la sous-classe. */
    private static String resolveType(String defaultType, Map<String, Object> raw) {
        Object backend = raw.get("type");
        if (backend instanceof String s && KNOWN_TYPES.contains(s.toUpperCase())) return s.toUpperCase();
        return defaultType;
    }

    public String getType()             { return type; }
    public String getCode()             { return code; }
    public int getStatusCode()          { return statusCode; }
    public Map<String, Object> getRaw() { return raw; }

    /** Identifiant de la requête à communiquer au support, si le backend en a fourni un. */
    public String getRequestId()        { return raw.get("request_id") instanceof String s ? s : null; }

    /** Lien vers la documentation de ce code d'erreur, si fourni. */
    public String getDocUrl()           { return raw.get("doc_url") instanceof String s ? s : null; }

    /** Champ fautif, si fourni. */
    public String getParam()            { return raw.get("param") instanceof String s ? s : null; }
}
