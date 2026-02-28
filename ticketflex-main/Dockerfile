# -----------------------------
# Etapa de construcción (Maven oficial + JDK 17)
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml ./
COPY .mvn/ .mvn/ 

# Descargar dependencias para cache
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

# 1. INSTALAR NETCAT en la etapa final para que wait-for-db.sh funcione
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Copiar el JAR generado desde la etapa de build
COPY --from=builder /app/target/ticketflex-0.0.1-SNAPSHOT.jar app.jar

# 2. Copiar el script de espera (asumiendo que lo creaste en la raíz)
COPY wait-for-db.sh /usr/local/bin/wait-for-db.sh
RUN chmod +x /usr/local/bin/wait-for-db.sh

# Exponer puerto de la aplicación
EXPOSE 8080

# 3. Comando de arranque: Ejecuta el script de espera, y LUEGO inicia la app
# La estructura correcta es [script, argumento1, argumento2, ...]
ENTRYPOINT ["/usr/local/bin/wait-for-db.sh", "java", "-jar", "app.jar"]