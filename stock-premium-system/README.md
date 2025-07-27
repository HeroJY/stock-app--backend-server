# 股票溢价率监控系统

## 📊 项目简介

股票溢价率监控系统是一个专为A+H股票溢价率监控而设计的后端服务系统。该系统通过实时获取A股和H股价格数据，计算溢价率，为投资者提供准确的市场分析数据。

## ✨ 主要功能

### 🔄 数据采集
- **实时价格获取**: 通过腾讯财经API获取A股和H股实时价格
- **汇率管理**: 手动设置和管理港币对人民币汇率（已移除自动获取功能）
- **定时任务**: 支持自定义间隔的数据采集任务

### 📈 溢价率计算
- **实时计算**: 基于最新价格和汇率计算A+H股溢价率
- **历史数据**: 保存历史溢价率数据，支持趋势分析
- **统计分析**: 提供日统计、周统计、月统计等多维度数据

### 🎯 监控预警
- **阈值监控**: 支持设置溢价率阈值预警
- **数据异常检测**: 自动检测数据异常并记录
- **市场状态监控**: 实时监控A股和港股市场开市状态

### 📱 API接口
- **RESTful API**: 提供完整的REST API接口
- **Swagger文档**: 自动生成API文档
- **数据导出**: 支持多种格式的数据导出

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 2.7.14
- **数据库**: MySQL 8.0 / H2 (开发测试)
- **ORM**: MyBatis Plus 3.5.3
- **缓存**: Redis
- **定时任务**: Spring Scheduler + Quartz
- **API文档**: Swagger 2.9.2
- **工具库**: Hutool 5.8.20

### 数据库设计
- **stock_info**: 股票基础信息表
- **stock_price_record**: 实时价格记录表
- **exchange_rate_record**: 汇率记录表
- **premium_rate_record**: 溢价率记录表
- **daily_premium_stats**: 日统计数据表
- **system_config**: 系统配置表

## 🚀 快速开始

### ⚠️ 重要提示
- **汇率管理**: 系统已移除自动汇率获取功能，需要手动设置汇率数据
- **数据完整性**: 已修复创建时间戳问题，所有新记录都会正确设置时间戳
- **API访问**: Swagger文档地址为 `http://localhost:8080/swagger-ui.html`

### 环境要求
- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选)

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd stock-premium-system
```

2. **数据库配置**
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE stock_premium DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本
mysql -u root -p stock_premium < src/main/resources/sql/init.sql
```

3. **配置文件**
```yaml
# src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/stock_premium
    username: root
    password: your_password
```

4. **启动应用**
```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境启动
mvn clean package
java -jar target/stock-premium-system-1.0.0.jar --spring.profiles.active=prod
```

5. **访问应用**
- **应用地址**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/test/health

## 📋 API接口

### 健康检查
```http
GET /test/health
```

### 股票数据
```http
# 获取股票列表
GET /api/stocks

# 获取股票价格
GET /api/stocks/{stockCode}/price

# 获取溢价率
GET /api/premium-rate/{stockCode}
```

### 数据采集
```http
# 手动触发数据采集
POST /api/data-collection/collect

# 获取采集状态
GET /api/data-collection/status
```

### 统计数据
```http
# 获取日统计
GET /api/stats/daily?stockCode={code}&date={date}

# 获取历史统计
GET /api/stats/history?stockCode={code}&startDate={start}&endDate={end}
```

## 🔧 配置说明

### 应用配置
```yaml
# 定时任务配置
schedule:
  stock-data-collect:
    enabled: true
    cron: "0 */5 * * * ?"  # 每5分钟执行
  daily-stats:
    enabled: true
    cron: "0 0 18 * * ?"   # 每天18点执行

# 腾讯财经API配置
tencent:
  finance:
    api:
      base-url: https://qt.gtimg.cn/q=
      timeout: 5000
```

### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/stock_premium
    username: root
    password: password
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
```

## 🐳 Docker部署

### 构建镜像
```bash
docker build -t stock-premium-system:1.0.0 .
```

### 使用Docker Compose
```bash
docker-compose up -d
```

### 环境变量
```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=stock_premium
DB_USERNAME=root
DB_PASSWORD=password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
```

## 📊 监控指标

### 系统指标
- **应用状态**: 健康检查接口
- **数据库连接**: 连接池状态监控
- **API响应时间**: 接口性能监控

### 业务指标
- **数据采集成功率**: 数据获取成功率统计
- **溢价率计算准确性**: 计算结果验证
- **定时任务执行状态**: 任务执行情况监控

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn integration-test
```

### API测试
```bash
# 健康检查
curl -X GET "http://localhost:8080/test/health"

# 数据库连接测试
curl -X GET "http://localhost:8080/test/database"

# 腾讯财经API测试
curl -X GET "http://localhost:8080/test/tencent-api/sh601088"
```

## 📝 开发指南

### 代码结构
```
src/main/java/com/stock/premium/
├── controller/          # 控制器层
├── service/            # 服务层
├── mapper/             # 数据访问层
├── entity/             # 实体类
├── config/             # 配置类
├── scheduled/          # 定时任务
└── utils/              # 工具类
```

### 开发规范
- **代码风格**: 遵循阿里巴巴Java开发规范
- **注释规范**: 使用JavaDoc注释
- **日志规范**: 使用SLF4J + Logback
- **异常处理**: 统一异常处理机制

### 新增功能
1. 在对应的service包中添加业务逻辑
2. 在controller包中添加API接口
3. 在mapper包中添加数据访问方法
4. 更新API文档和测试用例

## 🔍 故障排查

### 常见问题

**1. 应用启动失败**
- 检查JDK版本是否为17+
- 检查数据库连接配置
- 查看启动日志中的错误信息

**2. 数据采集失败**
- 检查网络连接
- 验证腾讯财经API是否可访问
- 检查定时任务配置

**3. 数据库连接异常**
- 验证数据库服务是否启动
- 检查连接参数配置
- 查看数据库日志

### 日志查看
```bash
# 查看应用日志
tail -f logs/stock-premium.log

# 查看错误日志
tail -f logs/error.log
```

## 📈 性能优化

### 数据库优化
- 合理设置连接池参数
- 添加必要的数据库索引
- 定期清理历史数据

### 缓存优化
- 使用Redis缓存热点数据
- 设置合理的缓存过期时间
- 实现缓存预热机制

### API优化
- 实现接口限流
- 添加响应缓存
- 优化查询性能

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🔄 更新日志

### v1.0.1 (2024-07-27)
- 🐛 **修复MyBatis-Plus自动填充问题**: 修复股票价格记录和汇率记录创建时间戳为null的问题
  - 为 `MybatisPlusConfig` 添加 `@Configuration` 注解
  - 修复分页插件配置，添加 `@Bean` 注解
  - 确保所有数据记录都正确设置创建时间戳
- 🔧 **优化汇率管理**: 完全移除腾讯财经汇率API，改为纯手动汇率管理
  - 删除 `refreshRate` 和 `batchImport` 方法
  - 汇率数据只能通过手动设置管理
  - 优化汇率查询接口，无数据时给出明确提示
- 📝 **接口优化**: 溢价率历史接口股票代码设为必填项，提升数据查询准确性
- 🎯 **数据结构优化**: 创建 `ExchangeRateSimpleVO`，汇率接口只返回纯汇率数据
- ⚡ **性能提升**: 优化数据库查询和API响应速度
- 📚 **文档更新**: 完善API文档和系统配置说明

### v1.0.0 (2024-01-01)
- ✨ 初始版本发布
- 🔄 实现基础数据采集功能
- 📊 添加溢价率计算功能
- 📱 提供完整的REST API接口
- 🐳 支持Docker部署
- 📚 完善项目文档

---

**⭐ 如果这个项目对你有帮助，请给我们一个星标！**