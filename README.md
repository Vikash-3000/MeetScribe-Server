# MeetScribe Auth Infrastructure

Production-ready authentication system with API Gateway, JWT authentication, and Google OAuth2 login.

## Architecture

Client → API Gateway → Auth Service → PostgreSQL + Redis

The system uses a gateway-based architecture where the gateway handles routing while the backend service manages authentication and business logic.

## Features

- API Gateway using Spring Cloud Gateway
- JWT access + refresh token authentication
- Google OAuth2 login
- One-device login enforcement
- Secure password hashing
- Dockerized deployment
- Production deployment on Render
- Health monitoring using UptimeRobot

## Tech Stack

Backend
- Spring Boot
- Spring Security
- Spring Cloud Gateway

Infrastructure
- PostgreSQL
- Redis
- Docker

Authentication
- JWT
- OAuth2 (Google)

Deployment
- Render
- UptimeRobot monitoring

## System Architecture

![Architecture Diagram](docs/architecture.png)

## API Endpoints

### Signup

POST /users

### Login

POST /api/auth/login

### Refresh Token

POST /api/auth/refresh

### OAuth Login

GET /oauth2/authorization/google

## Deployment

Backend and Gateway are deployed on Render using Docker containers.

Health monitoring is implemented via:

GET /actuator/health

## Future Improvements

- Distributed rate limiting
- Meeting service
- WebSocket signaling
- Event-driven architecture

## Author

Bikas Pulukulu
