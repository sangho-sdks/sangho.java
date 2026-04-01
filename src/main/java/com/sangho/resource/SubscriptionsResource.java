package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Subscription;
import com.sangho.model.ListResponse;
import com.sangho.param.SubscriptionCreateParams;

import java.util.Map;

public class SubscriptionsResource {

    private static final TypeReference<ListResponse<Subscription>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public SubscriptionsResource(HttpClient http) { this.http = http; }

    public ListResponse<Subscription> list(Map<String, String> params) {
        http.assertSecretKey("subscriptions.list");
        return http.get("/subscriptions/", params, LIST_TYPE);
    }

    public Subscription retrieve(String id) {
        http.assertSecretKey("subscriptions.retrieve");
        return http.get("/subscriptions/" + id + "/", null, Subscription.class);
    }

    public Subscription create(SubscriptionCreateParams params) {
        http.assertSecretKey("subscriptions.create");
        return http.post("/subscriptions/", params, Subscription.class);
    }

    public Subscription update(String id, Map<String, Object> payload) {
        http.assertSecretKey("subscriptions.update");
        return http.patch("/subscriptions/" + id + "/", payload, Subscription.class);
    }

    public Subscription cancel(String id, Map<String, Object> payload) {
        http.assertSecretKey("subscriptions.cancel");
        return http.post("/subscriptions/" + id + "/cancel/", payload != null ? payload : Map.of(), Subscription.class);
    }

    public Subscription pause(String id) {
        http.assertSecretKey("subscriptions.pause");
        return http.post("/subscriptions/" + id + "/pause/", Map.of(), Subscription.class);
    }

    public Subscription resume(String id) {
        http.assertSecretKey("subscriptions.resume");
        return http.post("/subscriptions/" + id + "/resume/", Map.of(), Subscription.class);
    }

    public Map<String, Object> options() {
        return http.options("/subscriptions/");
    }
}
