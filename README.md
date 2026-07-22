# Nexus Recur — Subscription Payment Platform

订阅支付平台，基于 Java 17 + Spring Boot 3.3 + Vue 3 + Vite。

## Project Layout

```
nexus-recur/
├── pom.xml                        Maven root (modules: [backend])
├── backend/
│   ├── pom.xml                    Aggregator (nexus-recur-backend)
│   ├── subscription-service/      订阅业务服务 (port 8080)
│   └── payment-gateway/           支付编排引擎 (port 8081)
├── frontend-subscription/         Vue 3 管理控制台
├── docker-compose.yml             PostgreSQL 本地环境
├── ROADMAP.md                     分阶段实施路线
└── USAGE.md                       API 使用文档
```

## Architecture

```
frontend-subscription (Vue 3)
        │
        ▼
subscription-service (port 8080)          payment-gateway (port 8081)
┌─────────────────────────────┐          ┌──────────────────────────────────┐
│ DDD: domain / application / │──REST──▶ │ Payment Orchestration Engine     │
│ infrastructure / interfaces │ ◀webhook─│  ├── RoutingEngine (weighted)    │
│                             │          │  ├── CircuitBreaker (per-connector)│
│ PaymentGatewayClient (port) │          │  ├── FailoverPolicy              │
│  ├── MockAdapter (default)  │          │  └── PaymentProviderDispatcher   │
│  └── RestAdapter (gateway)  │          │       ├── MockAlpha (100% ok)    │
└─────────────────────────────┘          │       └── MockBeta  (70% ok)     │
                                         └──────────────────────────────────┘
```

**Design Patterns (payment-gateway):**
- Strategy: `PaymentProviderService` interface, per-provider implementations
- Dispatcher: auto-discovers providers via Spring DI, routes by brand
- Weighted Routing: config-driven traffic distribution with circuit breaker awareness
- Circuit Breaker: 3 consecutive retryable failures → open 30s → half-open probe
- Failover Policy: hard declines (card_declined, etc.) never retry; soft failures trigger next connector
- 3-Phase Orchestration: setup → retry loop (max 3 attempts) → finalize + merchant webhook

**Design Patterns (subscription-service):**
- Hexagonal (Port & Adapter): `PaymentGatewayClient` port in application layer
- Conditional Adapter: `@ConditionalOnProperty` switches Mock vs Rest implementation
- DDD: domain/model, domain/repository, application/service, infrastructure, interfaces/rest

## Backend Features

**subscription-service:**
- Plans: CRUD + archive (`/v1/plans`)
- Subscriptions: create/list/detail/cancel/pause/resume/upgrade (`/v1/subscriptions`)
- MIT Billing: 周期扣款调度 + 1/3/7/14 自动重试 + 试用转付费
- Dunning: 3 封邮件序列（失败/最后机会/取消通知）
- Tax: inclusive/exclusive/none 模式，国别税率（UK 20%, DE/FR 21% 等）
- Invoices + Events: per-subscription history
- Entitlements: check by userId (`/v1/entitlements/check`)
- Wallets: multi-currency balance + freeze/unfreeze + transactions (`/v1/wallets`)
- Merchants: 注册 + KYC 审核 + 自动开户 (`/v1/merchants`)
- Settlements: 结汇发起/审批/拒绝/完成，>$10K 需审批 (`/v1/settlements`)
- Dashboard: MRR, charge success rate, pending actions (`/v1/dashboard/stats`)
- Webhooks: inbound (subscription events) + outbound (event delivery with retry)
- API Keys: generate/list/revoke (`/v1/api-keys`)
- Audit Log: 敏感操作审计 + 查询 (`/v1/audit-logs`)
- Permission: `@CheckPermission` via flow-permission-client (optional, disabled by default)
- Observability: Actuator health/info/metrics/prometheus (`/actuator`)
- Security: RateLimitFilter (120 req/min/IP) + SecurityHeadersFilter (CSP/HSTS/nosniff)

**payment-gateway:**
- Payment Intents: create → confirm → succeeded/failed (`/v1/payments/intents`)
- Refunds: create + status (`/v1/payments/refunds`)
- Merchant Webhook: notifies subscription-service on payment completion

Response format (subscription-service):

```json
{"success": true, "code": "OK", "message": "success", "data": {}}
```

## Local Development

```bash
# PostgreSQL
docker compose up -d postgres

# subscription-service (port 8080)
mvn -pl backend/subscription-service spring-boot:run

# payment-gateway (port 8081)
mvn -pl backend/payment-gateway spring-boot:run

# frontend (port 5173, proxies /api → 8080)
cd frontend-subscription && npm install && npm run dev
```

Enable payment orchestration (subscription-service calls payment-gateway):

```bash
PAYMENT_GATEWAY_ENABLED=true mvn -pl backend/subscription-service spring-boot:run
```

Without this flag, subscription-service uses its built-in MockPaymentGatewayClient.

## Frontend

Vue 3 + Pinia + Vue Router. Views:
- Dashboard: metric cards (MRR, active subs, charge rate), revenue by currency, pending actions, recent events
- Plans: create/archive plans
- Subscriptions: list + detail page (invoices, events, actions)
- Wallets: multi-currency balances + transaction history
- Settlements: 结汇申请列表 + 发起/审批操作
- Entitlements: check user access

## Database

PostgreSQL (prod), H2 (test). Flyway migrations in `backend/subscription-service/src/main/resources/db/migration/` (V1-V6).

```text
DB_URL=jdbc:postgresql://localhost:5432/nexus_recur
DB_USERNAME=nexus_recur
DB_PASSWORD=nexus_recur
```

## Webhook Signature

`subscription.webhook-secret` defaults to `change-me`.

```http
X-Webhook-Signature: <hex hmac-sha256(rawBody, webhookSecret)>
```

## Tests

```bash
mvn clean test   # runs all modules (54 tests total)
```
