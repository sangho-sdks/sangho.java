package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Invoice;
import com.sangho.model.ListResponse;
import com.sangho.param.InvoiceCreateParams;

import java.util.Map;

public class InvoicesResource {

    private static final TypeReference<ListResponse<Invoice>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public InvoicesResource(HttpClient http) { this.http = http; }

    public ListResponse<Invoice> list(Map<String, String> params) {
        http.assertSecretKey("invoices.list");
        return http.get("/invoices/", params, LIST_TYPE);
    }

    public Invoice retrieve(String id) {
        http.assertSecretKey("invoices.retrieve");
        return http.get("/invoices/" + id + "/", null, Invoice.class);
    }

    public Invoice create(InvoiceCreateParams params) {
        http.assertSecretKey("invoices.create");
        return http.post("/invoices/", params, Invoice.class);
    }

    public Invoice update(String id, Map<String, Object> payload) {
        http.assertSecretKey("invoices.update");
        return http.patch("/invoices/" + id + "/", payload, Invoice.class);
    }

    public void delete(String id) {
        http.assertSecretKey("invoices.delete");
        http.delete("/invoices/" + id + "/");
    }

    public Invoice pay(String id, Map<String, Object> payload) {
        http.assertSecretKey("invoices.pay");
        return http.post("/invoices/" + id + "/pay/", payload != null ? payload : Map.of(), Invoice.class);
    }

    public Invoice voidInvoice(String id) {
        http.assertSecretKey("invoices.void");
        return http.post("/invoices/" + id + "/void/", Map.of(), Invoice.class);
    }

    public Invoice markUncollectible(String id) {
        http.assertSecretKey("invoices.markUncollectible");
        return http.post("/invoices/" + id + "/mark-uncollectible/", Map.of(), Invoice.class);
    }

    public Invoice send(String id) {
        http.assertSecretKey("invoices.send");
        return http.post("/invoices/" + id + "/send/", Map.of(), Invoice.class);
    }

    /** URL signée et expirante du PDF : {@code url}, {@code expires_at}. */
    public Map<String, Object> getPdfUrl(String id) {
        http.assertSecretKey("invoices.getPdfUrl");
        return http.get("/invoices/" + id + "/pdf/", null, new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> options() {
        return http.options("/invoices/");
    }
}
