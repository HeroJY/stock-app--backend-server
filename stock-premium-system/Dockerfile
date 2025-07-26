# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs

# 复制jar包到容器
COPY target/stock-premium-system-1.0.0.jar app.jar

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 暴露端口
EXPOSE 8080

# 设置JVM参数 (针对JDK 17优化)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=prod -jar app.jar"]