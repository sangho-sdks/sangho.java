package com.sangho.resource;

import com.sangho.param.RequestOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Product;
import com.sangho.model.ListResponse;
import com.sangho.param.*;

import java.util.Map;

public class ProductsResource {

    private static final TypeReference<ListResponse<Product>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public ProductsResource(HttpClient http) { this.http = http; }

    public ListResponse<Product> list(ProductListParams params) {
        http.assertSecretKey("products.list");
        return http.get("/products/", params != null ? params.toMap() : Map.of(), LIST_TYPE);
    }

    public Product retrieve(String id) {
        http.assertSecretKey("products.retrieve");
        return http.get("/products/" + id + "/", null, Product.class);
    }

    public Product create(ProductCreateParams params) {
        return create(params, null);
    }

    /** Comme {@link #create(ProductCreateParams)}, avec une clé d'idempotence (rejeu sans doublon). */
    public Product create(ProductCreateParams params, RequestOptions options) {
        http.assertSecretKey("products.create");
        return http.post("/products/", params, Product.class, options);
    }

    public Product update(String id, ProductUpdateParams params) {
        http.assertSecretKey("products.update");
        return http.patch("/products/" + id + "/", params, Product.class);
    }

    public void delete(String id) {
        http.assertSecretKey("products.delete");
        http.delete("/products/" + id + "/");
    }

    public Map<String, Object> options() {
        return http.options("/products/");
    }
}
