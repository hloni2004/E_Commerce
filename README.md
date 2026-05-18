# E-Commerce API

## Project Title
E-Commerce

## Short Description
An e-commerce backend built with Spring Boot that manages users, products, carts, orders, payments, shipping, reviews, promotions, and related admin workflows.

## Problem It Solves
This project provides the server-side foundation for an online store, handling authentication, catalog management, checkout, inventory, and order processing in one centralized API.

## Features
- JWT-based authentication and role-based authorization
- Product, category, colour, size, and image management
- Cart and checkout workflow
- Orders, order items, payments, and shipments
- Promo code handling
- Reviews and product feedback
- Address management
- Email support for transactional notifications
- Security controls including CORS, CSP, HTTPS enforcement, rate limiting, and no-cache headers
- Supabase-backed storage support for uploaded media

## Tech Stack
- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- PostgreSQL
- H2 Database
- JWT (JJWT)
- Caffeine Cache
- Bucket4j Rate Limiting
- OkHttp
- Spring Mail

## Screenshots
No screenshots are currently included in the repository. Add embedded screenshots of the storefront, admin dashboard, checkout flow, and API responses when available.

## Installation Steps
1. Clone the repository.
2. Make sure Java 21 and Maven are installed.
3. Create the required environment variables:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `MAILJET_API_KEY`
   - `MAILJET_API_SECRET`
   - `MAIL_SENDER_EMAIL`
   - `MAIL_SENDER_NAME`
   - `SPRING_MAIL_HOST`
   - `SPRING_MAIL_PORT`
   - `SPRING_MAIL_USERNAME`
   - `SPRING_MAIL_PASSWORD`
   - `JWT_SECRET`
   - `SUPABASE_URL`
   - `SUPABASE_SERVICE_KEY`
   - `APP_CORS_ALLOWED_ORIGINS`
   - `PORT` (optional)
4. Configure the database and external services.
5. Run the application with Maven.

## How to Run It
```bash
mvn clean spring-boot:run
```

The application starts on port `8080` by default, or the value provided in `PORT`.

## API Documentation
### Public endpoints
- `GET /` — application status
- `GET /health` — health check

### Main resource groups
The API is organized under `/api/*` and includes the following resources:
- `/api/users`
- `/api/products`
- `/api/categories`
- `/api/cart`
- `/api/cart-items`
- `/api/orders`
- `/api/order-items`
- `/api/payments`
- `/api/shipments`
- `/api/reviews`
- `/api/promos`
- `/api/addresses`
- `/api/shipping-methods`
- `/api/product-colours`
- `/api/product-colour-sizes`
- `/api/product-images`
- `/api/inventory`
- `/api/email`
- `/api/checkout`
- `/api/roles`
- `/api/permissions`
- `/api/csrf`

Most resources expose standard create, read, update, and delete operations, while checkout, payment, and email endpoints support workflow-specific actions.

## Folder Structure
```text
src/main/java/za/ac/styling/
├── config/          # Application, security, cache, CORS, and startup configuration
├── controller/      # REST controllers
├── domain/          # JPA entities and enums
├── dto/             # Request and response payloads
├── events/          # Domain events
├── factory/         # Entity factory helpers
├── filter/          # Security and response filters
├── listener/        # Event listeners
├── repository/      # Spring Data repositories
├── security/        # JWT utilities and authentication filters
├── service/         # Service interfaces and implementations
└── util/            # Shared helper classes

src/main/resources/
└── application.properties

docs/
└── SECURITY_OVERVIEW.md
```

## Future Improvements
- Add API examples and response schemas for each endpoint
- Publish Swagger/OpenAPI documentation
- Add automated integration tests
- Add deployment and environment setup guides
- Add screenshots for the UI and API flows

## Author / Contact Info
Maintained by: hloni2004 | GitHub: https://github.com/hloni2004
