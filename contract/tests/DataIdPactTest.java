import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataIdPactTest {

    public static String baseUrl;
    public static String providerName;
    public static String consumerName;
    public static String port;

    @BeforeAll
    public static void setup() {
        baseUrl = ConfigLoader.getBaseUrl();
        providerName = ConfigLoader.getProviderName();
        consumerName = ConfigLoader.getConsumerName();
        port = ConfigLoader.getPort();
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Data exists with ID 1")
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
    void testGetSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/1", String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("No data exists with ID 1")
                .uponReceiving("A request for data 1")
                    .path("/data/1")
                    .method("GET")
                .willRespondWith()
                    .status(400)
                    .headers(headers)
                    .body("{\"error\": \"Bad Request\"}")
                .toPact();
    }

    @Test
    void testGetBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/1", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactNotFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("No data exists with ID 2")
                .uponReceiving("A request for data 2")
                    .path("/data/2")
                    .method("GET")
                .willRespondWith()
                    .status(404)
                    .headers(headers)
                    .body("{\"error\": \"Not Found\"}")
                .toPact();
    }

    @Test
    void testGetNotFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/2", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactUnauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("No authentication exists for data 3")
                .uponReceiving("A request for data 3")
                    .path("/data/3")
                    .method("GET")
                .willRespondWith()
                    .status(401)
                    .headers(headers)
                    .body("{\"error\": \"Unauthorized\"}")
                .toPact();
    }

    @Test
    void testGetUnauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/3", String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Pact(consumer = "DataConsumer")
    public RequestResponsePact createPactServerError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Error occurred while retrieving data 4")
                .uponReceiving("A request for data 4")
                    .path("/data/4")
                    .method("GET")
                .willRespondWith()
                    .status(500)
                    .headers(headers)
                    .body("{\"error\": \"Internal Server Error\"}")
                .toPact();
    }

    @Test
    void testGetServerError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/data/4", String.class);
        assertEquals(500, response.getStatusCode().value());
    }
}