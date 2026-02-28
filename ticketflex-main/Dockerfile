# Etapa 1: Construcción
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
# Entramos a la carpeta donde está el pom.xml antes de compilar
WORKDIR /ticketflex-main
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jdk-alpine
# Ajustamos la ruta para copiar el archivo generado desde la subcarpeta
COPY --from=build /ticketflex-main/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]