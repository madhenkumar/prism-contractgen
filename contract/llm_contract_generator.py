import json
import urllib.request
import ollama

OLLAMA_API_URL = "http://localhost:11434/api/generate"
OLLAMA_MODEL = "incept5/llama3.1-claude"

def generate_contract_for_endpoint(endpoint_chunk):
    print(f"Generating contract for {endpoint_chunk['method']} {endpoint_chunk['path']}...")
    
    prompt = f"""
You are an API contract generator. Convert the following OpenAPI route chunk into a specific JSON contract format.

### Target JSON Schema Format to Output:
{{
  "service": "api-service",
  "endpoint": "METHOD /path",
  "request": {{
    "headers": {{
      "Content-Type": "application/json"
    }},
    "body_schema": {{}}
  }},
  "response": {{
    "status": 200,
    "body_schema": {{}}
  }}
}}

### Input OpenAPI Route Chunk:
{json.dumps(endpoint_chunk, indent=2)}

Return ONLY valid JSON matching the exact schema above. Make sure the body_schema matches the types inferred. Do not wrap taking in markdown blocks (e.g. no ```json).
"""

    try:
        response = ollama.chat(
            model=OLLAMA_MODEL,
            messages=[{'role': 'user', 'content': prompt}],
            format='json'
        )
        return json.loads(response['message']['content'])
    except Exception as e:
        print(f"Error querying Ollama for {endpoint_chunk['path']}: {e}")
        return None

def main():
    try:
        with open("openapi.json", "r", encoding="utf-8") as f:
            openapi = json.load(f)
    except FileNotFoundError:
        print("Error: openapi.json not found.")
        return

    # Build chunks from paths
    chunks = []
    for path, methods in openapi.get("paths", {}).items():
        for method, details in methods.items():
            desc = details.get("description") or ""
            chunks.append({
                "method": method.upper(),
                "path": path,
                "description": desc,
                "parameters": details.get("parameters", []),
                "requestBody": details.get("requestBody", {}),
                "responses": details.get("responses", {})
            })

    print(f"Found {len(chunks)} endpoint(s) to process.")

    all_contracts = []
    for endpoint in chunks:
        contract = generate_contract_for_endpoint(endpoint)
        if contract:
            all_contracts.append(contract)

    with open("generated_contracts.json", "w", encoding="utf-8") as f:
        json.dump(all_contracts, f, indent=2, ensure_ascii=False)

    print("✅ Generated contracts saved to generated_contracts.json")

if __name__ == "__main__":
    main()
