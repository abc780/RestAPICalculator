# RestAPICalculator

## Considerations

The current implementation is experiencing connectivity issues with Kafka, likely due to configuration problems that need to be resolved. Although this was my first experience working with Kafka technology, the challenge has been both insightful and engaging, providing valuable learning opportunities despite the non-functional result.

# Calculator REST API

A Spring Boot REST API service that provides basic arithmetic operations through HTTP endpoints, using Apache Kafka for asynchronous processing and communication with backend calculation services.
A Spring Boot microservice that performs arithmetic calculations by consuming requests from Kafka topics and publishing results back. This service acts as the backend calculation engine for the Calculator REST API.

## Architecture Overview

### Rest Module
This service acts as a REST gateway that:
1. Receives HTTP requests for mathematical operations
2. Forwards requests to Kafka topics for processing
3. Waits for responses from backend calculation services
4. Returns results to HTTP clientes

### Calculator Module
1. Consumes calculation requests from calc-requests Kafka topic
2. Performs high-precision arithmetic operations using BigDecimal
3. Publishes calculation results to calc-results Kafka topic
4. Maintains correlation IDs for request tracking

## Features

- **RESTful Endpoints**: HTTP endpoints for basic arithmetic operations
- **Asynchronous Processing**: Uses Kafka for request-response pattern
- **High Precision**: Utilizes `BigDecimal` for accurate decimal calculations  
- **Request Correlation**: Tracks requests using correlation IDs
- **Health Monitoring**: Built-in health check endpoints
- **Structured Logging**: MDC-based request tracking
- **Error Handling**: Comprehensive exception handling with meaningful responses

## API Endpoints

### Arithmetic Operations

All arithmetic endpoints accept two parameters (`a` and `b`) and return JSON responses.

```
GET /api/sum?a=10.5&b=5.2
GET /api/subtract?a=10.5&b=5.2  
GET /api/multiply?a=10.5&b=5.2
GET /api/divide?a=10.5&b=5.2
```

**Parameters:**
- `a` (required): First operand (BigDecimal)
- `b` (required): Second operand (BigDecimal)

**Response Format:**
```json
{
  "result": "15.7",
  "correlationId": "uuid-string",
  "operation": "SUM", 
  "timestamp": "2024-01-01T10:00:00Z",
  "success": true
}
```

**Headers:**
- `X-Request-ID`: Unique request identifier for tracking

### Health Check

```
GET /api/health
```
Returns service health status.

### Debug Endpoints

```
GET /debug/kafka-health  
```
Returns Kafka connection status and configuration details.

## Configuration

### Required Environment Variables

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092  # Kafka broker address
```

### Kafka Topics

The service uses the following Kafka topics:
- `calculator.requests` - Outbound calculation requests
- `calculator.responses` - Inbound calculation results

### Consumer Groups

- `calculator-rest-group` - For consuming calculation responses

## Prerequisites

- Java 11 or higher
- Apache Kafka cluster
- Backend calculation service (that processes requests from `calculator.requests` topic)

## Quick Start


### Building

### Build jar Files in Target Folder
```bash
mvn clean package
```

### Docker Compose File
```bash
docker-compose up --build
```

## Usage Examples

### Basic Calculations

```bash
# Addition
curl "http://localhost:8080/api/sum?a=15.5&b=10.25"

# Subtraction  
curl "http://localhost:8080/api/subtract?a=100&b=25.5"

# Multiplication
curl "http://localhost:8080/api/multiply?a=7.5&b=4"

# Division
curl "http://localhost:8080/api/divide?a=100&b=3"
```

### Health Checks

```bash
# Service health
curl http://localhost:8080/api/health

# Kafka connectivity
curl http://localhost:8080/debug/kafka-health
```

## Error Handling

The services provides structured error responses:

### Rest Module

```json
{
  "result": null,
  "correlationId": "uuid-string", 
  "operation": "DIVIDE",
  "timestamp": "2024-01-01T10:00:00Z",
  "success": false,
  "error": "Division by zero"
}
```

**HTTP Status Codes:**
- `200` - Successful calculation
- `500` - Internal server error (calculation timeout, Kafka issues)
- `400` - Bad request (invalid parameters)

### Calculator Module

Arithmetic Errors

Division by zero: Returns error message "Division by zero is not allowed"
Overflow conditions: Handled by BigDecimal precision limits

Invalid Operations

Unsupported operation: Returns error "Unsupported operation: [operation]"
Null requests: Logged and ignored

Processing Errors

JSON deserialization: Handled by Kafka configuration
Kafka publishing failures: Logged but don't block processing


## Development

### Project Structure
```
src/main/java/com/calculator/rest/
├── CalculatorController.java       # REST endpoints
├── CalculatorKafkaService.java     # Kafka producer/consumer
├── KafkaConfigRest.java           # Kafka configuration  
├── KafkaHealthController.java     # Debug endpoints
├── RestApplication.java           # Spring Boot main class
└── model/
    ├── OperationRequest.java      # Request model
    └── OperationResult.java       # Response model

src/main/java/com/calculator/calculator/
├── CalculatorApplication.java    	 # Spring Boot main class
├── CalculatorKafkaConsumer.java    # Kafka message consumer and Core calculation logic
├── KafkaConfigCalculator.java      # Kafka configuration
└── model/
    ├── OperationRequest.java       # Input model  
    └── OperationResult.java        # Output model
```
