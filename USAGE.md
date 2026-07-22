# 订阅模块使用说明

本文说明如何在本地启动订阅模块、使用前端页面、调用后端 API、模拟 Webhook，以及查看测试覆盖率。

## 1. 环境要求

- JDK 17
- Maven 3.8+
- Node.js 22+
- npm

项目结构：

```text
nexus-recur/
  subscription-service/      后端 Spring Boot 服务
  frontend-subscription/     前端 Vue + Vite 应用
```

## 2. 启动 PostgreSQL

默认运行时数据库是 PostgreSQL。项目根目录已提供 `docker-compose.yml`：

```bash
docker compose up -d postgres
```

默认连接信息：

```text
Host: localhost
Port: 5432
Database: nexus_recur
User: nexus_recur
Password: nexus_recur
```

后端默认读取这些环境变量：

```text
DB_URL=jdbc:postgresql://localhost:5432/nexus_recur
DB_USERNAME=nexus_recur
DB_PASSWORD=nexus_recur
JPA_DDL_AUTO=update
```

如果不设置环境变量，会使用上面的默认值。

## 3. 启动后端

在项目根目录执行：

```bash
mvn -pl subscription-service spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

默认配置文件：

```text
subscription-service/src/main/resources/application.yml
```

关键配置：

```yaml
subscription:
  webhook-secret: change-me
  checkout-base-url: https://checkout.example.local
```

数据库配置：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/nexus_recur}
    username: ${DB_USERNAME:nexus_recur}
    password: ${DB_PASSWORD:nexus_recur}
```

## 4. 启动前端

首次进入前端目录安装依赖：

```bash
cd frontend-subscription
npm install
```

启动开发服务：

```bash
npm run dev
```

默认访问：

```text
http://localhost:5173
```

开发环境前端默认请求 `/api`，由 Vite 代理到：

```text
http://localhost:8080
```

如需改后端地址，修改：

```text
frontend-subscription/.env
```

```env
VITE_API_PROXY_TARGET=http://localhost:8080
```

## 5. 前端功能

前端页面提供三个主要视图：

- 计划：创建订阅计划、查看计划列表、下架计划
- 订阅：创建订阅、查看订阅列表、暂停、恢复、取消
- 权益：按 `userId` 校验用户是否有有效订阅权益

创建订阅后，后端会返回一个模拟 checkout URL。当前支付网关是 `MockPaymentGatewayClient`，实际扣费能力后续可通过实现 `PaymentGatewayClient` 替换。

## 6. 后端 API

统一响应格式：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {}
}
```

### 6.1 创建订阅计划

```http
POST /subscription/plans
Content-Type: application/json
```

```json
{
  "name": "Pro Plan",
  "description": "Professional subscription plan",
  "productId": "prod_pro",
  "billingCycle": "monthly",
  "price": 29.9,
  "currency": "USD",
  "trialDays": 0,
  "features": {
    "max_api_calls": 10000,
    "storage_gb": 50,
    "premium_support": true
  },
  "status": "active"
}
```

支持的 `billingCycle`：

- `monthly`
- `3month`
- `6month`
- `yearly`

### 6.2 查询计划

```http
GET /subscription/plans?page=1&limit=20&status=active
GET /subscription/plans/{planId}
```

### 6.3 更新和下架计划

```http
PUT /subscription/plans/{planId}
POST /subscription/plans/{planId}/archive
```

### 6.4 创建订阅

```http
POST /subscriptions
Content-Type: application/json
```

```json
{
  "planId": "plan_xxx",
  "userId": "user_001",
  "successUrl": "https://example.com/subscription/success",
  "cancelUrl": "https://example.com/subscription/cancel"
}
```

返回示例：

```json
{
  "subscriptionId": "sub_xxx",
  "checkoutUrl": "https://checkout.example.local/checkout/sub_xxx",
  "status": "pending"
}
```

### 6.5 查询订阅

```http
GET /subscriptions?userId=user_001&status=active&page=1&limit=20
GET /subscriptions/{subscriptionId}
```

### 6.6 取消、暂停、恢复、升级订阅

到期取消：

```http
POST /subscriptions/{subscriptionId}/cancel
Content-Type: application/json
```

```json
{
  "immediate": false,
  "reason": "no longer needed"
}
```

立即取消：

```json
{
  "immediate": true,
  "reason": "refund requested"
}
```

暂停：

```http
POST /subscriptions/{subscriptionId}/pause
Content-Type: application/json
```

```json
{
  "reason": "temporary break",
  "maxPauseDays": 90
}
```

恢复：

```http
POST /subscriptions/{subscriptionId}/resume
```

升级或降级：

```http
POST /subscriptions/{subscriptionId}/upgrade
Content-Type: application/json
```

```json
{
  "newPlanId": "plan_new"
}
```

### 6.7 查询流水

```http
GET /subscriptions/{subscriptionId}/invoices?page=1&limit=20
```

### 6.8 权益校验

```http
GET /entitlements/check?userId=user_001
```

有有效订阅时返回 `200`，无有效权益时返回 `402 Payment Required`。

## 7. Webhook 使用

Webhook 地址：

```http
POST /webhooks/subscription
```

请求头：

```http
X-Webhook-Signature: <hex hmac-sha256(rawBody, webhookSecret)>
Content-Type: application/json
```

默认 `webhookSecret` 是：

```text
change-me
```

支持事件：

- `subscription.active`
- `subscription.trialing`
- `subscription.paid`
- `subscription.past_due`
- `subscription.canceled`
- `subscription.scheduled_cancel`
- `subscription.expired`
- `subscription.paused`
- `subscription.update`

续费 Webhook 示例：

```json
{
  "eventType": "subscription.paid",
  "subscriptionId": "sub_xxx",
  "periodStart": "2026-07-13T00:00:00Z",
  "periodEnd": "2026-08-13T00:00:00Z",
  "amount": 29.9,
  "currency": "USD",
  "paymentMethod": "card",
  "externalTransactionId": "txn_xxx"
}
```

### 7.1 生成签名示例

PowerShell 示例：

```powershell
$secret = "change-me"
$body = '{"eventType":"subscription.paid","subscriptionId":"sub_xxx","periodStart":"2026-07-13T00:00:00Z","periodEnd":"2026-08-13T00:00:00Z","amount":29.9,"currency":"USD","externalTransactionId":"txn_xxx"}'
$hmac = [System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($secret))
$signature = [Convert]::ToHexString($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($body))).ToLower()
$signature
```

然后调用：

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/webhooks/subscription" `
  -Method Post `
  -Headers @{ "X-Webhook-Signature" = $signature } `
  -ContentType "application/json" `
  -Body $body
```

## 8. 测试和覆盖率

运行后端测试：

```bash
mvn -pl subscription-service clean test
```

当前测试覆盖：

- 计划 CRUD
- 订阅创建、激活、续费幂等
- 暂停、恢复、取消、升级
- Webhook 验签和事件分发
- REST API 和错误响应
- 权益校验
- 状态机合法/非法流转

查看覆盖率报告：

```text
subscription-service/target/site/jacoco/index.html
```

最近一次覆盖率：

- 行覆盖率：`88.26%`
- 指令覆盖率：`86.08%`
- 方法覆盖率：`84.98%`
- 分支覆盖率：`63.70%`

## 9. 前端生产构建和部署

构建静态资源：

```bash
cd frontend-subscription
npm run build
```

构建 Docker 镜像：

```bash
cd frontend-subscription
docker build -t frontend-subscription:latest .
```

Nginx 配置：

```text
frontend-subscription/nginx.conf
```

默认将 `/api/*` 代理到：

```text
http://subscription-service:8080/
```

如果部署环境中的后端服务名不同，需要修改 `proxy_pass`。

## 10. 常见问题

### 10.1 调用 Webhook 返回 401

检查 `X-Webhook-Signature` 是否使用原始请求体和 `subscription.webhook-secret` 计算。请求体格式化、空格、换行变化都会导致签名不同。

### 10.2 前端请求失败

确认后端已启动在 `8080`，并检查：

```text
frontend-subscription/.env
```

### 10.3 后端启动时报 PostgreSQL 连接失败

先启动 PostgreSQL：

```bash
docker compose up -d postgres
```

如果使用外部数据库，确认 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 指向正确实例。
