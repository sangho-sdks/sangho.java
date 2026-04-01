package com.sangho.integration;

import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.ListResponse;
import com.sangho.model.Transaction;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class TransactionsIntegrationTest extends IntegrationTestBase {

    @Test
    void testListTransactions() {
        ListResponse<Transaction> result = client.transactions().list(
            Map.of("page_size", "5")
        );
        assertNotNull(result.results());
        assertTrue(result.results().size() <= 5);
    }

    @Test
    void testListTransactionsOrderedByDate() {
        ListResponse<Transaction> result = client.transactions().list(
            Map.of("ordering", "-created_at", "page_size", "10")
        );
        assertNotNull(result.results());
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.transactions().retrieve("trans_doesnotexist000"));
    }
}
