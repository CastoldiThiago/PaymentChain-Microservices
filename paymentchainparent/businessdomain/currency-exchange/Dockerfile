# Usamos la imagen oficial de Java 21 (versión ligera Alpine)
FROM eclipse-temurin:21-jdk-alpine

# Directorio temporal para Tomcat (usado por Spring Boot)
VOLUME /tmp

# Argumento que recibe el JAR construido por Maven
ARG JAR_FILE=target/*.jar

# Copiamos el JAR dentro del contenedor con el nombre 'app.jar'
COPY ${JAR_FILE} app.jar

# Comando de ejecución
ENTRYPOINT ["java","-jar","/app.jar"]