package com.sangho.integration;

import com.sangho.exception.*;
import com.sangho.model.Customer;
import com.sangho.model.ListResponse;
import com.sangho.param.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomersIntegrationTest extends IntegrationTestBase {

    private static Customer sharedCustomer;

    @BeforeAll
    static void createSharedCustomer() {
        sharedCustomer = client.customers().create(
            CustomerCreateParams.builder()
                .email("shared-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@sangho-test.com")
                .name("Shared Integration Customer")
                .build()
        );
    }

    @AfterAll
    static void deleteSharedCustomer() {
        if (sharedCustomer != null) {
            try { client.customers().delete(sharedCustomer.id()); } catch (Exception ignored) {}
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void testCreateCustomer() {
        String email = uniqueEmail("create");
        Customer customer = client.customers().create(
            CustomerCreateParams.builder().email(email).name("Jean Ondo").build()
        );

        assertNotNull(customer.id());
        assertFalse(customer.id().isEmpty());
        assertEquals(email, customer.email());
        assertEquals("Jean Ondo", customer.name());
        assertNotNull(customer.createdAt());

        client.customers().delete(customer.id());
    }

    @Test
    @Order(2)
    void testRetrieveCustomer() {
        Customer retrieved = client.customers().retrieve(sharedCustomer.id());

        assertEquals(sharedCustomer.id(), retrieved.id());
        assertEquals(sharedCustomer.email(), retrieved.email());
    }

    @Test
    @Order(3)
    void testListCustomersPaginated() {
        ListResponse<Customer> result = client.customers().list(
            CustomerListParams.builder().pageSize(5).build()
        );

        assertNotNull(result);
        assertNotNull(result.data());
        assertTrue(result.data().size() <= 5);
        assertTrue(result.count() >= 0);
    }

    @Test
    @Order(4)
    void testListCustomersFilterByStatus() {
        ListResponse<Customer> result = client.customers().list(
            CustomerListParams.builder().status("active").pageSize(10).build()
        );

        result.data().forEach(c ->
            assertEquals("active", c.status(), "All results should have status=active")
        );
    }

    @Test
    @Order(5)
    void testListCustomersSearch() {
        ListResponse<Customer> result = client.customers().list(
            CustomerListParams.builder().search(sharedCustomer.email()).build()
        );

        boolean found = result.data().stream()
            .anyMatch(c -> c.id().equals(sharedCustomer.id()));
        assertTrue(found, "Shared customer should appear in search results");
    }

    @Test
    @Order(6)
    void testUpdateCustomer() {
        Customer updated = client.customers().update(
            sharedCustomer.id(),
            CustomerUpdateParams.builder().phone("+24177888888").build()
        );

        assertEquals(sharedCustomer.id(), updated.id());
        assertEquals("+24177888888", updated.phone());
    }

    @Test
    @Order(7)
    void testDeleteCustomer() {
        Customer temp = client.customers().create(
            CustomerCreateParams.builder()
                .email(uniqueEmail("del"))
                .name("À supprimer")
                .build()
        );

        assertDoesNotThrow(() -> client.customers().delete(temp.id()));

        assertThrows(SanghoNotFoundException.class,
            () -> client.customers().retrieve(temp.id()));
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        SanghoNotFoundException ex = assertThrows(SanghoNotFoundException.class,
            () -> client.customers().retrieve("cust_doesnotexist000"));
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    void testCreateDuplicateEmailThrowsValidation() {
        assertThrows(SanghoValidationException.class, () ->
            client.customers().create(
                CustomerCreateParams.builder()
                    .email(sharedCustomer.email())
                    .name("Duplicate")
                    .build()
            )
        );
    }

    @Test
    void testCreateInvalidEmailThrowsValidationWithFieldErrors() {
        SanghoValidationException ex = assertThrows(SanghoValidationException.class,
            () -> client.customers().create(
                CustomerCreateParams.builder().email("not-an-email").name("Bad").build()
            )
        );
        assertTrue(ex.getFieldErrors().containsKey("email"));
    }

    @Test
    void testPublicKeyBlockedOnList() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            pubClient != null, "SANGHO_TEST_PUBLIC_KEY not set"
        );
        assertThrows(SanghoPublicKeyException.class,
            () -> pubClient.customers().list(null));
    }

    @Test
    void testOptionsReturnsSchema() {
        Map<String, Object> schema = client.customers().options();
        assertNotNull(schema);
        assertFalse(schema.isEmpty());
    }
}
