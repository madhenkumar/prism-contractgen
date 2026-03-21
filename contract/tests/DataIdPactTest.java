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
public class DataIdPactTest {

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPactDelete(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Data with ID 1 exists")
                .uponReceiving("A request to delete data 1")
                    .path("/data/1")
                    .method("DELETE")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Data deleted\"}")
                .toPact();
    }

    @Test
    void testDeleteUser(MockServer mockServer) throws IOException {
        DataClient client = new DataClient(mockServer.getUrl());
        String result = client.deleteData(1);
        assertEquals("api-service", result.split(",")[0]);
        assertEquals("Data deleted", result.split(",")[1]);
    }

    @Pact(consumer = "DataFrontend")
    public RequestResponsePact createPactPut(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("Data with ID 1 exists")
                .uponReceiving("A request to update data 1")
                    .path("/data/1")
                    .method("PUT")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\"service\": \"api-service\", \"message\": \"Data updated\", \"data\": {\"status\": \"active\"}}")
                .toPact();
    }

    @Test
    void testUpdateUser(MockServer mockServer) throws IOException {
        DataClient client = new DataClient(mockServer.getUrl());
        Map<String, String> result = client.updateData(1);
        assertEquals("api-service", result.get("service"));
        assertEquals("Data updated", result.get("message"));
        assertEquals("active", result.get("data").get("status"));
    }
}