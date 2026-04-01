package com.sangho.integration;

import com.sangho.SanghoClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

/**
 * Classe de base pour les tests d'intégration Sangho Java SDK.
 *
 * Variables d'environnement requises :
 *   SANGHO_TEST_SECRET_KEY   sk_test_xxx
 *   SANGHO_TEST_PUBLIC_KEY   pk_test_xxx  (optionnel)
 *   SANGHO_API_BASE_URL      https://api.sangho.com/v1 (optionnel)
 *
 * Lancement :
 *   SANGHO_TEST_SECRET_KEY=sk_test_xxx mvn test -Dgroups=integration
 *   # ou via Makefile :
 *   make test-integration
 */
@Tag("integration")
public abstract class IntegrationTestBase {

    protected static SanghoClient client;
    protected static SanghoClient pubClient;
    protected static String baseUrl;

    @BeforeAll
    static void setUpIntegration() {
        String secretKey = System.getenv("SANGHO_TEST_SECRET_KEY");
        String publicKey = System.getenv("SANGHO_TEST_PUBLIC_KEY");

        baseUrl = System.getenv().getOrDefault("SANGHO_API_BASE_URL", "https://api.sangho.com/v1");

        org.junit.jupiter.api.Assumptions.assumeTrue(
            secretKey != null && !secretKey.isBlank(),
            "SANGHO_TEST_SECRET_KEY not set — integration tests skipped"
        );

        client = SanghoClient.builder()
            .apiKey(secretKey)
            .baseUrl(baseUrl)
            .build();

        if (publicKey != null && !publicKey.isBlank()) {
            pubClient = SanghoClient.builder()
                .apiKey(publicKey)
                .baseUrl(baseUrl)
                .build();
        }
    }

    protected String uniqueEmail(String prefix) {
        return prefix + "-" + java.util.UUID.randomUUID().toString().substring(0, 8) + "@sangho-test.com";
    }

    protected String uniqueName(String prefix) {
        return prefix + " " + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
