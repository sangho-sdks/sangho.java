package com.sangho.resource;

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
        http.assertSecretKey("products.create");
        return http.post("/products/", params, Product.class);
    }

    public Product update(String id, ProductUpdateParams params) {
        http.assertSecretKey("products.update");
        return http.patch("/products/" + id + "/", params, Product.class);
    }

    public void delete(String id) {
        http.assertSecretKey("products.delete");
        http.delete("/products/" + id + "/");
    }

    public Product archive(String id) {
        http.assertSecretKey("products.archive");
        return http.post("/products/" + id + "/archive/", Map.of(), Product.class);
    }

    public Product restore(String id) {
        http.assertSecretKey("products.restore");
        return http.post("/products/" + id + "/restore/", Map.of(), Product.class);
    }

    public Map<String, Object> options() {
        return http.options("/products/");
    }
}
