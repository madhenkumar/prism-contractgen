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
public class DataPactTest {

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

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactPOSTSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("System is up and running")
                .uponReceiving("A POST request to /data")
                    .path("/data")
                    .method("POST")
                    .body("{\"item\": \"Item 1\", \"quantity\": 1}")
                .willRespondWith()
                    .status(201)
                    .headers(headers)
                    .body("{\"service\": \"" + providerName + "\", \"message\": \"Data created successfully\", \"data\": {\"item\": \"Item 1\", \"quantity\": 1}}")
                .toPact();
    }

    @Test
    void testPOSTSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"item\": \"Item 1\", \"quantity\": 1}", String.class);
        assertEquals(201, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactPOSTBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("System is up and running")
                .uponReceiving("A POST request to /data with invalid data")
                    .path("/data")
                    .method("POST")
                    .body("{\"item\": \"Item 1\"}")
                .willRespondWith()
                    .status(400)
                    .headers(headers)
                    .body("{\"service\": \"" + providerName + "\", \"message\": \"Bad request\", \"errors\": {\"quantity\": \"Quantity is required\"}}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactPOSTBadRequest")
    void testPOSTBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"item\": \"Item 1\"}", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactPOSTNotFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("System is up and running")
                .uponReceiving("A POST request to /data with invalid data")
                    .path("/data")
                    .method("POST")
                    .body("{\"item\": \"Item 1\"}")
                .willRespondWith()
                    .status(404)
                    .headers(headers)
                    .body("{\"service\": \"" + providerName + "\", \"message\": \"Not found\", \"errors\": {\"data\": \"Data not found\"}}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactPOSTNotFound")
    void testPOSTNotFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"item\": \"Item 1\"}", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactPOSTUnauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("System is up and running")
                .uponReceiving("A POST request to /data with invalid credentials")
                    .path("/data")
                    .method("POST")
                    .headers(headers)
                .willRespondWith()
                    .status(401)
                    .headers(headers)
                    .body("{\"service\": \"" + providerName + "\", \"message\": \"Unauthorized\", \"errors\": {\"credentials\": \"Invalid credentials\"}}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactPOSTUnauthorized")
    void testPOSTUnauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"item\": \"Item 1\"}", String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactPOSTServerError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("System is up and running")
                .uponReceiving("A POST request to /data with invalid data")
                    .path("/data")
                    .method("POST")
                    .body("{\"item\": \"Item 1\"}")
                .willRespondWith()
                    .status(500)
                    .headers(headers)
                    .body("{\"service\": \"" + providerName + "\", \"message\": \"Server error\", \"errors\": {\"system\": \"Internal server error\"}}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactPOSTServerError")
    void testPOSTServerError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"item\": \"Item 1\"}", String.class);
        assertEquals(500, response.getStatusCode().value());
    }
}