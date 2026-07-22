# Nexus Recur Subscription Module

Subscription MVP implemented with Java 17, Spring Boot 3, and Vue + Vite.

The backend follows a DDD-style package layout. The frontend is named with the requested `frontend-xxx` convention: `frontend-subscription`.

## Project Layout

- `pom.xml`: Maven parent project.
- `subscription-service`: Spring Boot subscription backend.
- `frontend-subscription`: Vue + Vite frontend with Docker and Nginx deployment files.

Backend package layout:

- `domain/model`: entities, enums, aggregate state.
- `domain/repository`: repository interfaces.
- `domain/service`: domain services, including the subscription state machine.
- `application/service`: use case orchestration for plans, subscriptions, webhooks, and entitlements.
- `application/port`: external ports, such as the payment gateway client.
- `infrastructure`: port implementations and support utilities.
- `interfaces/rest`: REST controllers, DTOs, API response, and exception mapping.

## Backend Features

- Plan CRUD: `POST/GET/PUT /subscription/plans`, `POST /subscription/plans/{planId}/archive`
- Subscription create/list/detail/cancel/pause/resume/upgrade
- Invoice list: `GET /subscriptions/{subscriptionId}/invoices`
- Subscription webhook: `POST /webhooks/subscription`
- Entitlement check: `GET /entitlements/check?userId=...`

Response format:

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {}
}
```

## Local Development

For step-by-step usage, API examples, Webhook signing, deployment, and coverage instructions, see [USAGE.md](USAGE.md).

PostgreSQL:

```bash
docker compose up -d postgres
```

Backend:

```bash
mvn -pl subscription-service spring-boot:run
```

Frontend:

```bash
cd frontend-subscription
npm install
npm run dev
```

The frontend uses `/api` by default and proxies requests to `http://localhost:8080` in development. Override `VITE_API_PROXY_TARGET` in `frontend-subscription/.env` when needed.

## Frontend Deployment

Production build:

```bash
cd frontend-subscription
npm run build
```

Docker image:

```bash
cd frontend-subscription
docker build -t frontend-subscription:latest .
```

The Nginx container serves static assets and proxies `/api/*` to `http://subscription-service:8080/`. Change `frontend-subscription/nginx.conf` if the backend service name or address differs.

## Webhook Signature

`subscription.webhook-secret` defaults to `change-me` in `application.yml`.

Webhook requests must include:

```http
X-Webhook-Signature: <hex hmac-sha256(rawBody, webhookSecret)>
```

Example payload:

```json
{
  "eventType": "subscription.paid",
  "subscriptionId": "sub_xxx",
  "periodStart": "2026-07-13T00:00:00Z",
  "periodEnd": "2026-08-13T00:00:00Z",
  "amount": 29.9,
  "currency": "USD",
  "externalTransactionId": "txn_xxx"
}
```

## Database

The default runtime database is PostgreSQL. A local PostgreSQL service is provided in `docker-compose.yml`.

Default connection:

```text
DB_URL=jdbc:postgresql://localhost:5432/nexus_recur
DB_USERNAME=nexus_recur
DB_PASSWORD=nexus_recur
```

JPA creates these tables from entities:

- `subscription_plans`
- `subscriptions`
- `subscription_invoices`
- `subscription_events`
