FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon -x test

FROM amazoncorretto:21-alpine-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]