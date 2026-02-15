FROM maven:3.9.6-eclipse-temurin-21 AS dependencies
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY backend/pom.xml /workspace/backend/pom.xml
# Download dependencies (cached layer)
RUN mvn -q dependency:resolve dependency:resolve-plugins -pl backend -am

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY backend/pom.xml /workspace/backend/pom.xml
COPY backend/src /workspace/backend/src
# Copy cached Maven repository from dependencies stage
COPY --from=dependencies /root/.m2 /root/.m2
RUN mvn -q -pl backend -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/quarkus-app/ /app/
EXPOSE 8080
CMD ["java", "-jar", "/app/quarkus-run.jar"]
