package com.sangho;

import com.sangho.exception.*;
import com.sangho.model.Customer;
import com.sangho.model.ListResponse;
import com.sangho.param.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CustomersResourceTest {

    private MockWebServer server;
    private SanghoClient  client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = SanghoClient.builder()
            .apiKey("sk_test_abc123456789")
            .baseUrl(server.url("/").toString())
            .build();
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    @Test
    void testListCustomers() {
        server.enqueue(new MockResponse()
            .setBody("{\"count\":1,\"next\":null,\"previous\":null,\"data\":[{\"id\":\"cust_1\",\"email\":\"a@b.com\"}]}")
            .addHeader("Content-Type", "application/json"));

        ListResponse<Customer> result = client.customers().list(null);
        assertEquals(1, result.count());
        assertEquals("cust_1", result.data().get(0).id());
    }

    @Test
    void testRetrieveCustomer() {
        server.enqueue(new MockResponse()
            .setBody("{\"id\":\"cust_1\",\"email\":\"a@b.com\"}")
            .addHeader("Content-Type", "application/json"));

        Customer c = client.customers().retrieve("cust_1");
        assertEquals("cust_1", c.id());
    }

    @Test
    void testNotFoundThrows() {
        server.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("{\"message\":\"Not found\",\"code\":\"not_found\"}")
            .addHeader("Content-Type", "application/json"));

        assertThrows(SanghoNotFoundException.class,
            () -> client.customers().retrieve("bad_id"));
    }

    @Test
    void testValidationError() {
        server.enqueue(new MockResponse()
            .setResponseCode(422)
            .setBody("{\"message\":\"Invalid\",\"detail\":{\"email\":[\"Enter a valid email.\"]}}")
            .addHeader("Content-Type", "application/json"));

        SanghoValidationException ex = assertThrows(SanghoValidationException.class,
            () -> client.customers().create(
                CustomerCreateParams.builder().email("bad").name("Jean").build()));
        assertTrue(ex.getFieldErrors().containsKey("email"));
    }

    @Test
    void testPublicKeyThrowsOnWrite() {
        SanghoClient pubClient = SanghoClient.builder()
            .apiKey("pk_test_abc123456789")
            .baseUrl(server.url("/").toString())
            .build();
        assertThrows(SanghoPublicKeyException.class,
            () -> pubClient.customers().list(null));
    }

    @Test
    void testDeleteReturnsVoid() {
        server.enqueue(new MockResponse().setResponseCode(204));
        assertDoesNotThrow(() -> client.customers().delete("cust_1"));
    }
}
