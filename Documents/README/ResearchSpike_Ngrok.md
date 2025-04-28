# Ngrok Integration For "WebStore"
### SpringBoot Based Application

This research spike demonstrates how to access REST APIs through an Ngrok tunnel.

## Overview

Ngrok creates secure tunnels to expose your local web server to the internet, allowing you to:
- Test webhooks without deploying
- Share your local development environment
- Access your development environment from anywhere

This project demonstrates Ngrok integration with a Spring Boot application that provides CRUD operations for user management.

## Architecture

The architecture consists of:

1. **Spring Boot Application**: A RESTful API service with the following components:
   - Controllers: Handle HTTP requests
   - Services: Implement business logic
   - Repositories: Interface with the database

2. **Ngrok Client**: Creates a secure tunnel from the internet to your local web server

3. **Client Applications**: Any external client that needs to access your API

## Prerequisites

- Java 17 or later
- Maven or Gradle
- Ngrok account (sign up at [ngrok.com](https://ngrok.com))
- PostgreSQL database (or any database of your choice)

## Installation

### Mac OS

1. **Install Ngrok using Homebrew**:
   ```bash
   brew install ngrok
   ```

2. **Authenticate Ngrok**:
   ```bash
   ngrok config add-authtoken <YOUR_AUTH_TOKEN>
   ```

3. **Verify installation**:
   ```bash
   ngrok -v
   ```

### Windows

1. **Download Ngrok**:
   - Visit [ngrok.com/download](https://ngrok.com/download)
   - Download the Windows ZIP file
   - Extract the ZIP file to a location of your choice

2. **Add to PATH (optional but recommended)**:
   - Right-click "This PC" and select "Properties"
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Under System Variables, find and select "Path"
   - Click "Edit"
   - Click "New" and add the path to your ngrok.exe
   - Click "OK" on all dialogs

3. **Authenticate Ngrok** (open Command Prompt):
   ```cmd
   ngrok config add-authtoken <YOUR_AUTH_TOKEN>
   ```

### Linux

1. **Install Ngrok**:
   ```bash
   curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | \
     sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && \
     echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | \
     sudo tee /etc/apt/sources.list.d/ngrok.list && \
     sudo apt update && sudo apt install ngrok
   ```

   Alternatively, download and install manually:
   ```bash
   wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz
   tar xvzf ngrok-v3-stable-linux-amd64.tgz
   sudo mv ngrok /usr/local/bin
   ```

2. **Authenticate Ngrok**:
   ```bash
   ngrok config add-authtoken <YOUR_AUTH_TOKEN>
   ```

## Implementation

### 1. Spring Boot Application Structure

```
src/main/java/com/webstore/
├── WebStoreApplication.java
├── controller
│   └── UserController.java
├── dto
│   ├── request
│   │   └── UserRequestDto.java
│   └── response
│   │   └── UserResponseDto.java
├── entity
│   └── User.java
├── repository
│   └── UserRepository.java
├── service
│   └──UserService.java
├── implementation
│   └── UserServiceImplementaiton.java
└── validation
    └── UserValidation.java
```

### 2. Application Configuration

Ensure your `application.properties` includes:

```properties
# Server configuration
server.port=8080
server.address=0.0.0.0

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/webstore
spring.datasource.username=postgres
spring.datasource.password=postgres
```

The `server.address=0.0.0.0` setting is important to allow external access.

## Usage

### 1. Start the Spring Boot Application
### 2. Start Ngrok Tunnel

```bash
ngrok http 8080
```

This will display output similar to:

```
Session Status                online
Account                       Your Account
Version                       x.x.x
Region                        United States (us)
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://abc123.ngrok.io -> http://localhost:8080
```

### 3. Access Your APIs

You can now access your APIs through the Ngrok URL:

#### Get all users
```
GET https://abc123.ngrok.io/api/users
```

#### Get a specific user
```
GET https://abc123.ngrok.io/api/users/1
```

#### Create a new user
```
POST https://abc123.ngrok.io/api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "USER"
}
```

#### Update a user
```
PUT https://abc123.ngrok.io/api/users/1
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john.updated@example.com",
  "fullName": "John Doe Updated",
  "role": "ADMIN"
}
```

#### Delete a user
```
DELETE https://abc123.ngrok.io/api/users/1
```

### 4. Monitor Traffic

Ngrok provides a web interface to monitor requests at:
```
http://127.0.0.1:4040
```

## Security Considerations

When using Ngrok, especially in development:

1. **Do not expose sensitive data** - Be cautious about what data is accessible through your API
2. **Use authentication** - Implement proper authentication mechanisms
3. **Temporary usage** - Ngrok is primarily for development/testing, not production
4. **Secure your auth-token** - Do not share your Ngrok auth-token
5. **Monitor traffic** - Regularly check the Ngrok inspection interface

## References

1. [Ngrok Documentation](https://ngrok.com/docs)
2. [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
