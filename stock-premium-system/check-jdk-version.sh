#!/bin/bash

# JDK版本检查脚本
echo "=========================================="
echo "JDK版本统一性检查"
echo "=========================================="

# 检查系统Java版本
echo "1. 系统Java版本:"
java -version 2>&1 | head -3

# 检查Maven编译器版本
echo -e "\n2. Maven编译器配置:"
grep -A 2 -B 2 "java.version" pom.xml || echo "未找到java.version配置"

# 检查Dockerfile中的JDK版本
echo -e "\n3. Dockerfile JDK版本:"
grep "FROM openjdk" Dockerfile || echo "未找到JDK配置"

# 检查应用运行时版本
echo -e "\n4. 应用运行时版本检查:"
if [ -f "target/stock-premium-system-1.0.0.jar" ]; then
    echo "检查jar包中的Java版本..."
    unzip -p target/stock-premium-system-1.0.0.jar META-INF/MANIFEST.MF | grep -i "build-jdk" || echo "未找到构建JDK信息"
else
    echo "jar包不存在，请先编译项目"
fi

# 检查Docker容器中的Java版本
echo -e "\n5. Docker容器Java版本:"
if docker ps | grep -q "stock-premium-app"; then
    docker exec stock-premium-app java -version 2>&1 | head -1
else
    echo "应用容器未运行"
fi

echo -e "\n=========================================="
echo "检查完成！"
echo "建议: 确保所有环境都使用JDK 17"
echo "=========================================="