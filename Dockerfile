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
# DATABASE_URL (Neon/Render) → JDBC: ver api/docker-entrypoint.sh.
# tr quita los fin de línea de Windows por si el archivo llega con CRLF.
COPY api/docker-entrypoint.sh /app/arranque.tmp
RUN tr -d '\r' < /app/arranque.tmp > /app/arranque.sh && rm /app/arranque.tmp && chmod +x /app/arranque.sh
ENTRYPOINT ["/app/arranque.sh"]
