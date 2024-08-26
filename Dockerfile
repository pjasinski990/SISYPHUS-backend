FROM eclipse-temurin:21-jdk as build
WORKDIR /workspace/app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradlew ./
COPY gradle gradle
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jre

COPY --from=build /workspace/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
