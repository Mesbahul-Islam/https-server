# HTTPS Server in Java - Astronomical Observation Platform

This is a comprehensive HTTPS server implementation in Java that handles astronomical observation data with user authentication, message encryption/decryption, and weather integration. The project demonstrates advanced server-side programming concepts including SSL/TLS security, database operations, and RESTful API design.

## Project Overview

The server provides a secure platform for astronomical researchers to:
- **Register and authenticate users** with secure credential management
- **Store and retrieve observation records** with JSON-based data exchange
- **Encrypt/decrypt sensitive messages** using cipher algorithms
- **Search through observation data** with query parameters
- **Integrate weather data** for observatory conditions
- **Update existing records** with modification tracking

## Key Features

### 🔐 Security & Authentication
- **HTTPS/SSL encryption** for all communications
- **Basic HTTP authentication** with username/password
- **User registration system** with duplicate prevention
- **Secure credential storage** in SQLite database

### 📡 API Endpoints
- **`/registration`** - User registration (POST) and validation (GET)
- **`/datarecord`** - CRUD operations for observation records
- **`/search`** - Query and filter observation data
- **`/decipher`** - Decrypt encrypted message payloads

### 🗄️ Data Management
- **SQLite database integration** for persistent storage
- **JSON data format** for API communication
- **Observatory location tracking** (latitude/longitude)
- **Weather condition recording** (temperature, cloudiness, light pollution)
- **Message modification history** with timestamps and reasons

## Technical Architecture

The server is built with a modular architecture:
- **Main HTTP handler** with GET/POST/PUT operations
- **Authentication system** with user management
- **Database layer** for persistent storage
- **Search functionality** with query parameters
- **External service integration** (weather, decipher)

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven for dependency management
- SSL certificate for HTTPS (keystore.jks)

### Running the Server
```bash
# Compile the project
mvn compile

# Start the main server (requires SSL certificate)
java -cp target/classes com.o3.server.Server keystore.jks password
```

### Running Auxiliary Services (for testing)
Feature 5 tests require that you have the mock weather server running:
```bash
java -jar weatherserver.jar
```
This will start the server on port 4001. You can get info about the server by visiting `http://localhost:4001/wfs`.

Feature 8 tests require that you have the decipher server running:
```bash
java -jar decipherserver.jar
```
This will start the server on port 4002. You can get info about the server by visiting `http://localhost:4002/decipher`.

## Testing

This repository contains comprehensive tests for the Spring 2025 programming 3 course.
To run the tests locally, open this folder in your editor of choice and run the tests from there.
For VSCode, the tests can be found under the `Testing` tab on the left side of the window.
By expanding the `tests > com.o3.tests` folder, you can see all the tests that are available.

The tests can also be run from the command line:
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Run with custom server address
SERVER_ADDRESS=https://localhost:8001 mvn test
```

## API Usage Examples

### Register a User
```bash
curl -X POST https://localhost:8001/registration \
  -H "Content-Type: application/json" \
  -d '{"username":"astronomer","password":"secret","email":"user@example.com","userNickname":"StarGazer"}'
```

### Send Observation Data
```bash
curl -X POST https://localhost:8001/datarecord \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'astronomer:secret' | base64)" \
  -d '{"recordIdentifier":"OBS001","recordDescription":"Galaxy observation","recordPayload":"M31 Andromeda"}'
```

### Search Observations
```bash
curl "https://localhost:8001/search?query=galaxy&limit=10" \
  -H "Authorization: Basic $(echo -n 'astronomer:secret' | base64)"
```

## Project Structure
```
src/
├── main/java/com/o3/
│   ├── server/          # Server implementation
│   └── tests/           # Test utilities and models
├── test/java/com/o3/    # JUnit test suites
└── resources/           # Configuration files
```