package com.sangho.integration;

import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.ListResponse;
import com.sangho.model.Subscription;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class SubscriptionsIntegrationTest extends IntegrationTestBase {

    @Test
    void testListSubscriptions() {
        ListResponse<Subscription> result = client.subscriptions().list(
            Map.of("page_size", "5")
        );
        assertNotNull(result.results());
        assertTrue(result.results().size() <= 5);
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.subscriptions().retrieve("sub_doesnotexist000"));
    }
}
