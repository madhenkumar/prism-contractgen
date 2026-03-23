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

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPact_success(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Data with ID 1 exists")
                .uponReceiving("A request for data 1")
                    .path("/data/1")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\"id\": 1, \"name\": \"Alice\", \"email\": \"alice@example.com\"}")
                .toPact();
    }

    @Test
    void testGetData_success(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/1", String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPact_badRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Invalid data")
                .uponReceiving("A request for invalid data")
                    .path("/data/invalid")
                    .method("GET")
                .willRespondWith()
                    .status(400)
                    .headers(headers)
                    .body("{\"error\": \"Invalid data\"}")
                .toPact();
    }

    @Test
    void testGetData_badRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/invalid", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPact_notFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("No data with ID 2 exists")
                .uponReceiving("A request for non-existent data")
                    .path("/data/2")
                    .method("GET")
                .willRespondWith()
                    .status(404)
                    .headers(headers)
                    .body("{\"error\": \"Data not found\"}")
                .toPact();
    }

    @Test
    void testGetData_notFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/2", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPact_unauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Invalid credentials")
                .uponReceiving("A request with invalid credentials")
                    .path("/data/1")
                    .method("GET")
                .willRespondWith()
                    .status(401)
                    .headers(headers)
                    .body("{\"error\": \"Unauthorized\"}")
                .toPact();
    }

    @Test
    void testGetData_unauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/1", String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPact_serverError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Server error")
                .uponReceiving("A request that causes server error")
                    .path("/data/1")
                    .method("GET")
                .willRespondWith()
                    .status(500)
                    .headers(headers)
                    .body("{\"error\": \"Internal Server Error\"}")
                .toPact();
    }

    @Test
    void testGetData_serverError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/1", String.class);
        assertEquals(500, response.getStatusCode().value());
    }
}