# -----------------------------
# Etapa de construcción (Maven oficial + JDK 17)
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar pom (y .mvn si lo necesitas)
COPY pom.xml ./
COPY .mvn/ .mvn/  # opcional si usas wrapper para algunos plugins (no necesario para mvn del contenedor)

# Descargar dependencias para cache (acelera builds)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src/ src/

# Compilar y empaquetar sin tests
RUN mvn clean package -DskipTests -B

# -----------------------------
# Etapa de ejecución (runtime menor)
# -----------------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar el JAR generado desde la etapa de build
COPY --from=builder /app/target/ticketflex-0.0.1-SNAPSHOT.jar app.jar

# (Opcional) copiar script wait-for-db si lo usas
COPY wait-for-db.sh /usr/local/bin/wait-for-db.sh
RUN chmod +x /usr/local/bin/wait-for-db.sh || true

# Exponer puerto de la aplicación
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
