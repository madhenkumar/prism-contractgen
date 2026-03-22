package consume.api.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class ConsumerApplication {

    private final RestTemplate restTemplate;

    public ConsumerApplication() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress("127.0.0.1", 8090));
        factory.setProxy(proxy);
        this.restTemplate = new RestTemplate(factory);
    }
    // Producer is now listening on 8081 based on the properties change you made
    private final String producerUrl = "http://localhost:8081";

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

    @GetMapping("/call-a")
    public Map<String, Object> callA() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "B");
        try {
            ResponseEntity<Map> apiResponse = restTemplate.getForEntity(producerUrl + "/hello", Map.class);
            response.put("dataFromA", apiResponse.getBody());
        } catch (Exception e) {
            response.put("error", "Failed to fetch from Service A");
            response.put("details", e.getMessage());
        }
        return response;
    }

    @PostMapping("/send-to-a")
    public Map<String, Object> sendToA(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "B");
        try {
            ResponseEntity<Map> apiResponse = restTemplate.postForEntity(producerUrl + "/data", body, Map.class);
            response.put("dataFromA", apiResponse.getBody());
        } catch (Exception e) {
            response.put("error", "Failed to post to Service A");
            response.put("details", e.getMessage());
        }
        return response;
    }

    @PutMapping("/update-a/{id}")
    public Map<String, Object> updateA(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "B");
        try {
            ResponseEntity<Map> apiResponse = restTemplate.exchange(producerUrl + "/data/" + id, HttpMethod.PUT, new org.springframework.http.HttpEntity<>(body), Map.class);
            response.put("dataFromA", apiResponse.getBody());
        } catch (Exception e) {
            response.put("error", "Failed to put to Service A");
            response.put("details", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/delete-a/{id}")
    public Map<String, Object> deleteA(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "B");
        try {
            ResponseEntity<Map> apiResponse = restTemplate.exchange(producerUrl + "/data/" + id, HttpMethod.DELETE, null, Map.class);
            response.put("dataFromA", apiResponse.getBody());
        } catch (Exception e) {
            response.put("error", "Failed to delete from Service A");
            response.put("details", e.getMessage());
        }
        return response;
    }

}
