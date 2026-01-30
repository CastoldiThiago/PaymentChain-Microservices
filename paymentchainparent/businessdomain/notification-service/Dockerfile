# 1. Usamos una imagen base ligera con Java 21 (Alpine Linux)
FROM eclipse-temurin:21-jdk-alpine

# 2. Definimos un usuario para no correr como root (Seguridad básica)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 3. Directorio temporal necesario para Tomcat/Spring Boot
VOLUME /tmp

# 4. Argumento que apunta al archivo .jar generado por Maven
ARG JAR_FILE=target/*.jar

# 5. Copiamos el JAR del host al contenedor con el nombre 'app.jar'
COPY ${JAR_FILE} app.jar

# 6. Comando para arrancar la aplicación
ENTRYPOINT ["java","-jar","/app.jar"]