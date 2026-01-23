# 多階段構建的 Dockerfile
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY src/ src/

# 構建應用程式
RUN ./gradlew build -x test --no-daemon

# 運行階段
FROM openjdk:17-jdk-slim

WORKDIR /app

# 創建非 root 用戶來運行應用程式
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 複製構建產物
COPY --from=builder /app/build/libs/*.jar app.jar

# 設置檔案擁有者
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8080

# 啟動應用程式
CMD ["java", "-jar", "app.jar"]