import json
import ollama
import os
import re
from collections import defaultdict

OLLAMA_MODEL = "incept5/llama3.1-claude"
TEMPLATE = """
package consume.api.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.PactSpecVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "api-service", port = "8081", pactVersion = PactSpecVersion.V3)
public class ExamplePactTest {

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

    @Pact(consumer = "ExampleFrontend")
    public RequestResponsePact createPactSuccess(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /example")
                .path("/example")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body("{\"message\": \"success\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactSuccess")
    void testSuccess(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(mockServer.getUrl() + "/example", String.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Pact(consumer = "ExampleFrontend")
    public RequestResponsePact createPactBadRequest(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("service is up and running")
                .uponReceiving("A GET request to /example/bad-request")
                .path("/example/bad-request")
                .method("GET")
                .willRespondWith()
                .status(400)
                .headers(headers)
                .body("{\"error\": \"Bad Request\"}")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPactBadRequest")
    void testBadRequest(MockServer mockServer) throws IOException {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.getForEntity(mockServer.getUrl() + "/example/bad-request", String.class);
            fail("Expected HttpClientErrorException but request succeeded");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            assertEquals(400, ex.getStatusCode().value());
        }
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
STRICT OUTPUT RULES
========================
- OUTPUT ONLY JAVA CODE — no markdown, no explanation, no comments outside code
- ONLY ONE public class named exactly: {class_name}
- NO nested or inner classes
- ALL methods must be flat inside the class

========================
FIXED IMPORTS (COPY EXACTLY)
========================
Use EXACTLY these imports, no additions, no removals:

package consume.api.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.PactSpecVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

========================
CLASS-LEVEL ANNOTATIONS (CRITICAL — DO NOT MOVE)
========================
These TWO annotations MUST appear DIRECTLY above the class declaration.
NEVER place them on any method. NEVER place them inside the class body.

CORRECT (copy exactly):

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "api-service", port = "8081", pactVersion = PactSpecVersion.V3)
public class {class_name} {{

WRONG — will cause test failures:

public class {class_name} {{
    @ExtendWith(...)       // ← NEVER on a method
    @BeforeAll
    static void setup()    // ← NEVER put @ExtendWith here

REASON: @ExtendWith(PactConsumerTestExt.class) registers the MockServer 
parameter resolver. If it is not on the CLASS, every @Test method will fail 
with: "No ParameterResolver registered for parameter MockServer".

========================
BEFOREALL RULE (CRITICAL)
========================
@BeforeAll MUST have NO other annotations — only @BeforeAll.

CORRECT:
    @BeforeAll
    static void setup() {{
        baseUrl = ConfigLoader.getBaseUrl();
        ...
    }}

WRONG:
    @ExtendWith(PactConsumerTestExt.class)   // ← NEVER here
    @BeforeAll
    static void setup() {{

========================
CONFIG RULE
========================
Declare these four fields at the top of the class and populate via ConfigLoader ONLY:

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

DO NOT hardcode "localhost", "8081", "Producer", or "Consumer" anywhere.
DO NOT use ConfigLoader for request/response bodies.

========================
PACT METHOD RULE
========================
Every @Pact method MUST:
- Return: RequestResponsePact  (NOT V4Pact, NOT PactBuilder)
- Accept: PactDslWithProvider builder
- Be annotated: @Pact(consumer = "ConsumerName")

========================
SCENARIO RULE
========================
Generate exactly FIVE scenario pairs (one @Pact + one @Test each):

| @Pact method name       | @Test method name       | Status |
|-------------------------|-------------------------|--------|
| createPactSuccess       | testSuccess             | 200    |
| createPactBadRequest    | testBadRequest          | 400    |
| createPactNotFound      | testNotFound            | 404    |
| createPactUnauthorized  | testUnauthorized        | 401    |
| createPactServerError   | testServerError         | 500    |

========================
TEST ANNOTATION RULE (CRITICAL)
========================
EVERY @Test method MUST have BOTH annotations, in this order:

    @Test
    @PactTestFor(pactMethod = "createPactSuccess")
    void testSuccess(MockServer mockServer) throws IOException {{

NO @Test method is allowed without @PactTestFor(pactMethod = "...").

========================
ERROR CLASSIFICATION RULE (CRITICAL)
========================
4xx errors → catch HttpClientErrorException
5xx errors → catch HttpServerErrorException

CORRECT:
    // 404 test
    }} catch (org.springframework.web.client.HttpClientErrorException ex) {{
        assertEquals(404, ex.getStatusCode().value());
    }}

WRONG:
    // 404 test — 404 is a CLIENT error, NOT a server error
    }} catch (org.springframework.web.client.HttpServerErrorException ex) {{

Status → correct exception class:
- 400 → HttpClientErrorException
- 401 → HttpClientErrorException
- 404 → HttpClientErrorException
- 500 → HttpServerErrorException

========================
TEST IMPLEMENTATION RULE
========================
SUCCESS (200/201):
    ResponseEntity<String> response = restTemplate.getForEntity(
        mockServer.getUrl() + "/path", String.class);
    assertEquals(200, response.getStatusCode().value());

ERROR (4xx/5xx):
    try {{
        restTemplate.getForEntity(mockServer.getUrl() + "/path", String.class);
        fail("Expected exception but request succeeded");
    }} catch (org.springframework.web.client.HttpClientErrorException ex) {{
        assertEquals(400, ex.getStatusCode().value());
    }}

========================
SELF-CHECK BEFORE OUTPUT
========================
Before writing the final output, verify each item:

[ ] @ExtendWith(PactConsumerTestExt.class) is on the CLASS, not on any method
[ ] @PactTestFor(..., pactVersion = PactSpecVersion.V3) is on the CLASS
[ ] @BeforeAll setup() has NO other annotations
[ ] Every @Test has @PactTestFor(pactMethod = "...")
[ ] 4xx errors use HttpClientErrorException (including 404)
[ ] 5xx errors use HttpServerErrorException
[ ] No hardcoded baseUrl, port, providerName, consumerName
[ ] All bodies come from API contracts only
[ ] Exactly 5 scenario pairs generated
[ ] Package is: consume.api.consumer

If ANY item is unchecked → fix it before outputting.

========================
REFERENCE TEMPLATE
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
