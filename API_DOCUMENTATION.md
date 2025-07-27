# 股票信息API接口文档

## 新增接口：通过腾讯财经API查询股票信息并持久化

### 接口描述
该接口通过股票名称调用腾讯财经API查询股票基本信息，并将数据持久化到股票基本信息表中。

### 接口信息
- **URL**: `/stock/fetch-and-save`
- **方法**: `POST`
- **Content-Type**: `application/x-www-form-urlencoded` 或 `application/json`

### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 示例 |
|--------|------|------|------|------|
| stockName | String | 是 | 股票名称 | 中国平安 |

### 请求示例

#### 使用curl
```bash
curl -X POST "http://localhost:8080/stock/fetch-and-save" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "stockName=中国平安"
```

#### 使用JavaScript (fetch)
```javascript
fetch('/stock/fetch-and-save', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'stockName=中国平安'
})
.then(response => response.json())
.then(data => console.log(data));
```

### 响应格式

#### 成功响应
```json
{
    "success": true,
    "code": 200,
    "message": "股票信息查询并保存成功",
    "data": {
        "id": 1,
        "stockName": "中国平安",
        "marketType": "A+H",
        "aStockCode": "601318",
        "hStockCode": "02318",
        "exchange": "SH/HK",
        "industry": "保险",
        "status": 1,
        "createdTime": "2024-01-01T10:00:00",
        "updatedTime": "2024-01-01T10:00:00",
        "deleted": 0
    }
}
```

#### 股票已存在响应
```json
{
    "success": true,
    "code": 200,
    "message": "股票信息查询并保存成功",
    "data": {
        "id": 1,
        "stockName": "中国平安",
        "marketType": "A+H",
        "aStockCode": "601318",
        "hStockCode": "02318",
        "exchange": "SH/HK",
        "industry": "保险",
        "status": 1,
        "createdTime": "2024-01-01T09:00:00",
        "updatedTime": "2024-01-01T09:00:00",
        "deleted": 0
    }
}
```

#### 错误响应
```json
{
    "success": false,
    "code": 500,
    "message": "未能获取或保存股票信息",
    "data": null
}
```

### 功能特性

1. **智能去重**: 接口会先检查数据库中是否已存在相同名称的股票，如果存在则直接返回现有记录
2. **腾讯财经API集成**: 调用腾讯财经API获取实时股票信息
3. **A+H股支持**: 自动识别并匹配A股和H股代码
4. **行业分类**: 根据股票名称自动判断所属行业
5. **数据持久化**: 将获取的股票信息保存到数据库中

### 支持的股票类型

- **A股**: 上海证券交易所、深圳证券交易所上市股票
- **H股**: 香港交易所上市的中国内地公司股票
- **A+H股**: 同时在内地和香港上市的股票

### 常见A+H股示例

| 股票名称 | A股代码 | H股代码 |
|----------|---------|---------|
| 中国平安 | 601318 | 02318 |
| 中国建设银行 | 601939 | 00939 |
| 招商银行 | 600036 | 03968 |
| 中国农业银行 | 601288 | 01288 |
| 中国神华 | 601088 | 01088 |
| 中国石化 | 600028 | 00728 |
| 中国石油 | 601857 | 06837 |
| 工商银行 | 601398 | 03988 |

### 错误处理

接口包含完善的错误处理机制：

1. **参数验证**: 检查股票名称是否为空
2. **API调用失败**: 处理腾讯财经API调用异常
3. **数据解析错误**: 处理API响应数据解析异常
4. **数据库操作异常**: 处理数据保存失败的情况

### 日志记录

接口会记录详细的操作日志：
- 请求开始和结束
- API调用结果
- 数据保存结果
- 错误信息和堆栈跟踪

### 性能考虑

- 接口会先检查本地数据库，避免重复调用外部API
- 使用异步处理提高响应速度
- 包含适当的超时设置防止长时间等待

## 其他相关接口

### 1. 搜索股票信息
- **URL**: `/stock/search`
- **方法**: `GET`
- **参数**: `stockName` (股票名称)

### 2. 获取股票列表
- **URL**: `/stock/list`
- **方法**: `GET`

### 3. 根据股票代码查询
- **URL**: `/stock/{stockCode}`
- **方法**: `GET`
- **参数**: `stockCode` (股票代码)