package com.sangho.integration;

import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.ListResponse;
import com.sangho.model.Refund;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class RefundsIntegrationTest extends IntegrationTestBase {

    @Test
    void testListRefunds() {
        ListResponse<Refund> result = client.refunds().list(
            Map.of("page_size", "5")
        );
        assertNotNull(result.data());
        assertTrue(result.data().size() <= 5);
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.refunds().retrieve("ref_doesnotexist000"));
    }
}
