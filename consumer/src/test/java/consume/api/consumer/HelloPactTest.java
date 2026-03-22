package consume.api.consumer;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "api-service", port = "8081")
public class HelloPactTest {

    static String baseUrl;
    static String providerName;
    static String consumerName;
    static String port;

    @BeforeAll
    static void setup() {
        baseUrl = "http://localhost:8081";
        providerName = "Producer";
        consumerName = "Consumer";
        port = "8081";
    }

    @Pact(consumer = "HelloFrontend")
    public RequestResponsePact createPactSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("API service is up and running")
                .uponReceiving("A GET request to /hello")
                .path("/hello")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body("{\"service\": \"api-service\", \"message\": \"Hello from Pact\"}")
                .toPact();
    }

    @Test
    void testGetSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello", String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloFrontend")
    public RequestResponsePact createPactBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("API service is up and running")
                .uponReceiving("A GET request to /hello with bad request")
                .path("/hello/bad-request")
                .method("GET")
                .willRespondWith()
                .status(400)
                .headers(headers)
                .body("{\"error\": \"Bad Request\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactBadRequest")
    void testGetBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello/bad-request", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloFrontend")
    public RequestResponsePact createPactNotFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("API service is up and running")
                .uponReceiving("A GET request to /hello/not-found")
                .path("/hello/not-found")
                .method("GET")
                .willRespondWith()
                .status(404)
                .headers(headers)
                .body("{\"error\": \"Not Found\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactNotFound")
    void testGetNotFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello/not-found", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloFrontend")
    public RequestResponsePact createPactUnauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("API service is up and running")
                .uponReceiving("A GET request to /hello/unauthorized")
                .path("/hello/unauthorized")
                .method("GET")
                .willRespondWith()
                .status(401)
                .headers(headers)
                .body("{\"error\": \"Unauthorized\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactUnauthorized")
    void testGetUnauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello/unauthorized", String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloFrontend")
    public RequestResponsePact createPactServerError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("API service is up and running")
                .uponReceiving("A GET request to /hello/server-error")
                .path("/hello/server-error")
                .method("GET")
                .willRespondWith()
                .status(500)
                .headers(headers)
                .body("{\"error\": \"Internal Server Error\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactServerError")
    void testGetServerError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello/server-error", String.class);
        assertEquals(500, response.getStatusCode().value());
    }
}