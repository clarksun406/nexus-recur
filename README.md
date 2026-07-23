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
- Usage Billing: 幂等上报 + 周期末结算（metered/tiered）+ 异常检测 (`/v1/usage`)
- Dunning: 3 封邮件序列（失败/最后机会/取消通知）
- Tax: inclusive/exclusive/none 模式，DB 税率查询 + 硬编码 fallback
- Invoices + Events: per-subscription history
- Entitlements: check by userId (`/v1/entitlements/check`)
- Customers: CRUD (`/v1/customers`)
- Payment Methods: list/create/set-default/revoke (`/v1/customers/{id}/payment-methods`)
- Refunds: create/approve/list (`/v1/refunds`)
- Wallets: multi-currency balance + freeze/unfreeze + transactions (`/v1/wallets`)
- FX Exchange: 23 种货币换汇，50bps 点差，异常保护 (`/v1/fx`)
- Payment Orders: SEPA/ACH/Wire 付款，制裁筛查，>$5K 审批 (`/v1/payment-orders`)
- Merchants: 注册 + KYC 审核 + 自动开 8 币种钱包 (`/v1/merchants`)
- Settlements: 结汇发起/审批/拒绝/完成，>$10K 需审批，批量结汇，合规导出，额度管理 (`/v1/settlements`)
- Reconciliation: 月度对账报告 + CSV 导出 (`/v1/reconciliation`)
- Customer Portal: 魔法链接登录 + 订阅/发票/支付方式自助管理 (`/v1/portal`)
- Developer Center: Webhook 端点 CRUD + 投递日志 (`/v1/webhook-endpoints`)
- Smart Routing: 多策略路由（priority/weighted/cost/success-rate）+ 条件匹配 (`/v1/routing-rules`)
- Virtual Cards: 虚拟卡发行/冻结/关闭 + 消费限额 (`/v1/virtual-cards`)
- Legal Entities: 多主体管理 + 商户分配 (`/v1/legal-entities`)
- License Keys: 密钥生成/校验/设备绑定/暂停/吊销 (`/v1/licenses`)
- Plan Tiers: 分层定价阶梯管理 (`/v1/plans/{id}/tiers`)
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

Vue 3 + Pinia + Vue Router. Views (16):
- Dashboard: metric cards (MRR, active subs, charge rate), revenue by currency, pending actions, recent events
- Plans: create/archive plans + tier 管理
- Subscriptions: list + detail page (invoices, events, actions)
- Wallets: multi-currency balances + transaction history
- Settlements: 结汇申请列表 + 发起/审批操作
- Entitlements: check user access
- Developer: API Keys 生成/吊销 + Webhook 端点管理/投递日志
- Payment Orders: SEPA/ACH/Wire 付款 + 制裁筛查 + 审批
- Usage: 用量上报 + 按订阅查询
- FX: 多币种换汇 + 交易历史
- Reconciliation: 月度对账报告 + CSV 导出
- Routing: 智能路由规则 CRUD + 策略配置 + 路由测试
- Virtual Cards: 虚拟卡发行/冻结/关闭 + 消费限额
- Entities: 多主体管理 + 商户分配
- Licenses: License Key 生成/校验/暂停/吊销

## Database

PostgreSQL (prod), H2 (test). Flyway migrations in `backend/subscription-service/src/main/resources/db/migration/` (V1-V14).

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
mvn clean test   # runs all modules (65 tests total)
```
