package com.sangho;

import com.sangho.exception.*;
import com.sangho.param.RequestOptions;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Paiements Connect (séquestre) : comptes, KYC, soldes, retraits, release / refund / freeze, erreurs typées. */
class ConnectTest {

    private static final String ACCT = "acct_" + "a".repeat(32);
    private static final String PAY = "cpay_" + "b".repeat(32);

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

    private void json(int status, String body) {
        server.enqueue(new MockResponse().setResponseCode(status).setBody(body).addHeader("Content-Type", "application/json"));
    }

    @Test
    void createAccountSendsExternalIdAndIdempotencyKey() throws Exception {
        json(200, "{\"id\":\"" + ACCT + "\",\"claim_token\":\"ct_1\"}");
        Map<String, Object> res = client.connect().accounts()
            .create("seller-1", "a@b.ga", null, RequestOptions.idempotencyKey("acct-seller-1"));
        assertEquals("ct_1", res.get("claim_token"));
        RecordedRequest req = server.takeRequest();
        assertEquals("POST /connect/accounts/", req.getMethod() + " " + req.getPath());
        assertEquals("acct-seller-1", req.getHeader("Idempotency-Key"));
        assertTrue(req.getBody().readUtf8().contains("\"external_id\":\"seller-1\""));
    }

    @Test
    void readRoutesMatchTheBackend() throws Exception {
        for (int i = 0; i < 4; i++) json(200, "{\"id\":\"x\"}");
        client.connect().accounts().retrieve(ACCT);
        client.connect().accounts().list();
        client.connect().accounts().balance(ACCT);
        client.connect().accounts().listPayouts(ACCT);
        assertEquals("/connect/accounts/" + ACCT + "/", server.takeRequest().getPath());
        assertEquals("/connect/accounts/", server.takeRequest().getPath());
        assertEquals("/connect/accounts/" + ACCT + "/balance/", server.takeRequest().getPath());
        assertEquals("/connect/accounts/" + ACCT + "/payouts/", server.takeRequest().getPath());
    }

    @Test
    void kycSessionSendsReturnAndRefreshUrls() throws Exception {
        json(200, "{\"url\":\"https://kyc\",\"account\":\"" + ACCT + "\"}");
        client.connect().accounts().createKycSession(ACCT, "https://x/ok", "https://x/retry", null);
        RecordedRequest req = server.takeRequest();
        assertEquals("/connect/accounts/" + ACCT + "/kyc-session/", req.getPath());
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"return_url\":\"https://x/ok\"") && body.contains("\"refresh_url\":\"https://x/retry\""));
    }

    @Test
    void payoutRequiresAnIdempotencyKeyBeforeAnyNetworkCall() {
        assertThrows(SanghoValidationException.class,
            () -> client.connect().accounts().createPayout(ACCT, 1000, "pm_1", ""));
        assertEquals(0, server.getRequestCount());
    }

    @Test
    void payoutSendsAmountDestinationAndKey() throws Exception {
        json(200, "{\"id\":\"po_1\"}");
        client.connect().accounts().createPayout(ACCT, "1000", "pm_1", "payout-1");
        RecordedRequest req = server.takeRequest();
        assertEquals("payout-1", req.getHeader("Idempotency-Key"));
        assertEquals("{\"amount\":\"1000\",\"destination\":\"pm_1\"}", req.getBody().readUtf8());
    }

    @Test
    void releaseSendsTheKeyAndRequiresIt() throws Exception {
        json(200, "{\"status\":\"released\"}");
        client.connect().payments().release(PAY, "release-ORDER-1");
        RecordedRequest req = server.takeRequest();
        assertEquals("/connect/payments/" + PAY + "/release/", req.getPath());
        assertEquals("release-ORDER-1", req.getHeader("Idempotency-Key"));
        assertThrows(SanghoValidationException.class, () -> client.connect().payments().release(PAY, null));
    }

    @Test
    void refundSendsScopeAmountAndReason() throws Exception {
        json(200, "{\"status\":\"refunded\"}");
        client.connect().payments().refund(PAY, "amount", "refund-1", 500, "retour");
        assertEquals("{\"scope\":\"amount\",\"amount\":500,\"reason\":\"retour\"}", server.takeRequest().getBody().readUtf8());
    }

    @Test
    void refundRejectsUnknownScopeWithoutNetworkCall() {
        assertThrows(SanghoValidationException.class,
            () -> client.connect().payments().refund(PAY, "oups", "k", null, null));
        assertEquals(0, server.getRequestCount());
    }

    @Test
    void freezeUnfreezeRetrieveAndSimulatePayment() throws Exception {
        for (int i = 0; i < 4; i++) json(200, "{\"id\":\"x\"}");
        client.connect().payments().freeze(PAY, null);
        client.connect().payments().unfreeze(PAY, null);
        client.connect().payments().retrieve(PAY);
        client.connect().payments().simulatePayment(PAY);
        assertEquals("POST /connect/payments/" + PAY + "/freeze/", label(server.takeRequest()));
        assertEquals("POST /connect/payments/" + PAY + "/unfreeze/", label(server.takeRequest()));
        assertEquals("GET /connect/payments/" + PAY + "/", label(server.takeRequest()));
        assertEquals("POST /connect/payments/" + PAY + "/simulate-payment/", label(server.takeRequest()));
    }

    @Test
    void publicKeyIsRefused() {
        SanghoClient pub = SanghoClient.builder().apiKey("pk_test_abc123456789").baseUrl(server.url("/").toString()).build();
        assertThrows(SanghoPublicKeyException.class, () -> pub.connect().payments().retrieve(PAY));
    }

    @Test
    void platformPartnerRequiredIsTyped() {
        json(403, "{\"error\":{\"code\":\"platform_partner_required\",\"message\":\"Réservé\"}}");
        SanghoPlatformPartnerRequiredException e =
            assertThrows(SanghoPlatformPartnerRequiredException.class, () -> client.connect().accounts().list());
        assertEquals("platform_partner_required", e.getCode());
        assertEquals("Réservé", e.getMessage());
    }

    @Test
    void businessConflictIsNotAnIdempotencyError() {
        json(409, "{\"error\":{\"code\":\"account_not_claimed\",\"message\":\"Non réclamé\"}}");
        SanghoConflictException e = assertThrows(SanghoConflictException.class,
            () -> client.connect().accounts().createKycSession(ACCT, "https://x", null, null));
        assertEquals("account_not_claimed", e.getCode());
    }

    @Test
    void idempotencyConflictStaysAnIdempotencyError() {
        json(409, "{\"code\":\"idempotency_conflict\"}");
        assertThrows(SanghoIdempotencyException.class, () -> client.connect().accounts().list());
    }

    private static String label(RecordedRequest r) { return r.getMethod() + " " + r.getPath(); }

    @SuppressWarnings("unused")
    private static List<String> unused() { return List.of(); }
}
