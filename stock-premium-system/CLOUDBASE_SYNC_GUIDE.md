# CloudBase MySQL 数据同步指南

## 📋 概述

本指南将帮助你将股票溢价率监控系统的数据模型推送到腾讯云开发(CloudBase)的 MySQL 数据库中。

## 🔧 配置步骤

### 1. 获取 CloudBase MySQL 连接信息

登录腾讯云开发控制台，获取你的 MySQL 数据库连接信息：
- 数据库地址 (Host)
- 端口号 (Port)
- 数据库名称 (Database Name)
- 用户名 (Username)
- 密码 (Password)

### 2. 配置连接信息

编辑 `src/main/resources/application-cloudbase.yml` 文件，替换为你的实际连接信息：

```yaml
cloudbase:
  mysql:
    url: jdbc:mysql://your-cloudbase-mysql-host:3306/your-database-name?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your-username
    password: your-password
```

### 3. 启动应用

使用 CloudBase 配置启动应用：

```bash
# 使用CloudBase配置启动
mvn spring-boot:run -Dspring-boot.run.profiles=cloudbase

# 或者同时使用dev和cloudbase配置
mvn spring-boot:run -Dspring-boot.run.profiles=dev,cloudbase
```

## 🚀 数据同步操作

### 方式一：通过 API 接口同步

启动应用后，访问 Swagger 文档：`http://localhost:8080/swagger-ui.html`

找到 "CloudBase数据同步" 分组，使用以下接口：

#### 1. 初始化表结构
```http
POST /api/cloudbase/init-tables
```
- 功能：在 CloudBase MySQL 中创建所有数据表
- 包含表：stock_info, stock_price_record, exchange_rate_record, premium_rate_record, daily_premium_stats, system_config

#### 2. 插入初始数据
```http
POST /api/cloudbase/init-data
```
- 功能：插入系统配置和示例股票数据
- 包含：系统配置参数、招商银行/中国平安/中国石油等示例数据

#### 3. 完整初始化
```http
POST /api/cloudbase/full-init
```
- 功能：一键完成表结构创建和初始数据插入
- 推荐使用此接口进行首次初始化

### 方式二：通过云函数同步

如果你使用微信小程序，可以部署云函数来执行数据库初始化：

1. 将 `cloudfunctions/initDatabase/` 目录上传到你的云开发环境
2. 在小程序中调用云函数：

```javascript
wx.cloud.callFunction({
  name: 'initDatabase',
  success: res => {
    console.log('数据库初始化成功', res);
  },
  fail: err => {
    console.error('数据库初始化失败', err);
  }
});
```

## 📊 数据表结构

### 1. stock_info - 股票基础信息表
- 存储 A+H 股票的基本信息
- 包含股票代码、名称、交易所、行业等信息

### 2. stock_price_record - 股票价格记录表
- 存储实时股票价格数据
- 包含开盘价、收盘价、最高价、最低价、成交量等

### 3. exchange_rate_record - 汇率记录表
- 存储港币对人民币汇率数据
- 支持手动设置汇率信息

### 4. premium_rate_record - 溢价率记录表
- 存储计算出的 A+H 股溢价率数据
- 包含 A 股价格、H 股价格、汇率、溢价率等

### 5. daily_premium_stats - 日统计数据表
- 存储每日溢价率统计数据
- 包含开盘/收盘/最高/最低/平均溢价率等统计指标

### 6. system_config - 系统配置表
- 存储系统运行参数
- 包含数据采集间隔、交易时间、预警阈值等配置

## 🔍 验证同步结果

### 1. 检查表结构
登录 CloudBase 控制台，查看数据库中是否成功创建了 6 张表。

### 2. 检查初始数据
查询以下表是否包含初始数据：
- `system_config`: 应包含 6 条系统配置记录
- `stock_info`: 应包含 3 条示例股票记录

### 3. 测试 API 响应
调用同步接口，检查返回结果：
```json
{
  "success": true,
  "message": "CloudBase数据库完整初始化成功"
}
```

## ⚠️ 注意事项

### 1. 网络连接
- 确保应用服务器能够访问 CloudBase MySQL
- 检查防火墙和安全组设置

### 2. 权限配置
- 确保 MySQL 用户具有建表和插入数据的权限
- 建议使用专门的数据库用户，避免使用 root 用户

### 3. 数据安全
- 不要在代码中硬编码数据库密码
- 使用环境变量或配置文件管理敏感信息
- 定期更换数据库密码

### 4. 性能优化
- 根据实际需求调整连接池参数
- 为高频查询字段添加索引
- 定期清理历史数据

## 🔧 故障排查

### 1. 连接失败
- 检查数据库地址、端口、用户名、密码是否正确
- 确认数据库服务是否正常运行
- 检查网络连通性

### 2. 权限错误
- 确认数据库用户是否有建表权限
- 检查数据库用户是否有插入数据权限

### 3. 字符编码问题
- 确保数据库和连接都使用 UTF-8 编码
- 检查 URL 中的字符编码参数

## 📈 后续扩展

### 1. 数据同步
- 实现本地数据库到 CloudBase 的定时同步
- 支持增量数据同步，避免重复数据

### 2. 双写机制
- 在数据写入时同时写入本地和 CloudBase 数据库
- 实现数据一致性保证

### 3. 小程序集成
- 基于 CloudBase 数据库开发微信小程序
- 实现实时数据展示和查询功能

---

**🎯 完成以上步骤后，你的股票溢价率监控系统数据模型就成功推送到 CloudBase MySQL 了！**