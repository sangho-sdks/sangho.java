package com.sangho.integration;

import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.ListResponse;
import com.sangho.model.Product;
import com.sangho.param.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class ProductsIntegrationTest extends IntegrationTestBase {

    private static Product sharedProduct;

    @BeforeAll
    static void createSharedProduct() {
        sharedProduct = client.products().create(
            ProductCreateParams.builder()
                .name("Shared Product " + java.util.UUID.randomUUID().toString().substring(0, 6))
                .price(5000)
                .description("Integration test product")
                .build()
        );
    }

    @AfterAll
    static void deleteSharedProduct() {
        if (sharedProduct != null) {
            try { client.products().delete(sharedProduct.id()); } catch (Exception ignored) {}
        }
    }

    @Test
    void testCreateProduct() {
        Product product = client.products().create(
            ProductCreateParams.builder()
                .name(uniqueName("Product"))
                .price(12_000)
                .build()
        );
        assertNotNull(product.id());
        assertEquals(12_000, product.price());
        assertNotNull(product.createdAt());
        client.products().delete(product.id());
    }

    @Test
    void testRetrieveProduct() {
        Product retrieved = client.products().retrieve(sharedProduct.id());
        assertEquals(sharedProduct.id(), retrieved.id());
        assertEquals(sharedProduct.name(), retrieved.name());
    }

    @Test
    void testListProducts() {
        ListResponse<Product> result = client.products().list(
            ProductListParams.builder().pageSize(5).build()
        );
        assertNotNull(result.results());
        assertTrue(result.results().size() <= 5);
    }

    @Test
    void testUpdateProduct() {
        Product updated = client.products().update(
            sharedProduct.id(),
            ProductUpdateParams.builder().price(9999).build()
        );
        assertEquals(9999, updated.price());
    }

    @Test
    void testArchiveAndRestoreProduct() {
        Product product  = client.products().create(
            ProductCreateParams.builder().name(uniqueName("Archive")).price(1000).build()
        );
        Product archived = client.products().archive(product.id());
        assertNotNull(archived.status());

        Product restored = client.products().restore(product.id());
        assertEquals("active", restored.status());
        client.products().delete(product.id());
    }

    @Test
    void testDeleteProduct() {
        Product product = client.products().create(
            ProductCreateParams.builder().name(uniqueName("Del")).price(500).build()
        );
        assertDoesNotThrow(() -> client.products().delete(product.id()));
        assertThrows(SanghoNotFoundException.class,
            () -> client.products().retrieve(product.id()));
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.products().retrieve("prod_doesnotexist000"));
    }
}
