# Ecom Microservices

A Spring Boot–based microservices e-commerce backend built around independently deployable services for orders, products, users, and notifications, backed by centralized configuration, service discovery, a single-entry API gateway, and full observability tooling.

## Architecture Overview

```
                                   ┌─────────────────┐
                                   │  Config Server   │
                                   │  (centralized     │
                                   │   config)          │
                                   └────────▲──────────┘
                                            │ pulls config
                                            │
                        ┌───────────────────┼───────────────────┐
                        │                   │                   │
                ┌───────▼──────┐    ┌───────▼──────┐    ┌───────▼──────┐
                │  Eureka       │◄───┤  Order       │    │  Product     │
                │  Server        │    │  Service      │    │  Service      │
                │  (discovery)   │    │  (PostgreSQL) │    │  (PostgreSQL) │
                └───────▲───────┘    └───────┬───────┘    └───────▲──────┘
                        │                    │ RabbitMQ           │
                        │            ┌───────▼──────┐             │
                        │            │ Notification │             │
                        │            │  Service      │             │
                        │            └──────────────┘             │
                        │                                          │
                ┌───────┴───────┐                          ┌───────┴──────┐
                │  User Service  │                          │   Gateway     │
                │  (MongoDB)     │                          │  (single API  │
                └───────▲───────┘                          │   entry point)│
                        │                                   └───────▲──────┘
                        └───────────────────────────────────────────┘
                                            │
                                     Client Requests

        Observability: Prometheus (metrics) · Grafana (dashboards) · Zipkin (tracing)
```

All services (except Config Server) register with **Eureka** for service discovery. The **Gateway** is the single entry point for all client traffic and routes requests to the appropriate downstream service.

## Services

### Config Server
Centralized configuration management for all microservices. Every service (except itself) fetches its configuration from this server at startup, enabling environment-specific configs to be managed in one place without redeploying services.

### Eureka Server
Service registry and discovery server. All microservices (except Config Server) register themselves here, allowing the Gateway and other services to discover and communicate with each other dynamically without hardcoded URLs.

### Gateway
The single entry point (API Gateway) for the entire application. Routes incoming client requests to the appropriate downstream microservice, and can handle cross-cutting concerns such as routing, load balancing, and security.

### Order Service
Manages orders and shopping carts.
- **Database:** PostgreSQL
- Publishes events (e.g., order placed) to RabbitMQ for consumption by the Notification Service

### Product Service
Manages products and related details such as stock levels and discounts.
- **Database:** PostgreSQL

### User Service
Manages all user accounts and related data.
- **Database:** MongoDB

### Notification Service
Listens for events (e.g., order placed) via **RabbitMQ** and pushes notifications to users asynchronously, decoupling notification delivery from the core order flow.

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 20+, Spring Boot 4 |
| Service Discovery | Netflix Eureka |
| Centralized Config | Spring Cloud Config Server |
| API Gateway | Spring Cloud Gateway |
| Messaging | RabbitMQ |
| Relational Database | PostgreSQL (Order, Product services) |
| NoSQL Database | MongoDB (User service) |
| Metrics | Prometheus |
| Dashboards | Grafana |
| Distributed Tracing | Zipkin |
| Containerization | Docker / Docker Compose |

## Project Structure

```
ecom-microservices/
├── additional/
│   ├── evaluate-loki/
│   └── evaluate-prometheus/
│       └── docker-compose.yml   # Prometheus, Grafana, Zipkin, etc. (observability stack)
├── configserver/                # Centralized configuration server
├── eureka/                      # Service discovery / registry server
├── gateway/                     # API Gateway - single entry point
├── notification/                # Consumes RabbitMQ events, sends notifications
├── order/                       # Manages orders & carts (PostgreSQL)
├── product/                     # Manages products, stock, discounts (PostgreSQL)
├── user/                        # Manages users (MongoDB)
├── docker-compose.yml            # PostgreSQL, pgAdmin, RabbitMQ
└── logs/
```

## Getting Started

### Prerequisites
- Java 20+
- Maven
- Docker & Docker Compose
- MongoDB (for User Service; run separately or via your own setup)

### Running the Application

1. **Start core infrastructure** (PostgreSQL, pgAdmin, RabbitMQ) using the root `docker-compose.yml`:
   ```bash
   docker-compose up -d
   ```

2. **Start observability stack** (Prometheus, Grafana, Zipkin, etc.) using the compose file in `additional/evaluate-prometheus`:
   ```bash
   cd additional/evaluate-prometheus
   docker-compose up -d
   ```

3. **Start the Spring Boot services individually (recommended order):**
   ```bash
   # 1. Config Server must start first
   cd configserver && ./mvnw spring-boot:run

   # 2. Eureka Server
   cd eureka && ./mvnw spring-boot:run

   # 3. Core services (order, product, user, notification)
   cd order && ./mvnw spring-boot:run
   cd product && ./mvnw spring-boot:run
   cd user && ./mvnw spring-boot:run
   cd notification && ./mvnw spring-boot:run

   # 4. Gateway last, once all services are registered
   cd gateway && ./mvnw spring-boot:run
   ```

4. **Access the application** through the Gateway's exposed port (single entry point for all API calls).

## Observability & Monitoring

- **Prometheus** scrapes metrics exposed by each service (via Spring Boot Actuator).
- **Grafana** visualizes metrics collected by Prometheus through dashboards.
- **Zipkin** provides distributed tracing across service calls, helping trace a single request as it flows through the Gateway, Order, Product, User, and Notification services.

## Event-Driven Flow (Order Placement Example)

1. Client places an order via the **Gateway** → routed to **Order Service**.
2. **Order Service** persists the order in **PostgreSQL** and publishes an `OrderPlaced` event to **RabbitMQ**.
3. **Notification Service** consumes the event from RabbitMQ and pushes a notification to the user.
