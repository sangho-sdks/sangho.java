package com.sangho.integration;

import com.sangho.SanghoClient;
import com.sangho.exception.SanghoAuthException;
import com.sangho.exception.SanghoPublicKeyException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void testValidSecretKeyAuthenticates() {
        var result = client.customers().list(null);
        assertNotNull(result.results());
    }

    @Test
    void testInvalidKeyRaisesAuthError() {
        SanghoClient bad = SanghoClient.builder()
            .apiKey("sk_test_invalidkeyXXXXXXXXXXXX")
            .baseUrl(baseUrl)
            .build();

        SanghoAuthException ex = assertThrows(SanghoAuthException.class,
            () -> bad.customers().list(null));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void testPublicKeyBlockedOnWrite() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            pubClient != null, "SANGHO_TEST_PUBLIC_KEY not set"
        );
        assertThrows(SanghoPublicKeyException.class,
            () -> pubClient.customers().list(null));
    }

    @Test
    void testInvalidPrefixThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> SanghoClient.builder().apiKey("bad_key_no_prefix").build());
    }

    @Test
    void testAllValidPrefixesAccepted() {
        String[] prefixes = {"sk_prod_", "sk_test_", "pk_prod_", "pk_test_"};
        for (String prefix : prefixes) {
            assertDoesNotThrow(() ->
                SanghoClient.builder()
                    .apiKey(prefix + "x".repeat(20))
                    .baseUrl(baseUrl)
                    .build()
            );
        }
    }
}
