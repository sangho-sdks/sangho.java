package com.sangho.resource;

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
        http.assertSecretKey("customers.create");
        return http.post("/customers/", params, Customer.class);
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

    public ListResponse<Map<String, Object>> listTransactions(String id, Map<String, String> params) {
        http.assertSecretKey("customers.listTransactions");
        TypeReference<ListResponse<Map<String, Object>>> ref = new TypeReference<>() {};
        return http.get("/customers/" + id + "/transactions/", params, ref);
    }
}
