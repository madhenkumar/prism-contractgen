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

    @Pact(consumer = "DataService")
    public RequestResponsePact createPact_success(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data exists")
                .uponReceiving("A request for data")
                    .path("/data")
                    .method("POST")
                .willRespondWith()
                    .status(201)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Data created successfully\", \"data\": {\"id\": 1, \"item\": \"Item 1\", \"quantity\": 10}}")
                .toPact();
    }

    @Test
    void testPostData(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"service\": \"api-service\", \"message\": \"Data created successfully\", \"data\": {\"id\": 1, \"item\": \"Item 1\", \"quantity\": 10}}", String.class);
        assertEquals(201, response.getStatusCode().value());
    }

    @Pact(consumer = "DataService")
    public RequestResponsePact createPact_badRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data does not exist")
                .uponReceiving("A request for data")
                    .path("/data")
                    .method("POST")
                .willRespondWith()
                    .status(400)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Bad Request\"}")
                .toPact();
    }

    @Test
    void testPostData_badRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"service\": \"api-service\", \"message\": \"Bad Request\"}", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Pact(consumer = "DataService")
    public RequestResponsePact createPact_notFound(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data does not exist")
                .uponReceiving("A request for data")
                    .path("/data")
                    .method("POST")
                .willRespondWith()
                    .status(404)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Not Found\"}")
                .toPact();
    }

    @Test
    void testPostData_notFound(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"service\": \"api-service\", \"message\": \"Not Found\"}", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Pact(consumer = "DataService")
    public RequestResponsePact createPact_unauthorized(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data does not exist")
                .uponReceiving("A request for data")
                    .path("/data")
                    .method("POST")
                .willRespondWith()
                    .status(401)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Unauthorized\"}")
                .toPact();
    }

    @Test
    void testPostData_unauthorized(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"service\": \"api-service\", \"message\": \"Unauthorized\"}", String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Pact(consumer = "DataService")
    public RequestResponsePact createPact_serverError(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data does not exist")
                .uponReceiving("A request for data")
                    .path("/data")
                    .method("POST")
                .willRespondWith()
                    .status(500)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Internal Server Error\"}")
                .toPact();
    }

    @Test
    void testPostData_serverError(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(mockServer.getUrl() + "/data", "{\"service\": \"api-service\", \"message\": \"Internal Server Error\"}", String.class);
        assertEquals(500, response.getStatusCode().value());
    }
}