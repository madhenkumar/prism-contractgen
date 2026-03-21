public class HelloPactTest {

    @ExtendWith(PactConsumerTestExt.class)
    @PactTestFor(providerName = "api-service", port = "8080")
    public static class PactHelloTest {
        
        @Pact(consumer = "HelloFrontend")
        public RequestResponsePact createPact(PactDslWithProvider builder) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            return builder
                    .given("No Precondition")
                    .uponReceiving("A GET /hello request")
                        .path("/hello")
                        .method("GET")
                    .willRespondWith()
                        .status(200)
                        .headers(headers)
                        .body("{\"service\": \"api-service\", \"message\": \"Hello World!\"}")
                    .toPact();
        }

        @Test
        void testGetHello(MockServer mockServer) {
            HelloClient client = new HelloClient(mockServer.getUrl());
            String helloResponse = client.getHello();
            assertEquals("api-service", helloResponse.split("\\s+")[0]);
            assertEquals("Hello World!", helloResponse.split("\\s+")[1]);
        }
    }
}