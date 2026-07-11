# ============================================
# Dockerfile - 淘籍籍 Spring Boot 后端
# 多阶段构建: 先用 Maven 编译, 再用 JRE 运行
# ============================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建上传目录
RUN mkdir -p /var/lib/uploads

# 从 builder 阶段复制构建产物
COPY --from=builder /build/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动
ENTRYPOINT ["java", "-jar", "app.jar"]
