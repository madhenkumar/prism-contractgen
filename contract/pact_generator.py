import json
import ollama
import os
import re
from collections import defaultdict

OLLAMA_MODEL = "incept5/llama3.1-claude"
TEMPLATE = """import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.*;
import au.com.dius.pact.core.model.RequestResponsePact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "UserService", port = "8081")
public class UserConsumerPactTest {

    @Pact(consumer = "UserFrontend")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("User with ID 1 exists")
                .uponReceiving("A request for user 1")
                    .path("/users/1")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\\"id\\": 1, \\"name\\": \\"Alice\\", \\"email\\": \\"alice@example.com\\"}")
                .toPact();
    }

    @Test
    void testGetUser(MockServer mockServer) throws IOException {
        UserServiceClient client = new UserServiceClient(mockServer.getUrl());
        User user = client.getUser(1);
        assertEquals(1, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
    }
}
"""

def generate_java_filename(path):
    # Convert /data/{id} -> DataIdPactTest
    clean = re.sub(r'[^a-zA-Z0-9]', ' ', path)
    pascal = ''.join(word.capitalize() for word in clean.split() if word)
    if not pascal:
        pascal = "Root"
    return f"{pascal}PactTest"

def generate_pact_test_for_group(path, class_name, contracts):
    print(f"Generating Pact Test for {path} (Class: {class_name}.java) ...")
    
    prompt = f"""
You are an expert Java developer writing JUnit 5 + Pact JVM consumer tests.
Generate a single, complete, valid Java file implementing proper Pact JVM tests for the given API contracts.

CRITICAL INSTRUCTIONS:
1. OUTPUT RAW JAVA CODE ONLY. NO TRIPLE BACKTICKS (```). DO NOT INCLUDE ANY MARKDOWN formatting. NO EXPLANATIONS.
2. The class MUST be named exactly `{class_name}`.
3. Keep the provider annotations properly structured. Use `@ExtendWith(PactConsumerTestExt.class)`.
4. Create a `@Pact` method and a corresponding `@Test` method for EACH of the HTTP methods provided in the JSON payload below.
5. In the @Pact body, map the JSON schema types to concrete dummy string examples (e.g. if schema says type is integer, use 1; if string use "Alice").

Here is the EXACT Pact template structure to learn from (do not copy the endpoints literally, just the structure):
{TEMPLATE}


---

API CONTRACTS TO IMPLEMENT FOR `{path}`:
{json.dumps(contracts, indent=2)}
"""

    try:
        response = ollama.chat(
            model=OLLAMA_MODEL,
            messages=[{'role': 'user', 'content': prompt}]
        )
        content = response['message']['content'].strip()
        
        # Strip markdown code blocks if the LLM hallucinated them despite instructions
        if content.startswith("```"):
            lines = content.splitlines()
            if lines[0].startswith("```"):
                lines = lines[1:]
            if lines and lines[-1].startswith("```"):
                lines = lines[:-1]
            content = "\n".join(lines).strip()
            
        return content
    except Exception as e:
        print(f"Error generating test for {path}: {e}")
        return None

def main():
    try:
        with open("generated_contracts.json", "r", encoding="utf-8") as f:
            contracts = json.load(f)
    except FileNotFoundError:
        print("Error: generated_contracts.json not found. Run llm_contract_generator.py first.")
        return

    # Group by path
    groups = defaultdict(list)
    for c in contracts:
        endpoint = c.get("endpoint", "")
        if " " in endpoint:
            method, path = endpoint.split(" ", 1)
        else:
            path = endpoint
        groups[path].append(c)

    os.makedirs("tests", exist_ok=True)
    
    print(f"Found {len(groups)} distinct route paths to process.")

    for path, group_contracts in groups.items():
        class_name = generate_java_filename(path)
        java_code = generate_pact_test_for_group(path, class_name, group_contracts)
        
        if java_code:
            file_path = f"tests/{class_name}.java"
            with open(file_path, "w", encoding="utf-8") as f:
                f.write(java_code)
            print(f"  -> Saved {file_path}")

    print("✅ Pact generation completed.")

if __name__ == "__main__":
    main()
