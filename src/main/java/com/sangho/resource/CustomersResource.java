package com.sangho.resource;

import com.sangho.param.RequestOptions;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Customer;
import com.sangho.model.ListResponse;
import com.sangho.param.CustomerCreateParams;
import com.sangho.param.CustomerListParams;
import com.sangho.param.CustomerUpdateParams;

public class CustomersResource {

    private static final TypeReference<ListResponse<Customer>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public CustomersResource(HttpClient http) { this.http = http; }

    public ListResponse<Customer> list(CustomerListParams params) {
        http.assertSecretKey("customers.list");
        return http.get("/customers/", params != null ? params.toMap() : Map.of(), LIST_TYPE);
    }

    public Customer retrieve(String id) {
        http.assertSecretKey("customers.retrieve");
        return http.get("/customers/" + id + "/", null, Customer.class);
    }

    public Customer create(CustomerCreateParams params) {
        return create(params, null);
    }

    /** Comme {@link #create(CustomerCreateParams)}, avec une clé d'idempotence (rejeu sans doublon). */
    public Customer create(CustomerCreateParams params, RequestOptions options) {
        http.assertSecretKey("customers.create");
        return http.post("/customers/", params, Customer.class, options);
    }

    public Customer update(String id, CustomerUpdateParams params) {
        http.assertSecretKey("customers.update");
        return http.patch("/customers/" + id + "/", params, Customer.class);
    }

    public void delete(String id) {
        http.assertSecretKey("customers.delete");
        http.delete("/customers/" + id + "/");
    }

    public Map<String, Object> options() {
        return http.options("/customers/");
    }

    /**
     * Moyens de paiement d'un client : {@code GET /payment-methods/?customer=<id>} (la route
     * {@code /customers/{id}/payment-methods/} n'existe pas côté API).
     */
    public ListResponse<Map<String, Object>> listPaymentMethods(String id, Map<String, String> params) {
        http.assertSecretKey("customers.listPaymentMethods");
        Map<String, String> query = new java.util.LinkedHashMap<>(params != null ? params : Map.of());
        query.put("customer", id);
        TypeReference<ListResponse<Map<String, Object>>> ref = new TypeReference<>() {};
        return http.get("/payment-methods/", query, ref);
    }
}
