package consume.api.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.PactSpecVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "api-service", port = "8081", pactVersion = PactSpecVersion.V3)
public class DataPactTest {

    static String baseUrl;
    static String providerName;
    static String consumerName;
    static String port;

    @BeforeAll
    public static void setup() {
//        ConfigLoader loader = new ConfigLoader();
        baseUrl = "http://localhost:8081";
        providerName = "Producer";
        consumerName = "Consumer";
        port = "8081";
    }
    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/success")
                .path("/data/success")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body("{\"message\": \"success\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactSuccess")
    void testSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/success", String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/bad-request")
                .path("/data/bad-request")
                .method("GET")
                .willRespondWith()
                .status(400)
                .headers(headers)
                .body("{\"error\": \"Bad Request\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactBadRequest")
    void testBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/bad-request", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactUnauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/unauthorized")
                .path("/data/unauthorized")
                .method("GET")
                .willRespondWith()
                .status(401)
                .headers(headers)
                .body("{\"error\": \"Unauthorized\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactUnauthorized")
    void testUnauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/unauthorized", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(401, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactNotFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/not-found")
                .path("/data/not-found")
                .method("GET")
                .willRespondWith()
                .status(404)
                .headers(headers)
                .body("{\"error\": \"Not Found\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactNotFound")
    void testNotFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/not-found", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactServerError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/server-error")
                .path("/data/server-error")
                .method("GET")
                .willRespondWith()
                .status(500)
                .headers(headers)
                .body("{\"error\": \"Internal Server Error\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactServerError")
    void testServerError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/server-error", String.class);
            fail("Expected HttpServerErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpServerErrorException ex) {
            assertEquals(500, ex.getStatusCode().value());
        }
    }

}