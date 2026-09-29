package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/**
 * {@code client.terminal().readers()} — lecteurs de carte physiques ; {@code sessions()} — sessions de paiement en
 * personne ; {@code offline()} — synchronisation des transactions hors ligne.
 */
public class TerminalResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

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

    /** Lecteurs de carte. */
    public static class Readers {
        private static final String PATH = "/terminal/readers/";
        private final HttpClient http;

        Readers(HttpClient http) { this.http = http; }

        public ListResponse<Map<String, Object>> list(Map<String, String> params) {
            http.assertSecretKey("terminal.readers.list");
            return http.get(PATH, params, LIST_TYPE);
        }

        public Map<String, Object> retrieve(String id) {
            http.assertSecretKey("terminal.readers.retrieve");
            return http.get(PATH + id + "/", null, MAP_TYPE);
        }

        public Map<String, Object> create(Map<String, Object> payload) {
            http.assertSecretKey("terminal.readers.create");
            return http.post(PATH, payload, Map.class);
        }

        public Map<String, Object> update(String id, Map<String, Object> payload) {
            http.assertSecretKey("terminal.readers.update");
            return http.patch(PATH + id + "/", payload, Map.class);
        }

        /** Désactive le lecteur (suppression logique). */
        public void disable(String id) {
            http.assertSecretKey("terminal.readers.disable");
            http.delete(PATH + id + "/");
        }

        public Map<String, Object> refreshToken(String id) {
            http.assertSecretKey("terminal.readers.refreshToken");
            return http.post(PATH + id + "/refresh-token/", Map.of(), Map.class);
        }

        public Map<String, Object> heartbeat(String id) {
            http.assertSecretKey("terminal.readers.heartbeat");
            return http.post(PATH + id + "/heartbeat/", Map.of(), Map.class);
        }

        public Map<String, Object> options() {
            return http.options(PATH);
        }
    }

    /** Sessions de paiement. */
    public static class Sessions {
        private static final String PATH = "/terminal/sessions/";
        private final HttpClient http;

        Sessions(HttpClient http) { this.http = http; }

        public ListResponse<Map<String, Object>> list(Map<String, String> params) {
            http.assertSecretKey("terminal.sessions.list");
            return http.get(PATH, params, LIST_TYPE);
        }

        public Map<String, Object> retrieve(String id) {
            http.assertSecretKey("terminal.sessions.retrieve");
            return http.get(PATH + id + "/", null, MAP_TYPE);
        }

        public Map<String, Object> create(Map<String, Object> payload) {
            http.assertSecretKey("terminal.sessions.create");
            return http.post(PATH, payload, Map.class);
        }

        public Map<String, Object> presentPaymentMethod(String id, Map<String, Object> payload) {
            http.assertSecretKey("terminal.sessions.presentPaymentMethod");
            return http.post(PATH + id + "/present-payment-method/", payload, Map.class);
        }

        public Map<String, Object> pollStatus(String id) {
            http.assertSecretKey("terminal.sessions.pollStatus");
            return http.get(PATH + id + "/status/", null, MAP_TYPE);
        }

        public Map<String, Object> cancel(String id) {
            http.assertSecretKey("terminal.sessions.cancel");
            return http.post(PATH + id + "/cancel/", Map.of(), Map.class);
        }

        public Map<String, Object> options() {
            return http.options(PATH);
        }
    }

    /** Transactions hors ligne. */
    public static class Offline {
        private static final String PATH = "/terminal/offline/sync/";
        private final HttpClient http;

        Offline(HttpClient http) { this.http = http; }

        public Map<String, Object> sync(Map<String, Object> payload) {
            http.assertSecretKey("terminal.offline.sync");
            return http.post(PATH, payload, Map.class);
        }

        public ListResponse<Map<String, Object>> list(Map<String, String> params) {
            http.assertSecretKey("terminal.offline.list");
            return http.get(PATH, params, LIST_TYPE);
        }

        public Map<String, Object> options() {
            return http.options(PATH);
        }
    }
}
