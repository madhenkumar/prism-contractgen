import json
from datetime import datetime

def response(flow):
    data = {
        "timestamp": datetime.now().isoformat(),
        "method": flow.request.method,
        "url": flow.request.pretty_url,
        "status": flow.response.status_code,
        "request_headers": dict(flow.request.headers),
        "response_headers": dict(flow.response.headers),
        "response_body": flow.response.text
    }

    with open("logs.json", "a") as f:
        f.write(json.dumps(data) + "\n")