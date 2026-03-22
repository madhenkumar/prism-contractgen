# Set Java proxy system properties via JAVA_TOOL_OPTIONS so any JVM picked up in this session uses mitmdump
$env:JAVA_TOOL_OPTIONS="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=8080 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts="

Write-Host "Starting Consumer Service with proxy routed to http://127.0.0.1:8080..."

# Start the Spring Boot application using Maven
mvn spring-boot:run
