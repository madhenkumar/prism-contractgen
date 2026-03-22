package producer.api.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class ProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}

    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "A");
        response.put("message", "Hello from service A");
        return response;
    }

    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> createData(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "A");
        response.put("message", "Data created successfully");
        response.put("data", body);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/data/{id}")
    public Map<String, Object> updateData(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "A");
        response.put("message", "Data with ID " + id + " updated successfully");
        response.put("data", body);
        return response;
    }

    @DeleteMapping("/data/{id}")
    public Map<String, Object> deleteData(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "A");
        response.put("message", "Data with ID " + id + " deleted successfully");
        return response;
    }
}
