import json
from datetime import datetime

def load(loader):
    """Called when mitmproxy starts - verify addon is loaded"""
    print("[LOGGER] Addon loaded successfully!")

def response(flow):
    """Log all HTTP responses"""
    try:
        # Safely get response body (may be empty or binary)
        try:
            response_body = flow.response.text
        except Exception:
            response_body = "[Binary or non-UTF8 content]"
        
        data = {
            "timestamp": datetime.now().isoformat(),
            "method": flow.request.method,
            "url": flow.request.pretty_url,
            "status": flow.response.status_code,
            "request_headers": dict(flow.request.headers),
            "response_headers": dict(flow.response.headers),
            "response_body": response_body
        }

        with open("logs.json", "a") as f:
            f.write(json.dumps(data) + "\n")
        
        print(f"[LOGGER] Logged: {flow.request.method} {flow.request.pretty_url} -> {flow.response.status_code}")
    
    except Exception as e:
        print(f"[LOGGER ERROR] Failed to log response: {e}")
        import traceback
        traceback.print_exc()