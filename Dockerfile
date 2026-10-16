# gescazone-api (Spring Boot). Contexto de build: carpeta GESCAZONE/.
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY api/pom.xml .
RUN mvn -q dependency:go-offline
COPY api/src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*-exec.jar app.jar
# Instancias pequeñas (512 MB): la JVM se ajusta a la memoria del contenedor.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
# Render entrega la base como DATABASE_URL=postgresql://usuario:clave@host:puerto/db;
# Java necesita jdbc:postgresql://host:puerto/db (usuario y clave van en DB_USER/DB_PASSWORD).
ENTRYPOINT ["sh", "-c", "if [ -n \"$DATABASE_URL\" ]; then export DB_URL=\"jdbc:postgresql://${DATABASE_URL#*@}\"; fi; exec java -jar app.jar --server.port=${PORT:-8080}"]
