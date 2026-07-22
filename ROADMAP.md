# Nexus Recur Roadmap

> 基于《订阅管理 + 多币种资金闭环 PRD V1.0》与现有代码的差距分析，形成分阶段实施路线。
>
> PRD 来源：`pay-requirement/saas-subscription-payment-prd/saas-subscription-payment-prd.html`

## 现状总结

现有代码实现了一个 **Subscription MVP 骨架**，覆盖了 PRD 28 个功能点中的约 4 个（部分完成），整体完成度约 **15%**。

### 已有基础

| 能力 | 对应 PRD | 完成度 |
|------|----------|--------|
| Plan CRUD + 归档 | F01 | 40% — 缺 billing_type/metered_config/tax_mode/license 字段，billing_cycle 枚举不一致 |
| 订阅状态机 | F01-F04 | 60% — 8 种状态 + 流转守卫已实现，但缺 MIT 驱动、重试、dunning |
| Webhook 接收（入站） | F03 间接 | 30% — 仅接收外部 webhook 驱动状态变更，无出站 webhook 推送 |
| 发票记录 + 幂等 | F03 间接 | 40% — pay 事件幂等去重已实现，缺 tax 字段和 failed/refunded 状态 |
| 权益校验 | — | 80% — 按 userId 查有效订阅，返回 features |
| DDD 架构 | — | 90% — 端口适配器风格，PaymentGatewayClient 接口可替换 |
| 前端管理面板 | F21-F22 | 10% — 3 个 Tab（计划/订阅/权益），无仪表盘、无详情页、无权限 |

### 完全缺失

- 多币种钱包（F10-F15）— 0%
- 结汇回国通道（F16-F20）— 0%
- 客户自助门户（F27）— 0%
- License Key 管理（F28）— 0%
- 认证授权（Ch3）— 可复用 flow-permission 模块，见下文「认证授权复用方案」
- 用量计费（F05）— 0%
- 自动重试与 dunning（F04, F06）— 0%
- 税务计算（Ch5）— 0%
- 开发者中心（F25）— 0%
- 对账报表（F24）— 0%
- 定时调度（续期/过期/试用到期/到期取消）— 0%

---

## 功能差距分析（F01-F28）

### 模块一：订阅计费引擎（F01-F09）

| # | 优先级 | 功能 | 现有状态 | 差距 |
|---|--------|------|----------|------|
| F01 | P0 | 订阅计划管理 | 部分完成 | 缺 billing_type（flat_rate/metered/tiered）、metered_config、tax_mode/tax_category、license_enabled/license_instance_limit 字段；billing_cycle 枚举需改为 monthly/quarterly/annual；Plan ID 格式需对齐 plan_+24 字符；无 draft 状态（PRD 仅 active/archived，但现有多了 draft） |
| F02 | P0 | 信用卡收单(CIT) | 未开始 | 需接入真实收单机构，实现 hosted checkout 或 Payment Element，生成 payment_method_id 供 MIT 使用；现有 MockPaymentGatewayClient 需替换 |
| F03 | P0 | 周期性自动扣款(MIT) | 未开始 | 需定时调度（UTC 00:00 扫描 current_period_end 到期的 active 订阅），调用收单机构 MIT 接口扣款，推送 charge.succeeded/charge.failed webhook |
| F04 | P0 | 基础自动重试 | 未开始 | 需实现 1/3/7/14 固定重试策略，past_due 状态管理，decline_code 映射，dunning 邮件序列（3 封），超限自动取消 |
| F05 | P1 | 用量计费(metered) | 未开始 | 需 UsageRecord 实体、POST /v1/usage + /v1/usage/batch API、幂等键去重、周期末结算计算、异常用量检测（R-06） |
| F06 | P1 | 智能重试 | 未开始 | 需基于 card BIN + decline_code + 时间窗口的模型训练，Beta 阶段实现 |
| F07 | P1 | 免费试用转付费 | 部分完成 | trialing 状态已实现，但缺试用到期自动扣款逻辑、到期前 3 天提醒邮件 |
| F08 | P2 | 智能路由 | 未开始 | 多收单机构接入，基于 BIN/地区/历史成功率的路由策略 |
| F09 | P2 | 分层定价 | 未开始 | tiered 定价模型、阶梯价、组合定价 |

### 模块二：多币种钱包（F10-F15）

| # | 优先级 | 功能 | 现有状态 | 差距 |
|---|--------|------|----------|------|
| F10 | P0 | 多币种收款账户 | 未开始 | 需 Wallet 实体、KYC 后自动开户、USD/EUR/GBP 账户信息展示 |
| F11 | P0 | 多币种余额持有 | 未开始 | 需余额实时更新（≤5 秒）、pending_balance 冻结余额、收入/支出/FX/结汇流水记录 |
| F12 | P1 | 同币种付款 | 未开始 | 需付款订单、SEPA/ACH/Wire 清算、制裁名单筛查（OFAC/EU/UN）、审批流程 |
| F13 | P1 | 主动换汇 | 未开始 | 需汇率获取、点差 ≤0.5%、汇率异常保护（R-05）、换汇记录 |
| F14 | P2 | 企业虚拟卡 | 未开始 | Visa 虚拟卡发行、消费限额、品类限制、实时通知 |
| F15 | P2 | 扩展币种 | 未开始 | 扩展至 20+ 币种 |

### 模块三：结汇回国通道（F16-F20）

| # | 优先级 | 功能 | 现有状态 | 差距 |
|---|--------|------|----------|------|
| F16 | P0 | 结汇发起 | 未开始 | 需 Settlement 实体、余额/限额/合规校验、交易背景关联（background_refs）、银行账户验证 |
| F17 | P0 | 结汇审批 | 未开始 | 需 >$10K Owner 审批流程、审批通知、T+1~T+2 到账 |
| F18 | P1 | 合规申报数据 | 未开始 | 需自动关联收单记录、生成贸易背景数据、月/季外汇申报导出 |
| F19 | P2 | 批量结汇 | 未开始 | 批量发起、自动拆分合规申报 |
| F20 | P2 | 结汇额度管理 | 未开始 | 对接 SAFE 额度系统、实时可用额度展示 |

### 模块四：统一管理控制台（F21-F26）

| # | 优先级 | 功能 | 现有状态 | 差距 |
|---|--------|------|----------|------|
| F21 | P0 | 仪表盘 | 未开始 | 需 MRR/扣款成功率/活跃订阅数/各币种余额/本月结汇 指标卡，7/30/90 天筛选，趋势图，待办队列 |
| F22 | P0 | 订阅管理页 | 部分完成 | 有基础列表+筛选，缺详情页（扣款历史/用量/事件日志）、搜索增强、批量操作 |
| F23 | P0 | 钱包管理页 | 未开始 | 按币种分组的余额+流水明细、收入/支出/FX/结汇分类视图 |
| F24 | P1 | 对账报表 | 未开始 | 月度对账报告、CSV/Excel 导出、对账 API |
| F25 | P1 | 开发者中心 | 未开始 | API Key 管理（生成/吊销/sandbox）、Webhook 配置（事件订阅/重试/签名）、API 调用日志、交互式 API 文档 |
| F26 | P2 | 多主体管理 | 未开始 | 多公司主体、按主体查看数据、主体间资金调拨 |

### 模块五：客户门户与生态（F27-F28）

| # | 优先级 | 功能 | 现有状态 | 差距 |
|---|--------|------|----------|------|
| F27 | P1 | 客户自助门户 | 未开始 | 魔法链接登录、5 分钟有效期、1 小时会话、查看订阅/发票、更新支付方式、计划取消/恢复 |
| F28 | P2 | License Key 管理 | 未开始 | 密钥生成（XXXX-XXXX-XXXX-XXXX）、设备绑定、在线/离线校验、自动签发/暂停/吊销 |

---

## 跨模块基础设施差距

| 领域 | 现有状态 | 需补齐 |
|------|----------|--------|
| **认证授权** | 零认证 | **复用 flow-permission-client**：`@CheckPermission` 注解 + AOP 切面 + Caffeine 缓存客户端，8 个角色模板与 PRD 4 角色几乎一一对应。仅需自建 AuthFilter（Bearer Token → userId/merchantId request attribute）+ API Key 实体（bcrypt 存储）。详见下文「认证授权复用方案」 |
| **API 版本** | 路径前缀 /subscription/ | 需迁移至 /v1/ 前缀对齐 PRD 规范 |
| **出站 Webhook** | 无 | 15 种事件类型推送、指数退避重试（1m/5m/30m/2h/12h/24h）、签名验证（X-Signature） |
| **入站 Webhook** | 有基础 | 事件类型需对齐 PRD（现有 9 种 vs PRD 15 种），字段需补全 |
| **税务计算** | 无 | tax_mode/tax_category、接入 TaxJar/Avalara、EU VAT/UK VAT/US 州销售税 |
| **审计日志** | SubscriptionEvent 部分 | 需扩展至所有资金操作+权限操作+配置操作，保留 7 年，含操作人/角色/IP/设备指纹 |
| **数据库迁移** | Hibernate ddl-auto | 需引入 Flyway/Liquibase，生产环境不可依赖自动 DDL |
| **多租户** | 无 | 按 account_id 数据隔离，跨租户查询需显式 join |
| **定时调度** | 无 | 续期扣款、试用到期、到期取消、暂停到期恢复、用量结算 |
| **可观测性** | 无 | Actuator 健康检查、指标监控、日志聚合 |
| **前端** | 单文件 Vue SPA | 需路由、状态管理、组件拆分、权限控制、响应式设计 |

---

## 认证授权复用方案（flow-permission）

> 参考模块：`D:\github\nexus-flow\flow-permission`（含 flow-permission-server + flow-permission-client 两个子模块）

### 复用度评估：约 80%

flow-permission 提供 User→Role→Permission + scope 维度的 RBAC 模型，技术栈与 nexus-recur 完全一致（Spring Boot 3.3.x / Java 17 / PostgreSQL / Flyway），可直接复用，无需重建 RBAC 基础设施。

### 可直接复用

| 能力 | 说明 |
|------|------|
| RBAC 模型 | User→UserRole(scope)→Role→RolePermission→Permission，带 SYSTEM/MERCHANT/PROVIDER/ORGANIZATION scope 隔离 |
| `@CheckPermission` 注解 | 方法级声明式鉴权，`@CheckPermission("subscription:create")` 加在控制器方法上，AOP 切面自动拦截 |
| PermissionClient + Caffeine 缓存 | 远程查权限，60 秒 TTL 缓存，fail-closed（权限服务不可用时拒绝） |
| AutoConfiguration 机制 | 加 Maven 依赖即自动装配 PermissionClient + CheckPermissionAspect，零配置生效 |
| 角色模板 | MERCHANT_OWNER / MERCHANT_DEVELOPER / MERCHANT_FINANCE / MERCHANT_SUPPORT / MERCHANT_VIEWER — 与 PRD Owner / Developer / Finance / Support 几乎一一对应 |
| Flyway 迁移 | 4 个迁移脚本（建表→修索引→种权限→种角色），可作为 nexus-recur Flyway 的参照 |
| ServiceTokenFilter | flow-permission-server 自身的 service token 鉴权（常量时间比较，防时序攻击） |

### 需要适配

**1. 权限码扩展（V5 迁移脚本）**

flow-permission 现有 52 个权限码面向加密支付场景。nexus-recur 需新增订阅计费域权限码，保留可复用的（api_key:*、webhook:*、refund:*），替换不适用的（crypto_payment:*、fiat_ramp:*）：

| 新增权限码 | PRD 对应 |
|-----------|----------|
| `plan:read` / `plan:create` / `plan:update` / `plan:archive` | F01 |
| `subscription:read` / `subscription:create` | F01-F03 |
| `subscription:cancel` / `subscription:pause` / `subscription:resume` | F01 |
| `subscription:upgrade` | F01 Proration |
| `charge:read` | F03 MIT |
| `usage:report` / `usage:read` | F05 |
| `wallet:read` | F10-F11 |
| `wallet:payment:create` | F12 |
| `wallet:fx:create` | F13 |
| `settlement:initiate` / `settlement:approve` / `settlement:read` | F16-F17 |
| `settlement:export` | F18 |
| `reconciliation:read` / `reconciliation:export` | F24 |
| `license:activate` / `license:validate` / `license:revoke` | F28 |
| `customer_portal:generate_link` | F27 |
| `tax:read` | Ch5 税务 |
| `dashboard:read` | F21 |
| `developer_center:read` | F25 |

改动方式：新增 `V5__seed_subscription_permissions.sql`，seed 订阅域权限 + 更新 MERCHANT_* 角色权限映射，不动 V1-V4。

**2. AuthFilter（用户认证层）**

flow-permission 只做**授权**（你有什么权限），不做**认证**（你是谁）。它期望调用方已认证用户，通过 request attribute 传入 `userId` + `merchantId`。nexus-recur 需自建：

- `AuthFilter`：从 `Authorization: Bearer sk_live_*` 解析 API Key → 查 API Key 实体 → 提取 userId + merchantId → 设为 request attribute
- `ApiKey` 实体：`sk_live_*` / `sk_test_*` 格式，bcrypt 存储，权限范围（full_access / read_only / payments_only / settlements_only）
- 控制器加 `@CheckPermission("subscription:create")` 注解即可

**3. API Key 管理（F25 开发者中心）**

flow-permission 有 `api_key:*` 权限码但不管理 API Key 实体。API Key 实体 + 生成/吊销逻辑由 nexus-recur 自建。

### 集成方式

```xml
<!-- subscription-service/pom.xml -->
<dependency>
    <groupId>com.nexusflow</groupId>
    <artifactId>flow-permission-client</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```yaml
# subscription-service/src/main/resources/application.yml
permission:
  server-url: http://localhost:8090
  service-token: ${PERMISSION_SERVICE_TOKEN:}
  cache-ttl-seconds: 60
  enabled: true
```

```text
部署架构：
  flow-permission-server (port 8090)  ← 独立部署，共享 PostgreSQL
  subscription-service   (port 8080)  ← 引入 flow-permission-client
    ├── AuthFilter: Bearer Token → userId/merchantId → request attribute
    ├── @CheckPermission("subscription:create") on controllers
    └── PermissionClient (Caffeine cache → 远程查 flow-permission-server)
```

### PRD 角色映射

| PRD 角色 | flow-permission 角色 | 权限范围 |
|----------|---------------------|----------|
| Owner | MERCHANT_OWNER | 全部：plan:*, subscription:*, wallet:*, settlement:*, api_key:*, webhook:*, refund:approve |
| Developer | MERCHANT_DEVELOPER | plan:create/update, subscription:read, api_key:*, webhook:*, usage:report, developer_center:read |
| Finance | MERCHANT_FINANCE | wallet:read, wallet:payment:create, wallet:fx:create, settlement:initiate, reconciliation:export, settlement:export |
| Support | MERCHANT_SUPPORT | subscription:read, refund:create(needs approval), charge:read — 只读为主 |
| 终端客户 | （不适用控制台） | 通过客户门户自助操作，不走控制台权限体系 |

---

## 分阶段路线图

### Phase 0 — 技术债务清理与对齐（Week 1-2）

> 对齐 PRD 数据模型和 API 规范，为后续功能开发扫清障碍。

| 工作项 | 详情 |
|--------|------|
| API 路径迁移 | /subscription/plans → /v1/plans，/subscriptions → /v1/subscriptions，新增 /v1 前缀 |
| Plan 数据模型对齐 | 新增 billing_type、metered_config、tax_mode、tax_category、license_enabled、license_instance_limit 字段；billing_cycle 枚举改为 monthly/quarterly/annual；移除 draft 状态；ID 格式对齐 plan_+24 字符 |
| Subscription 数据模型对齐 | 新增 retry_count、last_decline_code、cancellation_reason 字段；customer_id 格式 cus_+24；payment_method_id 字段 |
| 数据库迁移引入 | 集成 Flyway，编写初始迁移脚本，关闭 ddl-auto |
| 出站 Webhook 框架 | 事件发布机制、签名生成、指数退避重试队列、Webhook 端点配置实体 |
| 认证授权集成 | 引入 flow-permission-client 依赖；自建 AuthFilter（Bearer Token → userId/merchantId request attribute）；API Key 实体 + bcrypt 存储；新增 V5 迁移脚本 seed 订阅域权限码。详见下文「认证授权复用方案」 |
| 前端脚手架升级 | 引入 vue-router、pinia、组件目录结构、API 拦截器 |

### Phase 1 — MVP 核心计费闭环（Week 3-10）

> PRD 里程碑：MVP 开发。目标：完成订阅计费引擎 + 税务 + 3 币种钱包 + 结汇通道。

**1A. 订阅计费引擎（F01-F04, F07）**

| 工作项 | PRD 对应 | 验收标准 |
|--------|----------|----------|
| 真实收单机构接入（CIT） | F02 | 托管 checkout 页面，Visa/MC/Amex 收单，生成 payment_method_id |
| MIT 周期扣款调度 | F03 | UTC 00:00 扫描到期订阅 → MIT 扣款 → 推送 charge.succeeded/failed |
| 固定重试策略 | F04 | 1/3/7/14 四次重试，past_due 状态管理，decline_code 映射 |
| Dunning 邮件序列 | F04 | 3 封邮件（失败即发/第 3 天最后机会/第 7 天取消通知），模板变量插值 |
| 试用转付费自动化 | F07 | 试用到期自动首扣，到期前 3 天提醒邮件 |
| scheduled_cancel 到期执行 | F01 | current_period_end 到期自动 canceled，可 resume |
| Proration 计算 | F01 | proration-charge-immediately / proration-charge / proration-none |
| 税务计算 | Ch5 | tax_mode inclusive/exclusive/none，接入 TaxJar，EU VAT/UK VAT/US 州销售税 |

**1B. 多币种钱包（F10-F11）**

| 工作项 | PRD 对应 | 验收标准 |
|--------|----------|----------|
| Wallet 实体与多币种账户 | F10 | KYC 后自动开 USD/EUR/GBP 钱包，account_number/routing_number 展示 |
| 余额实时更新 | F11 | 收单后 ≤5 秒余额可见，pending_balance 冻结/解冻 |
| 钱包流水记录 | F11 | 收入/支出/FX/结汇分类流水，可追溯 |

**1C. 结汇回国通道（F16-F17）**

| 工作项 | PRD 对应 | 验收标准 |
|--------|----------|----------|
| Settlement 实体与发起 | F16 | 币种+金额+银行账户+交易背景关联，余额/限额校验 |
| Owner 审批流程 | F17 | >$10K 需 Owner 审批，审批后提交持牌机构，T+1~T+2 到账 |
| 合规校验 | F16 | 交易背景金额匹配、银行账户验证、制裁名单筛查 |

**1D. 管理控制台基础（F21-F23）**

| 工作项 | PRD 对应 | 验收标准 |
|--------|----------|----------|
| 仪表盘 | F21 | MRR/扣款成功率/活跃订阅/各币种余额/本月结汇 指标卡 + 趋势图 + 待办队列 |
| 订阅管理页增强 | F22 | 详情页（扣款历史/用量/事件日志）、搜索/筛选/暂停/取消 |
| 钱包管理页 | F23 | 按币种分组余额+流水，收入/支出/FX/结汇分类 |

### Phase 2 — 种子客户与 MVP 上线（Week 11-15）

> PRD 里程碑：种子客户 + MVP 上线。

| 工作项 | 详情 |
|--------|------|
| 沙箱环境 | 独立沙箱数据、sk_test_* 密钥、测试卡号支持 |
| 审计日志完善 | 所有资金操作 + 权限操作 + 配置操作，含操作人/角色/IP/设备指纹，7 年保留 |
| 可观测性 | Actuator 健康检查、Prometheus 指标、日志聚合 |
| 安全加固 | TLS 1.2+、CSP、rate limiting、PCI DSS 合规审计 |
| 种子客户接入 | 10-20 家种子客户，真实交易跑通闭环 |
| MVP 上线 | 正式开放注册 |

### Phase 3 — Beta 功能（Week 16-24）

> PRD 里程碑：Beta 开发。

| 工作项 | PRD 对应 | 验收标准 |
|--------|----------|----------|
| 用量计费 | F05 | POST /v1/usage + /v1/usage/batch，幂等键，周期末结算，异常检测 |
| 智能重试 | F06 | 基于 BIN + decline_code + 时间窗口模型，+5pp 以上 |
| 同币种付款 | F12 | SEPA/ACH/Wire 清算，制裁筛查，审批流程 |
| 主动换汇 | F13 | 点差 ≤0.5%，汇率异常保护，换汇记录 |
| 客户自助门户 | F27 | 魔法链接登录，查看订阅/发票，更新支付，取消/恢复 |
| 对账报表 | F24 | 月度对账报告，CSV/Excel 导出，对账 API |
| 开发者中心 | F25 | API Key 管理，Webhook 配置 UI，调用日志，交互式文档 |
| 扩展币种 | F15 部分 | 扩展至 10+ 币种 |

### Phase 4 — GA（Week 25-36）

> PRD 里程碑：GA 上线。

| 工作项 | PRD 对应 | 验收标准 |
|--------|----------|----------|
| 智能路由 | F08 | 多收单机构接入，BIN/地区/历史成功率路由策略 |
| 分层定价 | F09 | tiered 定价、阶梯价、组合定价 |
| 企业虚拟卡 | F14 | Visa 虚拟卡，消费限额，品类限制，实时通知 |
| 扩展至 20+ 币种 | F15 | CAD/AUD/JPY/SGD/HKD 等 |
| 批量结汇 | F19 | 批量发起，自动拆分合规申报 |
| 结汇额度管理 | F20 | 对接 SAFE 额度系统，实时可用额度 |
| 多主体管理 | F26 | 多公司主体，按主体数据隔离，主体间调拨 |
| License Key 管理 | F28 | 密钥全生命周期，设备绑定，在线/离线校验 |
| 合规申报数据导出 | F18 | 月/季外汇申报数据导出 |

---

## 优先级总览

| 优先级 | 功能数 | 已完成 | 部分完成 | 未开始 |
|--------|--------|--------|----------|--------|
| P0 | 11 | 0 | 3（F01, F07, F22） | 8 |
| P1 | 9 | 0 | 0 | 9 |
| P2 | 8 | 0 | 0 | 8 |
| **合计** | **28** | **0** | **3** | **25** |

P0 功能是 MVP 的硬性门槛，必须在 Phase 1 全部完成。

---

## 关键依赖

| 类型 | 依赖项 | 影响范围 |
|------|--------|----------|
| 牌照 | 持牌收单机构（Visa/MC） | F02, F03 所有信用卡收单 |
| 牌照 | 持牌 EMI/支付机构（钱包合作方） | F10-F15 多币种钱包 |
| 牌照 | 持牌结汇机构（跨境支付牌照） | F16-F20 结汇回国 |
| 技术 | 卡网络 Token 化服务（Visa Token Service） | F02 卡绑定 + F03 MIT |
| 技术 | 制裁名单数据库（OFAC/EU/UN 实时更新） | F12, F16 AML 合规 |
| 税务 | 第三方税率 API（TaxJar/Avalara） | 税务计算 |
| 团队 | 合规/法务、风控工程师、支付系统后端 | 全部模块 |

## 风险关注

| 风险 | 应对 |
|------|------|
| 持牌通道签约延迟 | 同时谈判 2-3 家持牌机构，MVP 先用 1 家 |
| 收单风控误杀率高 | GA 引入智能路由，Beta 收集失败原因数据 |
| 结汇通道被监管暂停 | 多结汇机构备份，资金隔离托管 |
| 客户资金安全 | 100% 隔离托管，定期审计 |
| 客户门户越权访问 | 魔法链接 5 分钟 + 单次使用，1 小时会话，服务端身份验证 |
