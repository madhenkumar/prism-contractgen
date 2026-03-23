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
public class DataIdPactTest {

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

    @Pact(consumer = "DataIdConsumer")
    public RequestResponsePact createPactSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/{id}")
                .path("/data/{id}")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body("{"message": "success"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactSuccess")
    void testSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/{id}", String.class);
            fail("Expected exception but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(200, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataIdConsumer")
    public RequestResponsePact createPactBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/{id}/bad-request")
                .path("/data/{id}/bad-request")
                .method("GET")
                .willRespondWith()
                .status(400)
                .headers(headers)
                .body("{"error": "Bad Request"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactBadRequest")
    void testBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/{id}/bad-request", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataIdConsumer")
    public RequestResponsePact createPact401(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/{id}/401")
                .path("/data/{id}/401")
                .method("GET")
                .willRespondWith()
                .status(401)
                .headers(headers)
                .body("{"error": "Unauthorized"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPact401")
    void test401(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/{id}/401", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(401, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataIdConsumer")
    public RequestResponsePact createPact404(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/{id}/404")
                .path("/data/{id}/404")
                .method("GET")
                .willRespondWith()
                .status(404)
                .headers(headers)
                .body("{"error": "Not Found"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPact404")
    void test404(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/{id}/404", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(404, ex.getStatusCode().value());
        }
    }

    @Pact(consumer = "DataIdConsumer")
    public RequestResponsePact createPact500(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /data/{id}/500")
                .path("/data/{id}/500")
                .method("GET")
                .willRespondWith()
                .status(500)
                .headers(headers)
                .body("{"error": "Internal Server Error"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPact500")
    void test500(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/data/{id}/500", String.class);
            fail("Expected HttpServerErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpServerErrorException ex) {
            assertEquals(500, ex.getStatusCode().value());
        }
    }

}