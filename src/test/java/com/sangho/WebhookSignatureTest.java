package com.sangho;

import com.sangho.exception.SanghoException;
import com.sangho.exception.SanghoWebhookSignatureException;
import com.sangho.resource.WebhooksResource;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Signature des webhooks : erreurs typées, rotation, tolérance, corps brut. */
class WebhookSignatureTest {

    private static final String SECRET = "whsec_test_secret";
    private static final String BODY = "{\"id\":\"evt_1\",\"type\":\"kyc.updated\",\"created\":1780000100,"
        + "\"data\":{\"object\":{\"status\":\"active\",\"charges_enabled\":true}}}";

    private static String sign(String secret, long ts, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal((ts + "." + payload).getBytes(StandardCharsets.UTF_8)));
    }

    private static SanghoWebhookSignatureException failure(String header, String payload) {
        return assertThrows(SanghoWebhookSignatureException.class,
            () -> WebhooksResource.constructEvent(payload, header, SECRET));
    }

    @Test
    void validSignatureReturnsTheEvent() throws Exception {
        long ts = Instant.now().getEpochSecond();
        Map<String, Object> event = WebhooksResource.constructEvent(BODY, "t=" + ts + ",v1=" + sign(SECRET, ts, BODY), SECRET);
        assertEquals("kyc.updated", event.get("type"));
    }

    @Test
    void generateTestHeaderRoundTrips() {
        String header = WebhooksResource.generateTestHeader(BODY, SECRET);
        assertEquals("kyc.updated", WebhooksResource.constructEvent(BODY, header, SECRET).get("type"));
        assertTrue(WebhooksResource.generateTestHeader("x", SECRET, 1780000000L).matches("t=1780000000,v1=[0-9a-f]{64}"));
    }

    @Test
    void utf8BodyIsVerifiedOnRawBytes() {
        String payload = "{\"type\":\"account.updated\",\"data\":{\"object\":{\"business_name\":\"Café Épicé\"}}}";
        String header = WebhooksResource.generateTestHeader(payload, SECRET);
        assertEquals("account.updated", WebhooksResource.constructEvent(payload, header, SECRET).get("type"));
    }

    @Test
    void alteredBodyIsAMismatchWith401() throws Exception {
        long ts = Instant.now().getEpochSecond();
        SanghoWebhookSignatureException e = failure("t=" + ts + ",v1=" + sign(SECRET, ts, BODY), BODY + " ");
        assertEquals("mismatch", e.getReason());
        assertEquals(401, e.getStatusCode());
        assertEquals("invalid_signature", e.getCode());
    }

    @Test
    void wrongSecretIsAMismatch() throws Exception {
        long ts = Instant.now().getEpochSecond();
        assertEquals("mismatch", failure("t=" + ts + ",v1=" + sign("autre", ts, BODY), BODY).getReason());
    }

    @Test
    void oldOrFutureTimestampIsExpired() throws Exception {
        for (long ts : new long[] {Instant.now().getEpochSecond() - 3600, Instant.now().getEpochSecond() + 3600}) {
            SanghoWebhookSignatureException e = failure("t=" + ts + ",v1=" + sign(SECRET, ts, BODY), BODY);
            assertEquals("expired", e.getReason());
            assertEquals(400, e.getStatusCode());
            assertEquals("stale_event", e.getCode());
        }
    }

    @Test
    void toleranceIsHonoured() throws Exception {
        long ts = Instant.now().getEpochSecond() - 3600;
        Map<String, Object> event = WebhooksResource.constructEvent(
            BODY.getBytes(StandardCharsets.UTF_8), "t=" + ts + ",v1=" + sign(SECRET, ts, BODY), SECRET, 7200);
        assertEquals("evt_1", event.get("id"));
    }

    @Test
    void unreadableHeadersAreMalformed() {
        for (String header : new String[] {"", "n'importe quoi", "t=abc,v1=deadbeef", "t=" + Instant.now().getEpochSecond(), "v1=deadbeef"}) {
            SanghoWebhookSignatureException e = failure(header, BODY);
            assertEquals("malformed", e.getReason());
            assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    void rotationAcceptsSeveralV1AndSeveralSecrets() throws Exception {
        long ts = Instant.now().getEpochSecond();
        String header = "t=" + ts + ",v1=" + sign("ancien", ts, BODY) + ",v1=" + sign(SECRET, ts, BODY);
        assertEquals("evt_1", WebhooksResource.constructEvent(BODY, header, SECRET).get("id"));
        String header2 = "t=" + ts + ",v1=" + sign("nouveau", ts, BODY);
        assertEquals("evt_1", WebhooksResource.constructEvent(
            BODY.getBytes(StandardCharsets.UTF_8), header2, List.of(SECRET, "nouveau"), 300).get("id"));
    }

    @Test
    void signedButNonJsonBodyIsAnExplicitError() {
        String payload = "pas du json";
        String header = WebhooksResource.generateTestHeader(payload, SECRET);
        SanghoException e = assertThrows(SanghoException.class, () -> WebhooksResource.constructEvent(payload, header, SECRET));
        assertTrue(e.getMessage().contains("not valid JSON"));
    }

    @Test
    void remainsASanghoException() {
        assertTrue(SanghoException.class.isAssignableFrom(SanghoWebhookSignatureException.class));
    }
}
