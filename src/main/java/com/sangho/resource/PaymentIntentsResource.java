package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.PaymentIntent;
import com.sangho.model.ListResponse;
import com.sangho.param.*;

import java.util.Map;

public class PaymentIntentsResource {

    private static final TypeReference<ListResponse<PaymentIntent>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public PaymentIntentsResource(HttpClient http) { this.http = http; }

    public ListResponse<PaymentIntent> list(PaymentIntentListParams params) {
        http.assertSecretKey("paymentIntents.list");
        return http.get("/payment-intents/", params != null ? params.toMap() : Map.of(), LIST_TYPE);
    }

    public PaymentIntent retrieve(String id) {
        http.assertSecretKey("paymentIntents.retrieve");
        return http.get("/payment-intents/" + id + "/", null, PaymentIntent.class);
    }

    public PaymentIntent create(PaymentIntentCreateParams params) {
        http.assertSecretKey("paymentIntents.create");
        return http.post("/payment-intents/", params, PaymentIntent.class);
    }

    public PaymentIntent confirm(String id, Map<String, Object> payload) {
        http.assertSecretKey("paymentIntents.confirm");
        return http.post("/payment-intents/" + id + "/confirm/", payload != null ? payload : Map.of(), PaymentIntent.class);
    }

    public PaymentIntent capture(String id, Map<String, Object> payload) {
        http.assertSecretKey("paymentIntents.capture");
        return http.post("/payment-intents/" + id + "/capture/", payload != null ? payload : Map.of(), PaymentIntent.class);
    }

    public PaymentIntent cancel(String id, Map<String, Object> payload) {
        http.assertSecretKey("paymentIntents.cancel");
        return http.post("/payment-intents/" + id + "/cancel/", payload != null ? payload : Map.of(), PaymentIntent.class);
    }

    public Map<String, Object> options() {
        return http.options("/payment-intents/");
    }
}
