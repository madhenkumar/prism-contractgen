import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.*;
import au.com.dius.pact.core.model.RequestResponsePact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "api-service", port = "8081")
public class DataPactTest {

    @Pact(consumer = "api-client")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data item and quantity provided")
                .uponReceiving("A POST request for data")
                    .path("/data")
                    .method("POST")
                .willRespondWith()
                    .status(201)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Data created successfully\", \"data\": {\"item\": \"Item1\", \"quantity\": 10}}")
                .toPact();
    }

    @Test
    void testCreateData(MockServer mockServer) throws IOException {
        DataClient client = new DataClient(mockServer.getUrl());
        String response = client.createData("Item1", 10);
        assertEquals("api-service", response.split(",")[0].split(":")[1]);
        assertEquals("Data created successfully", response.split(",")[1].split(":")[1]);
        Map<String, Object> data = (Map<String, Object>) client.parseResponse(response);
        assertEquals("Item1", data.get("item"));
        assertEquals(10, data.get("quantity"));
    }

    @Pact(consumer = "api-client")
    public RequestResponsePact createPactGet(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("data exists")
                .uponReceiving("A GET request for data")
                    .path("/data")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Data retrieved successfully\", \"data\": {\"item\": \"Item1\", \"quantity\": 10}}")
                .toPact();
    }

    @Test
    void testGetData(MockServer mockServer) throws IOException {
        DataClient client = new DataClient(mockServer.getUrl());
        String response = client.getData();
        assertEquals("api-service", response.split(",")[0].split(":")[1]);
        assertEquals("Data retrieved successfully", response.split(",")[1].split(":")[1]);
        Map<String, Object> data = (Map<String, Object>) client.parseResponse(response);
        assertEquals("Item1", data.get("item"));
        assertEquals(10, data.get("quantity"));
    }
}