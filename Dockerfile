# =============================================================================
# Dockerfile multi-stage para app Spring Boot (JDK 21) con Gradle
# FASE 1: build  -> compila y empaqueta el JAR
# FASE 2: runtime -> ejecuta el JAR en una imagen mínima (Alpine JRE 21)
# =============================================================================

# ========================= FASE 1: BUILD =====================================
# Imagen con Gradle 8.10.1 + JDK 21 para compilar el proyecto
FROM gradle:8.10.1-jdk21 AS build

# Carpeta de trabajo dentro de la imagen de build
WORKDIR /home/gradle/project

# --- Optimización de caché ---
# Copiamos primero SOLO archivos de configuración de Gradle para aprovechar
# la caché de dependencias en builds posteriores (si estos no cambian).
COPY gradle/ gradle/
COPY build.gradle main.gradle gradle.properties settings.gradle gradlew gradlew.bat ./
COPY lombok.config ./

# Resolución de dependencias (capa cacheable).
# TIP: puedes enfocar al módulo con ":app-service:dependencies"
RUN gradle --no-daemon dependencies

# Copia del resto del código fuente del repo
COPY . .

# Compila y genera el JAR ejecutable del módulo app-service
# TIP: si no quieres correr tests aquí, agrega:  -x test
RUN gradle --no-daemon :app-service:clean :app-service:bootJar --no-build-cache


# ======================== FASE 2: RUNTIME ====================================
# Imagen de ejecución liviana con JRE 21 (Alpine)
FROM eclipse-temurin:21-jre-alpine

# Carpeta donde vivirá el JAR y desde donde se ejecutará
WORKDIR /app

# Instala herramientas mínimas:
# - curl: para healthcheck
# - tzdata: para configurar zona horaria si lo necesitas
# Crea usuario/grupo 'spring' para ejecutar en modo no-root (mejor práctica)
RUN apk add --no-cache curl tzdata && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# Cambiamos a usuario no-root
USER spring:spring

# Copiamos el JAR generado en la fase de build.
# IMPORTANTE: verifica que la ruta apunte al artefacto correcto.
# Si tu módulo no está en "applications/app-service", ajusta esta ruta.
COPY --from=build --chown=spring:spring /home/gradle/project/applications/app-service/build/libs/*.jar /app/app.jar

# ====================== Configuración de entorno ==============================
# Zona horaria por defecto
ENV TZ=UTC

# Perfil activo de Spring (debe existir en tu application-docker.yml/properties)
ENV SPRING_PROFILES_ACTIVE=docker

# Opciones JVM comunes para contenedores:
# - Usa límites de memoria del contenedor
# - Ajusta MaxRAMPercentage según tus necesidades
# - Fuente de entropía rápida para arranque
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Puerto lógico de la app (documentativo). Expón el que use tu app.
EXPOSE 8080

# ============================ Healthcheck =====================================
# Verifica la salud llamando al endpoint Actuator. Asegúrate de tener
# "management.endpoints.web.exposure.include=health" y que /actuator/health esté habilitado.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# ============================ Entrypoint ======================================
# Ejecuta la app con las opciones JVM definidas arriba.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
