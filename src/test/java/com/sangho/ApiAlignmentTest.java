package com.sangho;

import com.sangho.exception.*;
import com.sangho.http.SdkVersion;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Alignement avec l'API Sangho réelle (backend/api) : chaque route testée existe côté backend ; les méthodes retirées
 * appelaient des routes inexistantes (404/405) et ne doivent pas revenir.
 */
class ApiAlignmentTest {

    private MockWebServer server;
    private SanghoClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = SanghoClient.builder().apiKey("sk_test_abc123456789").baseUrl(server.url("/").toString()).maxRetries(0).build();
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private void ok(String body) {
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));
    }

    private void expect(String method, String path, Consumer<SanghoClient> call) throws InterruptedException {
        ok("{\"id\":\"x\",\"data\":[]}");
        call.accept(client);
        RecordedRequest req = server.takeRequest();
        assertEquals(method + " " + path, req.getMethod() + " " + req.getPath());
    }

    @Test
    void routesMatchTheBackend() throws InterruptedException {
        expect("GET", "/account/", c -> c.account().retrieve());
        expect("GET", "/addresses/", c -> c.addresses().list(null));
        expect("POST", "/addresses/", c -> c.addresses().create(Map.of("line1", "x")));
        expect("GET", "/apps/app_1/keys/", c -> c.apps().keys("app_1"));
        expect("POST", "/checkout-sessions/cs_1/expire/", c -> c.checkoutSessions().expire("cs_1"));
        expect("GET", "/invoices/inv_1/pdf/", c -> c.invoices().getPdfUrl("inv_1"));
        expect("PATCH", "/payment-intents/pi_1/", c -> c.paymentIntents().update("pi_1", Map.of("description", "d")));
        expect("DELETE", "/payment-intents/pi_1/", c -> c.paymentIntents().delete("pi_1"));
        expect("POST", "/payment-links/pl_1/archive/", c -> c.paymentLinks().archive("pl_1"));
        expect("POST", "/payment-links/pl_1/restore/", c -> c.paymentLinks().restore("pl_1"));
        expect("POST", "/payment-methods/pm_1/attach/", c -> c.paymentMethods().attach("pm_1", "cus_1"));
        expect("POST", "/payment-methods/pm_1/detach/", c -> c.paymentMethods().detach("pm_1"));
        expect("POST", "/payment-methods/pm_1/set-default/", c -> c.paymentMethods().setDefault("pm_1"));
        expect("GET", "/receipts/rcp_1/pdf/", c -> c.receipts().getPdfUrl("rcp_1"));
        expect("POST", "/reset/", c -> c.sandbox().reset());
        expect("GET", "/security/me/", c -> c.security().retrieve());
        expect("PATCH", "/security/update_me/", c -> c.security().update(Map.of("allowed_ips", List.of())));
        expect("POST", "/subscriptions/sub_1/reactivate/", c -> c.subscriptions().reactivate("sub_1"));
        expect("PATCH", "/transactions/tx_1/", c -> c.transactions().update("tx_1", Map.of("description", "d")));
        expect("POST", "/transactions/tx_1/cancel/", c -> c.transactions().cancel("tx_1"));
        expect("POST", "/webhooks/wh_1/enable/", c -> c.webhooks().enable("wh_1"));
        expect("POST", "/webhooks/wh_1/disable/", c -> c.webhooks().disable("wh_1"));
        expect("GET", "/webhooks/wh_1/deliveries/", c -> c.webhooks().listDeliveries("wh_1", null));
        expect("GET", "/webhooks/wh_1/deliveries/d_1/", c -> c.webhooks().retrieveDelivery("wh_1", "d_1"));
        expect("POST", "/terminal/readers/r_1/heartbeat/", c -> c.terminal().readers().heartbeat("r_1"));
        expect("POST", "/terminal/readers/r_1/refresh-token/", c -> c.terminal().readers().refreshToken("r_1"));
        expect("GET", "/terminal/sessions/s_1/status/", c -> c.terminal().sessions().pollStatus("s_1"));
        expect("POST", "/terminal/sessions/s_1/present-payment-method/",
            c -> c.terminal().sessions().presentPaymentMethod("s_1", Map.of("card", "x")));
        expect("POST", "/terminal/offline/sync/", c -> c.terminal().offline().sync(Map.of("transactions", List.of())));
    }

    @Test
    void customerPaymentMethodsUseTheCustomerFilter() throws InterruptedException {
        ok("{\"count\":0,\"data\":[]}");
        client.customers().listPaymentMethods("cus_1", null);
        RecordedRequest req = server.takeRequest();
        assertEquals("GET /payment-methods/?customer=cus_1", req.getMethod() + " " + req.getPath());
    }

    @Test
    void checkoutRetrieveAcceptsAPublicKey() {
        SanghoClient pub = SanghoClient.builder().apiKey("pk_test_abc123456789").baseUrl(server.url("/").toString()).build();
        ok("{\"id\":\"cs_1\"}");
        assertDoesNotThrow(() -> pub.checkoutSessions().retrieve("cs_1"));
        assertThrows(SanghoPublicKeyException.class, () -> pub.webhooks().list(null));
    }

    @Test
    void sendsTheSdkVersionAndDefaultsToTheGaHost() throws InterruptedException {
        assertEquals("https://api.sangho.ga/v1", SanghoClient.DEFAULT_BASE_URL);
        ok("{}");
        client.account().retrieve();
        RecordedRequest req = server.takeRequest();
        assertEquals("java/" + SdkVersion.VERSION, req.getHeader("X-Sangho-SDK"));
        assertEquals("sandbox", req.getHeader("X-Sangho-Environment"));
    }

    @Test
    void sdkVersionMatchesThePom() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        assertTrue(pom.contains("<artifactId>sangho-java</artifactId>\n    <version>" + SdkVersion.VERSION + "</version>"));
    }

    @Test
    void phantomEndpointsAreGone() {
        record Phantom(Class<?> type, String method) {}
        List<Phantom> gone = List.of(
            new Phantom(com.sangho.resource.CustomersResource.class, "listTransactions"),
            new Phantom(com.sangho.resource.InvoicesResource.class, "finalize"),
            new Phantom(com.sangho.resource.ProductsResource.class, "archive"),
            new Phantom(com.sangho.resource.ProductsResource.class, "restore"),
            new Phantom(com.sangho.resource.PartnersResource.class, "create"),
            new Phantom(com.sangho.resource.PartnersResource.class, "update"),
            new Phantom(com.sangho.resource.PartnersResource.class, "delete"),
            new Phantom(com.sangho.resource.PaymentMethodsResource.class, "create"),
            new Phantom(com.sangho.resource.PaymentMethodsResource.class, "update"),
            new Phantom(com.sangho.resource.PaymentMethodsResource.class, "delete"),
            new Phantom(com.sangho.resource.ReceiptsResource.class, "create"),
            new Phantom(com.sangho.resource.ReceiptsResource.class, "update"),
            new Phantom(com.sangho.resource.ReceiptsResource.class, "delete"),
            new Phantom(com.sangho.resource.CheckoutSessionsResource.class, "update"),
            new Phantom(com.sangho.resource.SecurityResource.class, "create"),
            new Phantom(com.sangho.resource.SecurityResource.class, "delete"));
        for (Phantom p : gone) {
            for (Method m : p.type().getDeclaredMethods()) {
                assertNotEquals(p.method(), m.getName(), p.type().getSimpleName() + "." + p.method() + " ne doit plus exister");
            }
        }
    }
}
