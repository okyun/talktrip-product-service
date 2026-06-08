FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
