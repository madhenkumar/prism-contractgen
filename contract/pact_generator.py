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
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/users/1", String.class);
        assertEquals(200, response.getStatusCode().value());
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
You are a senior Java test automation engineer specializing in Pact JVM.

Generate a COMPLETE, COMPILABLE Java file.

========================
STRICT RULES
========================
- OUTPUT ONLY JAVA CODE
- NO markdown, NO explanation
- Class name MUST be {class_name}
- ONLY ONE public class
- ABSOLUTELY NO @Nested CLASSES OR INNER CLASSES
- FATAL ERROR IF YOU USE @Nested
- ALL @Test and @Pact methods MUST be flat and placed directly inside the main class

========================
MANDATORY IMPORTS (DO NOT MODIFY)
========================
You MUST include EXACTLY these imports:

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

DO NOT REMOVE OR CHANGE THESE IMPORTS.

========================
FRAMEWORK RULES
========================
- JUnit 5 ONLY
- Use @ExtendWith(PactConsumerTestExt.class)
- Use @PactTestFor(providerName="api-service", port="8081")

========================
CONFIG RULES
========================
Use ConfigLoader:

static String baseUrl;
static String providerName;
static String consumerName;
static String port;

@BeforeAll
static void setup() {{
    baseUrl = ConfigLoader.getBaseUrl();
    providerName = ConfigLoader.getProviderName();
    consumerName = ConfigLoader.getConsumerName();
    port = ConfigLoader.getPort();
}}

========================
PACT RULES
========================
For EACH endpoint generate:

1. success (200)
2. bad request (400)
3. not found (404)
4. unauthorized (401)
5. server error (500)

Each MUST have:
- ONE @Pact method returning RequestResponsePact
- ONE @Test method

Each @Test MUST include:
@PactTestFor(pactMethod="EXACT_METHOD_NAME")

========================
CRITICAL TYPE RULE
========================
ALL pact methods MUST return:

RequestResponsePact

Use EXACT type:
au.com.dius.pact.core.model.RequestResponsePact

DO NOT use any other type.

========================
NAMING RULE
========================
createPact_<scenario>
test_<scenario>

========================
DATA RULES
========================
int → 1
string → "Alice"
boolean → true

========================
HTTP CLIENT RULE
========================
- DO NOT invent or assume any custom client classes (e.g. HelloClient, UserServiceClient)
- YOU MUST use standard `org.springframework.web.client.RestTemplate` or `java.net.http.HttpClient` directly in the @Test method to make the HTTP request to the MockServer URL.

========================
IMPORTANT
========================
- FATAL ERROR IF YOU USE @Nested CLASSES
- NO multiple classes
- MUST COMPILE
- DO NOT omit imports
- DO NOT use wildcard imports

========================
REFERENCE STRUCTURE
========================
{TEMPLATE}

========================
API CONTRACTS
========================
{json.dumps(contracts, indent=2)}

"""

    try:
        response = ollama.chat(
            model=OLLAMA_MODEL,
            messages=[{'role': 'user', 'content': prompt}]
        )
        content = response['message']['content'].strip()
        
        # Strip markdown code blocks if the LLM hallucinated them despite instructions
        if content.startswith(""):
            lines = content.splitlines()
            if lines[0].startswith(""):
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
