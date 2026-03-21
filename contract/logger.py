import json

def response(flow):
    data = {
        "url": flow.request.pretty_url,
        "method": flow.request.method,
        "status": flow.response.status_code,
        "response": flow.response.text
    }

    with open("logs.json", "a") as f:
        f.write(json.dumps(data) + "\n")