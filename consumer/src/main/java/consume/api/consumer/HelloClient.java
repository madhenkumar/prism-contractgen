package consume.api.consumer;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class HelloClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HelloClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ✅ Success case
    public String getHello() {
        String url = baseUrl + "/hello";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return extractMessage(response.getBody());
    }

    // ✅ Bad request / not found / server error
    public String getHello(String param) {
        String url;

        if ("invalid".equals(param)) {
            url = baseUrl + "/hello/invalid"; // 404 case
        } else {
            url = baseUrl + "/hello"; // 400 / 500 case
        }

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return extractMessage(response.getBody());
        } catch (Exception ex) {
            return extractError(ex);
        }
    }

    // ✅ Unauthorized case
    public String getHello(String param, String token) {
        String url = baseUrl + "/hello";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return extractMessage(response.getBody());
        } catch (Exception ex) {
            return extractError(ex);
        }
    }

    // 🔧 Extract "message" field
    private String extractMessage(String body) {
        try {
            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            return (String) map.get("message");
        } catch (Exception e) {
            return null;
        }
    }

    // 🔧 Extract "error" field
    private String extractError(Exception ex) {
        try {
            if (ex instanceof org.springframework.web.client.HttpStatusCodeException httpEx) {
                String body = httpEx.getResponseBodyAsString();
                Map<String, Object> map = objectMapper.readValue(body, Map.class);
                return (String) map.get("error");
            }
        } catch (Exception ignored) {}
        return null;
    }
}