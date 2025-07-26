#!/bin/bash

# 微信小程序股票溢价率监控系统部署脚本 (JDK 17版本)
# 作者: system
# 版本: 1.0.0

echo "=========================================="
echo "股票溢价率监控系统部署脚本 (JDK 17)"
echo "=========================================="

# 检查Java版本
echo "检查Java版本..."
java -version 2>&1 | head -1
if ! java -version 2>&1 | grep -q "17"; then
    echo "警告: 当前Java版本不是17，建议使用JDK 17"
fi

# 设置环境变量
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}
export PATH=$JAVA_HOME/bin:$PATH

echo "当前Java版本: $(java -version 2>&1 | head -1)"
echo "JAVA_HOME: $JAVA_HOME"

# 停止现有服务
echo "停止现有服务..."
docker-compose down

# 清理旧镜像
echo "清理旧镜像..."
docker rmi stock-premium-system:jdk17-1.0.0 2>/dev/null || true

# 编译项目
echo "编译项目..."
mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17

if [ $? -ne 0 ]; then
    echo "编译失败，请检查代码"
    exit 1
fi

# 检查jar包是否生成
if [ ! -f "target/stock-premium-system-1.0.0.jar" ]; then
    echo "jar包未找到，编译可能失败"
    exit 1
fi

echo "jar包大小: $(du -h target/stock-premium-system-1.0.0.jar | cut -f1)"

# 构建Docker镜像
echo "构建Docker镜像..."
docker build -t stock-premium-system:jdk17-1.0.0 .

if [ $? -ne 0 ]; then
    echo "Docker镜像构建失败"
    exit 1
fi

# 启动服务
echo "启动服务..."
docker-compose up -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 检查服务状态
echo "检查服务状态..."
docker-compose ps

# 检查应用健康状态
echo "检查应用健康状态..."
for i in {1..10}; do
    if curl -f http://localhost:8080/api/health >/dev/null 2>&1; then
        echo "✅ 应用启动成功！"
        break
    else
        echo "等待应用启动... ($i/10)"
        sleep 10
    fi
done

# 显示访问地址
echo "=========================================="
echo "部署完成！"
echo "应用地址: http://localhost:8080/api"
echo "API文档: http://localhost:8080/swagger-ui.html"
echo "Nginx代理: http://localhost"
echo "=========================================="

# 显示日志
echo "最近的应用日志:"
docker-compose logs --tail=20 app