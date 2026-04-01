package com.sangho.integration;

import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.Customer;
import com.sangho.model.ListResponse;
import com.sangho.model.PaymentIntent;
import com.sangho.param.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class PaymentIntentsIntegrationTest extends IntegrationTestBase {

    private static Customer sharedCustomer;

    @BeforeAll
    static void createCustomer() {
        sharedCustomer = client.customers().create(
            CustomerCreateParams.builder()
                .email("pi-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@sangho-test.com")
                .name("PI Integration Customer")
                .build()
        );
    }

    @AfterAll
    static void cleanup() {
        if (sharedCustomer != null) {
            try { client.customers().delete(sharedCustomer.id()); } catch (Exception ignored) {}
        }
    }

    @Test
    void testCreatePaymentIntent() {
        PaymentIntent intent = client.paymentIntents().create(
            PaymentIntentCreateParams.builder()
                .amount(10_000)
                .customer(sharedCustomer.id())
                .build()
        );

        assertNotNull(intent.id());
        assertEquals(10_000, intent.amount());
        assertEquals(sharedCustomer.id(), intent.customer());
        assertNotNull(intent.status());

        client.paymentIntents().cancel(intent.id(), null);
    }

    @Test
    void testRetrievePaymentIntent() {
        PaymentIntent intent = client.paymentIntents().create(
            PaymentIntentCreateParams.builder()
                .amount(5_000)
                .customer(sharedCustomer.id())
                .build()
        );

        PaymentIntent retrieved = client.paymentIntents().retrieve(intent.id());
        assertEquals(intent.id(), retrieved.id());
        assertEquals(5_000, retrieved.amount());

        client.paymentIntents().cancel(intent.id(), null);
    }

    @Test
    void testListPaymentIntents() {
        ListResponse<PaymentIntent> result = client.paymentIntents().list(
            PaymentIntentListParams.builder().pageSize(5).build()
        );

        assertNotNull(result.results());
        assertTrue(result.results().size() <= 5);
    }

    @Test
    void testCancelPaymentIntent() {
        PaymentIntent intent = client.paymentIntents().create(
            PaymentIntentCreateParams.builder()
                .amount(2_500)
                .customer(sharedCustomer.id())
                .build()
        );

        PaymentIntent canceled = client.paymentIntents().cancel(intent.id(), null);
        assertEquals("canceled", canceled.status());
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.paymentIntents().retrieve("pay_doesnotexist000"));
    }
}
