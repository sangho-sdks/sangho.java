package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecurityResource {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;
    private final String path = "/security/";

    public SecurityResource(HttpClient http) { this.http = http; }

    /** Récupère le profil de sécurité de l'app courante. */
    public Map<String, Object> retrieve() {
        http.assertSecretKey("security.retrieve");
        return http.get(path + "me/", null, MAP_TYPE);
    }

    /** Met à jour les paramètres de sécurité (liste blanche d'IP, 2FA, etc.). */
    public Map<String, Object> update(Map<String, Object> payload) {
        http.assertSecretKey("security.update");
        return http.patch(path + "update_me/", payload, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> addAllowedIps(List<String> ips) {
        // Pas d'action dédiée côté backend pour ajouter/retirer des IP : on
        // relit le profil, on recompose la liste complète, puis on la
        // renvoie via update().
        http.assertSecretKey("security.addAllowedIps");
        Map<String, Object> profile = retrieve();
        Object existing = profile.get("allowed_ips");
        Set<String> merged = new LinkedHashSet<>();
        if (existing instanceof List<?> list) list.forEach(ip -> merged.add(String.valueOf(ip)));
        merged.addAll(ips);
        return update(Map.of("allowed_ips", List.copyOf(merged)));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> removeAllowedIps(List<String> ips) {
        http.assertSecretKey("security.removeAllowedIps");
        Map<String, Object> profile = retrieve();
        Object existing = profile.get("allowed_ips");
        Set<String> remaining = new LinkedHashSet<>();
        if (existing instanceof List<?> list) list.forEach(ip -> remaining.add(String.valueOf(ip)));
        remaining.removeAll(ips);
        return update(Map.of("allowed_ips", List.copyOf(remaining)));
    }
}
