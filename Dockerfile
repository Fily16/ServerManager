FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ServerManager-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=10000
EXPOSE ${PORT}
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx350m", "-Xms200m", "-jar", "app.jar", "--spring.profiles.active=prod"]
