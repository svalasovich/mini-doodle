FROM bellsoft/liberica-openjdk-alpine:25.0.4-9 AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM bellsoft/liberica-runtime-container:jre-25.0.4_9-slim-musl
WORKDIR /app

RUN addgroup -S appuser && adduser -S -G appuser -h /app appuser
COPY --from=build /workspace/build/libs/*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]