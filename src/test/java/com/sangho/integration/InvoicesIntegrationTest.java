package com.sangho.integration;

import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.Customer;
import com.sangho.model.Invoice;
import com.sangho.model.ListResponse;
import com.sangho.param.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class InvoicesIntegrationTest extends IntegrationTestBase {

    private static Customer sharedCustomer;

    @BeforeAll
    static void createCustomer() {
        sharedCustomer = client.customers().create(
            CustomerCreateParams.builder()
                .email("inv-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@sangho-test.com")
                .name("Invoice Integration Customer")
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
    void testCreateInvoice() {
        Invoice invoice = client.invoices().create(
            InvoiceCreateParams.builder()
                .customer(sharedCustomer.id())
                .amount(10_000)
                .build()
        );
        assertNotNull(invoice.id());
        assertEquals(10_000, invoice.amount());
        assertEquals(sharedCustomer.id(), invoice.customer());
        client.invoices().delete(invoice.id());
    }

    @Test
    void testRetrieveInvoice() {
        Invoice invoice   = client.invoices().create(
            InvoiceCreateParams.builder().customer(sharedCustomer.id()).amount(5_000).build()
        );
        Invoice retrieved = client.invoices().retrieve(invoice.id());
        assertEquals(invoice.id(), retrieved.id());
        client.invoices().delete(invoice.id());
    }

    @Test
    void testListInvoices() {
        ListResponse<Invoice> result = client.invoices().list(
            Map.of("page_size", "5")
        );
        assertNotNull(result.results());
    }

    @Test
    void testFinalizeAndVoidInvoice() {
        Invoice invoice   = client.invoices().create(
            InvoiceCreateParams.builder().customer(sharedCustomer.id()).amount(3_000).build()
        );
        Invoice finalized = client.invoices().finalize(invoice.id());
        assertNotNull(finalized.status());

        Invoice voided = client.invoices().voidInvoice(finalized.id());
        assertEquals("void", voided.status());
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.invoices().retrieve("inv_doesnotexist000"));
    }
}
