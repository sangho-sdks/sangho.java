package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;

import java.util.Map;

/**
 * client.terminal().readers()   — gestion des lecteurs de carte physiques
 * client.terminal().sessions()  — sessions de paiement in-person
 * client.terminal().offline()   — synchronisation des transactions hors-ligne
 */
public class TerminalResource {

    private final Readers readers;
    private final Sessions sessions;
    private final Offline offline;

    public TerminalResource(HttpClient http) {
        this.readers = new Readers(http);
        this.sessions = new Sessions(http);
        this.offline = new Offline(http);
    }

    public Readers readers()   { return readers; }
    public Sessions sessions() { return sessions; }
    public Offline offline()   { return offline; }

    public static class Readers {
        private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
        private final HttpClient http;
        private final String path = "/terminal/readers/";

        Readers(HttpClient http) { this.http = http; }

        public Map<String, Object> list(Map<String, String> params) {
            http.assertSecretKey("terminal.readers.list");
            return http.get(path, params, MAP_TYPE);
        }

        public Map<String, Object> retrieve(String id) {
            http.assertSecretKey("terminal.readers.retrieve");
            return http.get(path + id + "/", null, MAP_TYPE);
        }

        public Map<String, Object> create(Map<String, Object> payload) {
            http.assertSecretKey("terminal.readers.create");
            return http.post(path, payload, Map.class);
        }

        public Map<String, Object> update(String id, Map<String, Object> payload) {
            http.assertSecretKey("terminal.readers.update");
            return http.patch(path + id + "/", payload, Map.class);
        }

        /** Désactive un lecteur (soft-delete). */
        public void disable(String id) {
            http.assertSecretKey("terminal.readers.disable");
            http.delete(path + id + "/");
        }

        public Map<String, Object> refreshToken(String id) {
            http.assertSecretKey("terminal.readers.refreshToken");
            return http.post(path + id + "/refresh-token/", Map.of(), Map.class);
        }

        public Map<String, Object> heartbeat(String id) {
            http.assertSecretKey("terminal.readers.heartbeat");
            return http.post(path + id + "/heartbeat/", Map.of(), Map.class);
        }

        public Map<String, Object> options() {
            return http.options(path);
        }
    }

    public static class Sessions {
        private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
        private final HttpClient http;
        private final String path = "/terminal/sessions/";

        Sessions(HttpClient http) { this.http = http; }

        public Map<String, Object> list(Map<String, String> params) {
            http.assertSecretKey("terminal.sessions.list");
            return http.get(path, params, MAP_TYPE);
        }

        public Map<String, Object> retrieve(String id) {
            http.assertSecretKey("terminal.sessions.retrieve");
            return http.get(path + id + "/", null, MAP_TYPE);
        }

        public Map<String, Object> create(Map<String, Object> payload) {
            http.assertSecretKey("terminal.sessions.create");
            return http.post(path, payload, Map.class);
        }

        public Map<String, Object> presentPaymentMethod(String id, Map<String, Object> payload) {
            http.assertSecretKey("terminal.sessions.presentPaymentMethod");
            return http.post(path + id + "/present-payment-method/", payload != null ? payload : Map.of(), Map.class);
        }

        public Map<String, Object> pollStatus(String id) {
            http.assertSecretKey("terminal.sessions.pollStatus");
            return http.get(path + id + "/status/", null, MAP_TYPE);
        }

        public Map<String, Object> cancel(String id) {
            http.assertSecretKey("terminal.sessions.cancel");
            return http.post(path + id + "/cancel/", Map.of(), Map.class);
        }

        public Map<String, Object> options() {
            return http.options(path);
        }
    }

    public static class Offline {
        private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
        private final HttpClient http;
        private final String path = "/terminal/offline/sync/";

        Offline(HttpClient http) { this.http = http; }

        public Map<String, Object> sync(Map<String, Object> payload) {
            http.assertSecretKey("terminal.offline.sync");
            return http.post(path, payload, Map.class);
        }

        public Map<String, Object> list(Map<String, String> params) {
            http.assertSecretKey("terminal.offline.list");
            return http.get(path, params, MAP_TYPE);
        }

        public Map<String, Object> options() {
            return http.options(path);
        }
    }
}
