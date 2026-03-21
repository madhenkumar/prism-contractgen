import json
from urllib.parse import urlparse
import re
from collections import defaultdict

# ---------- Helpers ----------

def infer_param(segment):
    if re.fullmatch(r"\d+", segment):
        return "{id}"
    if re.fullmatch(r"[0-9a-fA-F-]{36}", segment):
        return "{uuid}"
    return segment

def normalize_path(path):
    segments = path.strip("/").split("/")
    normalized = [infer_param(seg) for seg in segments]
    return "/" + "/".join(normalized)

def extract_path_params(path):
    params = []
    segments = path.strip("/").split("/")

    for seg in segments:
        if re.fullmatch(r"\d+", seg):
            params.append({
                "name": "id",
                "in": "path",
                "required": True,
                "schema": {"type": "string"}
            })
    return params

def infer_schema(obj):
    if isinstance(obj, dict):
        return {
            "type": "object",
            "properties": {
                k: infer_schema(v) for k, v in obj.items()
            }
        }
    elif isinstance(obj, list):
        return {
            "type": "array",
            "items": infer_schema(obj[0]) if obj else {}
        }
    elif isinstance(obj, int):
        return {"type": "integer"}
    elif isinstance(obj, float):
        return {"type": "number"}
    elif isinstance(obj, bool):
        return {"type": "boolean"}
    else:
        return {"type": "string"}


# ---------- Main Logic ----------

def generate_openapi(log_file):
    with open(log_file, "r") as f:
        lines = f.readlines()

    paths = defaultdict(dict)

    for line in lines:
        log = json.loads(line)

        parsed_url = urlparse(log["url"])
        raw_path = parsed_url.path
        method = log["method"].lower()

        normalized_path = normalize_path(raw_path)

        # Parse response
        try:
            response_body = json.loads(log["response"])
        except:
            response_body = {}

        response_schema = infer_schema(response_body)

        # Path params
        parameters = extract_path_params(raw_path)

        # Request body (only for POST/PUT)
        request_body = None
        if method in ["post", "put"] and "data" in response_body:
            request_body = {
                "content": {
                    "application/json": {
                        "schema": infer_schema(response_body["data"])
                    }
                }
            }

        # Build operation
        operation = {
            "summary": f"{method.upper()} {normalized_path}",
            "responses": {
                str(log["status"]): {
                    "description": "Auto-generated response",
                    "content": {
                        "application/json": {
                            "schema": response_schema
                        }
                    }
                }
            }
        }

        if parameters:
            operation["parameters"] = parameters

        if request_body:
            operation["requestBody"] = request_body

        paths[normalized_path][method] = operation

    # Final OpenAPI spec
    openapi_spec = {
        "openapi": "3.0.0",
        "info": {
            "title": "Generated API",
            "version": "1.0.0"
        },
        "paths": dict(paths)
    }

    return openapi_spec


# ---------- Run ----------

if __name__ == "__main__":
    spec = generate_openapi("logs.json")

    with open("openapi.json", "w") as f:
        json.dump(spec, f, indent=2)

    print("✅ OpenAPI spec generated: openapi.json")