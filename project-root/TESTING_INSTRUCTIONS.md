# How to Test mitmproxy Interception

This guide explains how to manually test the setup on your Windows machine.

### 1. Stop Existing Processes
If you have any existing conflicting services running, stop them. You can press `Ctrl+C` in any running terminals. I have already started some of these processes in the background for you, but assuming you are starting fresh:

### 2. Start mitmproxy
Open a new terminal (Command Prompt or PowerShell) at the `project-root` and start the interactive mitmproxy UI. Since you are on Windows and installed via pip, you should run:
```bash
mitmproxy --listen-port 8080
```
*(Leave this terminal window open. It will show a black UI where intercepted traffic appears).*

### 3. Start Service A
Open a **second** terminal inside the `project-root/service-a` folder and start it normally:
```bash
cd service-a
node index.js
```

### 4. Start Service B (with Proxy Config)
Open a **third** terminal inside the `project-root/service-b` folder. Set the environment variables and start the service.

Since we are on Windows Command Prompt, run:
```cmd
set HTTP_PROXY=http://localhost:8080
set HTTPS_PROXY=http://localhost:8080
set NO_PROXY=
set NODE_OPTIONS=--use-env-proxy
node index.js
```
*(Note: I have also created a `run-b.cmd` and `start-b.ps1` file in the `service-b` directory which run these exact commands for you).*

### 5. Trigger the Request
Open a **fourth** terminal (or just use your browser) and trigger `service-b` to call `service-a`:
```bash
curl http://localhost:3002/call-a
```
*(You should see the JSON response: `{"service":"B","dataFromA":{"service":"A","message":"Hello from service A"}}`)*

### 6. Verify Interception!
Go back to the **first** terminal where `mitmproxy` is running.
You will see a log of the request made from Service B to Service A:
```
GET http://localhost:3001/hello
```
You can use your arrow keys to select the request and hit `Enter` to inspect the headers and output!
