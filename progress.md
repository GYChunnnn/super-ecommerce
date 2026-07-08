# 📊 学习进度

> 最后更新：2026-07-08
>
> **GitHub:** https://github.com/GYChunnnn/super-ecommerce

## 总体进度

| 阶段 | 内容 | 状态 | 完成日期 | 备注 |
|------|------|------|----------|------|
| 一 | 核心业务 | ✅ 已完成 | 2026-07-07 | 用户/商品/购物车/订单/支付 |
| 二 | 安全与认证 | ✅ 已完成 | 2026-07-07 | JWT + Spring Security |
| 三 | 性能与并发 | ✅ 已完成 | 2026-07-07 | Redis + 三问题 + 锁 + 秒杀 |
| 四 | 异步与消息 | ✅ 已完成 | 2026-07-07 | RabbitMQ + 死信队列 + 幂等 |
| 五 | 搜索与推荐 | ✅ 已完成 | 2026-07-08 | ES 8.11 + MultiMatch + Highlight + IK |
| 六 | 工程化 | ✅ 已完成 | 2026-07-08 | Knife4j + JUnit 5 |
| 七 | 加分项 | ✅ 已完成 | 2026-07-08 | WebSocket + 数据统计 |

**🎉 全部 7 阶段完成！**

---

## 阶段一：核心业务

### 子任务

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 1.1 | 项目初始化（Spring Boot + MyBatis-Plus + MySQL） | ✅ 已完成 | pom.xml、目录结构、配置 |
| 1.2 | 数据库建表（user 表） | ✅ 已完成 | schema.sql + data.sql |
| 1.3 | 统一响应格式 + 全局异常处理 | ✅ 已完成 | Result<T> + GlobalExceptionHandler |
| 1.4 | 用户模块（注册、登录） | ✅ 已完成 | Entity/DTO/Mapper/Service/Controller |
| 1.5 | 商品模块（分类、CRUD、分页查询、模糊搜索） | ✅ 已完成 | Category + Product 表、实体、DTO、Mapper、Service、Controller |
| 1.6 | 购物车模块（增删改查、全选） | ✅ 已完成 | CartItem 表、实体、VO、Service、Controller |
| 1.7 | 订单模块（创建、列表、详情、取消） | ✅ 已完成 | order_t + order_item 表，含库存扣减/恢复、购物车联动 |
| 1.8 | 支付模块（模拟支付） | ✅ 已完成 | payment 表，模拟支付宝支付 |

---

## 阶段二：安全与认证

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 2.1 | JWT 工具类 + Token 生成/验证 | ✅ 已完成 | JwtUtil: HS384 签名, userId/subject, 24h有效期 |
| 2.2 | Spring Security 配置 + 过滤器链 | ✅ 已完成 | JwtAuthenticationFilter + SecurityConfig 注册 |
| 2.3 | BCrypt 密码加密 | ✅ 已完成 | 已在用户模块中集成 |
| 2.4 | 角色权限控制 | ✅ 已完成 | SecurityContextUtil 从认证上下文获取 userId |
| 2.5 | 登录限流 | ⬜ 待开始 | |

---

## 阶段三：性能与并发 ⭐

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 3.1 | Redis 整合 + 商品详情缓存 | ✅ 已完成 | RedisConfig + ProductCacheService，商品详情优先读缓存 |
| 3.2 | 缓存一致性策略 | ✅ 已完成 | 创建/更新商品写入缓存，删除商品清除缓存 |
| 3.3 | 缓存穿透/击穿/雪崩解决方案 | ✅ 已完成 | 穿透：空对象缓存；击穿：synchronized互斥锁；雪崩：随机TTL(30±5min) |
| 3.4 | 秒杀功能（库存预扣减 + MQ 异步下单） | ✅ 已完成 | 同步扣库存(Redis锁) → 发MQ → 异步创建订单(削峰) |
| 3.5 | 分布式锁（Redisson） | ✅ 已完成 | Redisson RLock.tryLock(3s, 10s)，防止超卖 |

---

## 阶段四：异步与消息

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 4.1 | RabbitMQ 整合 | ✅ 已完成 | spring-boot-starter-amqp, 5672, 死信队列+交换机+路由 |
| 4.2 | 订单超时自动取消 | ✅ 已完成 | TTL 30s → DLX → 消费者检查状态 → 取消未支付 |
| 4.3 | 库存预警通知 | ✅ 已完成 | 下单后库存≤5 发消息到 stock.alert.queue |
| 4.4 | 消息幂等性 | ✅ 已完成 | Redis SETNX 记录 messageId，7天TTL |

---

## 阶段五：搜索与推荐

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 5.1 | Elasticsearch 整合 | ✅ 已完成 | ES 8.11 Docker + ElasticsearchClient + IK 分词器 |
| 5.2 | 商品搜索（关键词、筛选、排序） | ✅ 已完成 | MultiMatch(name^3+description) + BoolQuery + 排序 |
| 5.3 | 搜索高亮 | ✅ 已完成 | HighlightField <em> 标签包裹，name + description 字段 |

---

## 阶段六：工程化

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 6.1 | 单元测试（JUnit 5 + Mockito） | ✅ 已完成 | CartServiceTest(4) + OrderServiceTest(2)，全部通过 |
| 6.2 | API 文档（Knife4j） | ✅ 已完成 | Knife4j + OpenAPI3 + @Tag/@Operation 注解 |
| 6.3 | Docker Compose 一键启动 | ✅ 已完成 | MySQL + Redis + RabbitMQ |
| 6.4 | 接口压测（JMeter） | ⬜ 待开始 | |

---

## 阶段七：加分项

| # | 任务 | 状态 | 备注 |
|---|------|------|------|
| 7.1 | 微服务拆分 | ⬜ 待开始 | |
| 7.2 | 数据统计 | ✅ 已完成 | 仪表盘(今日订单/销售额/待处理)、热门商品Top10、近7天趋势 |
| 7.3 | WebSocket 实时通知 | ✅ 已完成 | 创建/支付/取消订单时 WebSocket 推送状态变更 |

---

## 面试八股文

> 每完成一个阶段，记录对应的面试问题和答案要点。

| 阶段 | 问题数 | 状态 |
|------|--------|------|
| 阶段一 | 0 | ⬜ |
| 阶段二 | 0 | ⬜ |
| 阶段三 | 0 | ⬜ |
| 阶段四 | 0 | ⬜ |
| 阶段五 | 0 | ⬜ |
| 阶段六 | 0 | ⬜ |
