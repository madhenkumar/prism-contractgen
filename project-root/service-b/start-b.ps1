$env:HTTP_PROXY="http://localhost:8080"
$env:HTTPS_PROXY="http://localhost:8080"
$env:NO_PROXY=""
$env:NODE_OPTIONS="--use-env-proxy"
node index.js
