import json
import urllib.request
import os
import sys
import argparse
import re
import ollama

# Pull Broker URL from environment or fallback to localhost
BROKER_URL = os.environ.get("PACT_BROKER_URL", "http://localhost:9292")
OLLAMA_MODEL = "incept5/llama3.1-claude"

TEMPLATE = """package consume.api.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Provider("{provider_name}") // MUST match pact
@PactBroker(url = "${{pactbroker.url:http://localhost:9292}}")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class {class_name} {{

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup(PactVerificationContext context) {{
        context.setTarget(new HttpTestTarget("localhost", port));
    }}

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {{
        context.verifyInteraction();
    }}
}}
"""

def generate_class_name(provider_name):
    clean = re.sub(r'[^a-zA-Z0-9]', ' ', provider_name)
    pascal = ''.join(word.capitalize() for word in clean.split() if word)
    if not pascal.endswith("ProviderPactTest"):
        pascal = f"{pascal}ProviderPactTest"
    return pascal

def fetch_broker(url):
    print(f"Fetching from: {url}")
    try:
        req = urllib.request.Request(url, headers={
            'Accept': 'application/hal+json, application/json'
        })
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
            return True, data
    except urllib.error.HTTPError as e:
        print(f"❌ HTTP Error: {e.code} - {e.reason}")
    except Exception as e:
        print(f"❌ Error fetching from broker: {e}")
    return False, None

def generate_pact_test_with_ollama(provider, class_name, pact_data):
    print(f"Asking Ollama to generate Provider Test for API '{provider}' (Class: {class_name}.java) ...")
    
    # We provide the base template with correct variables injected
    base_template = TEMPLATE.format(provider_name=provider, class_name=class_name)
    
    pact_json = json.dumps(pact_data, indent=2)
    prompt = f"""
You are a senior Java engineer specializing in Pact JVM and Spring Boot testing.

Your task is to generate a COMPLETE and COMPILABLE Java test class for Pact Provider verification.

========================
STRICT OUTPUT RULES (MANDATORY)
========================
- Output ONLY valid Java code
- Do NOT include markdown, explanations, or comments outside code
- Do NOT wrap output in ``` blocks
- Output EXACTLY ONE public class
- Class name MUST be exactly: {class_name}
- Do NOT generate additional classes
- Do NOT modify package name or imports
- Do NOT remove or alter existing template structure
- Do NOT add extra annotations, fields, or methods outside what is required

========================
TEMPLATE (IMMUTABLE BASE)
========================
You MUST use the following template EXACTLY as the base.
Only allowed modification: inserting @State methods before the @TestTemplate method.

{base_template}

========================
STATE GENERATION RULES
========================
1. Parse the provided Pact JSON
2. Extract ALL provider state names from:
   interactions[].providerStates[].name

3. Create ONE method per UNIQUE state:
   - Deduplicate states
   - Preserve EXACT string (case-sensitive)

4. Each state must be implemented as:

@State("EXACT_STATE_NAME")
public void methodNameDerivedFromState() 

5. Method naming rules:
   - camelCase
   - remove spaces and special characters
   - prefix with "setup"
   Example:
     "User with ID 1 exists" → setupUserWithId1Exists

6. DO NOT:
   - add logic inside methods
   - add parameters
   - change annotations

========================
PLACEMENT RULE
========================
- Insert ALL @State methods AFTER setup() method
- Insert BEFORE @TestTemplate method
- Do NOT reorder existing methods

========================
PACT DATA (JSON)
========================
{pact_json}
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
        print(f"Error generating test via Ollama for {provider}: {e}")
        return None

def generate_provider_test(provider, pact_data):
    os.makedirs("tests_provider", exist_ok=True)
    class_name = generate_class_name(provider)
    
    java_code = generate_pact_test_with_ollama(provider, class_name, pact_data)
    
    if java_code:
        file_path = os.path.join("tests_provider", f"{class_name}.java")
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(java_code)
        
        print(f"✅ Generated Provider Test using Ollama for '{provider}': {file_path}")

def main():
    parser = argparse.ArgumentParser(description="Generate Spring Boot Provider Pact Tests using Ollama")
    parser.add_argument("--provider", required=True, help="Name of the Provider (e.g., UserService)")
    parser.add_argument("--consumer", help="Name of the Consumer (optional)")
    parser.add_argument("--all", action="store_true", help="Fetch all pacts for the provider instead of just the latest")
    
    args = parser.parse_args()
    
    provider = args.provider
    consumer = args.consumer
    fetch_all = args.all

    # Route 3. Get specific consumer-provider pact
    if consumer:
        url = f"{BROKER_URL}/pacts/provider/{provider}/consumer/{consumer}/latest"
        found, data = fetch_broker(url)
        if found:
            print(f"✅ Found latest pact between Consumer '{consumer}' and Provider '{provider}'.")
            generate_provider_test(provider, data)
        else:
            print(f"❌ Could not find a latest pact for Consumer '{consumer}' and Provider '{provider}'.")
        return

    # Route 2. Get all pacts for provider
    if fetch_all:
        url = f"{BROKER_URL}/pacts/provider/{provider}"
        found, data = fetch_broker(url)
        if found:
            print(f"✅ Found pacts for Provider '{provider}'.")
            generate_provider_test(provider, data)
        else:
            print(f"❌ Could not find any pacts for Provider '{provider}'.")
        return

    # Route 1. Get latest pacts for provider
    url = f"{BROKER_URL}/pacts/provider/{provider}/latest"
    found, data = fetch_broker(url)
    if found:
        print(f"✅ Found latest pacts for Provider '{provider}'.")
        generate_provider_test(provider, data)
    else:
        print(f"❌ Could not find latest pacts for Provider '{provider}'.")

if __name__ == "__main__":
    main()
