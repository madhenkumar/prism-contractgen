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

public class HelloPactTest {

    static String baseUrl;
    static String providerName;
    static String consumerName;
    static String port;

    @BeforeAll
    static void setup() {
        baseUrl = ConfigLoader.getBaseUrl();
        providerName = ConfigLoader.getProviderName();
        consumerName = ConfigLoader.getConsumerName();
        port = ConfigLoader.getPort();
    }

    @Pact(consumer = "HelloService")
    public RequestResponsePact createPactGetSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Hello Service exists")
                .uponReceiving("A request to get hello message")
                    .path("/hello")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Hello, World!\"}")
                .toPact();
    }

    @Test
    void testGetHelloSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello", String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloService")
    public RequestResponsePact createPactGetBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Hello Service exists")
                .uponReceiving("A request to get hello message with bad request")
                    .path("/hello")
                    .method("POST")
                    .body("{\"service\": \"api-service\", \"message\": \"Hello, World!\"}")
                .willRespondWith()
                    .status(400)
                    .headers(headers)
                    .body("{\"error\": \"Bad Request\"}")
                .toPact();
    }

    @Test
    void testGetHelloBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/hello", "{\"service\": \"api-service\", \"message\": \"Hello, World!\"}", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloService")
    public RequestResponsePact createPactGetNotFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Hello Service exists")
                .uponReceiving("A request to get hello message not found")
                    .path("/hello/not-found")
                    .method("GET")
                .willRespondWith()
                    .status(404)
                    .headers(headers)
                    .body("{\"error\": \"Not Found\"}")
                .toPact();
    }

    @Test
    void testGetHelloNotFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello/not-found", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloService")
    public RequestResponsePact createPactGetUnauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Hello Service exists")
                .uponReceiving("A request to get hello message unauthorized")
                    .path("/hello")
                    .method("GET")
                    .headers(headers)
                .willRespondWith()
                    .status(401)
                    .headers(headers)
                    .body("{\"error\": \"Unauthorized\"}")
                .toPact();
    }

    @Test
    void testGetHelloUnauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello", String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Pact(consumer = "HelloService")
    public RequestResponsePact createPactGetServerError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Hello Service exists")
                .uponReceiving("A request to get hello message server error")
                    .path("/hello")
                    .method("GET")
                .willRespondWith()
                    .status(500)
                    .headers(headers)
                    .body("{\"error\": \"Server Error\"}")
                .toPact();
    }

    @Test
    void testGetHelloServerError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/hello", String.class);
        assertEquals(500, response.getStatusCode().value());
    }
}