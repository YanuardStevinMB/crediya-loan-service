# 🐳 Guía de Dockerización - Sistema **Loan** Crediya

## 📋 Tabla de Contenidos

* [Introducción](#introducción)
* [Arquitectura Docker](#arquitectura-docker)
* [Pre-requisitos](#pre-requisitos)
* [Configuración Inicial](#configuración-inicial)
* [Construcción y Ejecución](#construcción-y-ejecución)
* [Verificación del Sistema](#verificación-del-sistema)
* [Gestión de la Base de Datos](#gestión-de-la-base-de-datos)
* [Comandos Útiles](#comandos-útiles)
* [Troubleshooting](#troubleshooting)
* [Configuración de Producción](#configuración-de-producción)

## 🌟 Introducción

Este proyecto ha sido dockerizado para facilitar el desarrollo, testing y despliegue del **Sistema Loan** de Crediya. La solución incluye:

* **Aplicación Spring Boot** (reactiva) para gestión de solicitudes
* **Base de datos MySQL 8.x** con inicialización automática
* **Adminer** para gestión visual de la base de datos
* **Red Docker externa** compartida con IAM (`crediya-network`) para comunicación entre servicios
* **Health checks** y **variables de entorno** para configuración flexible

> **Nota**: El servicio Loan consume IAM internamente vía DNS Docker `http://iam-service:8080` dentro de la red `crediya-network`.

## 🏗️ Arquitectura Docker

```
📦 Contenedores Docker
├── 🗄️ mysql-loan       # Base de datos MySQL 8.x
├── 🚀 loan-service     # Aplicación Spring Boot
└── 🌐 adminer          # Interfaz web para DB (opcional)

🌐 Red: crediya-network (bridge, externa)
💾 Volumen: mysql-loan-data (persistente)
```

## ✅ Pre-requisitos

Antes de comenzar, asegúrate de tener instalado:

* **Docker**: v20.10 o superior
* **Docker Compose**: v2.0 o superior
* **Git**: Para clonar el repositorio
* **4GB RAM libre**: Mínimo recomendado
* **Puertos disponibles**: 8083, 3308, 8082

### Verificar Instalación

```bash
# Verificar Docker
docker --version
docker compose --version

# Verificar que Docker está ejecutándose
docker info
```

## ⚙️ Configuración Inicial

### 1. Clonar y Navegar al Proyecto

```bash
git clone <repository-url>
cd loan
```

### 2. Crear la red externa compartida

```bash
docker network create crediya-network || true
```

### 3. Configurar Variables de Entorno

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar variables según tu entorno
notepad .env  # Windows
nano .env     # Linux/macOS
```

### 4. Variables de Entorno Principales

```env
# Base de datos
MYSQL_ROOT_PASSWORD=root_secure_password_2024
MYSQL_DATABASE=crediya_loan
MYSQL_USER=loan
MYSQL_PASSWORD=loan_secure_2024
MYSQL_LOAN_PORT=3308

# Aplicación
LOAN_SERVICE_PORT=8083
ADMINER_PORT=8082

# Seguridad (alineado con IAM)
JWT_SECRET=super_secret_256_bits_base64
JWT_EXPIRATION=3600
JWT_ISSUER=crediya

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost
```

> **Importante**: `JWT_SECRET` y `JWT_ISSUER` deben coincidir con los usados en **IAM**.

## 🚀 Construcción y Ejecución

### Opción 1: Inicialización Completa (Recomendado)

```bash
# Construir y ejecutar todos los servicios del Loan
docker compose -f compose.yml up --build

# Para ejecutar en segundo plano
docker compose -f docker-compose.yml up --build -d
```

### Opción 2: Construcción por Pasos

```bash
# 1) Construir solo las imágenes
docker compose -f docker-compose.yml build

# 2) Iniciar primero la base de datos
docker compose -f docker-compose.yml up mysql-loan -d

# 3) Cuando MySQL esté healthy, iniciar la app
docker compose -f docker-compose.yml up loan-service -d

# 4) Iniciar Adminer (opcional)
docker compose -f docker-compose.yml up adminer -d
```

### Opción 3: Solo la Aplicación (sin Adminer)

```bash
docker compose -f docker-compose.yml up mysql-loan loan-service --build -d
```

## ✅ Verificación del Sistema

### 1. Verificar Estado de los Contenedores

```bash
# Ver todos los contenedores
docker compose -f docker-compose.yml ps

# Ver logs en tiempo real
docker compose -f docker-compose.yml logs -f

# Ver logs de un servicio específico
docker compose -f docker-compose.yml logs -f loan-service
```

### 2. Health Checks

```bash
# Verificar salud de la base de datos
docker compose -f docker-compose.yml exec mysql-loan mysqladmin ping -p

# Verificar salud de la aplicación
curl http://localhost:8083/actuator/health

# (Opcional) Desde loan-service, verificar IAM en la red compartida
# docker compose -f compose.yml exec loan-service sh -lc "curl -fsS http://iam-service:8080/actuator/health"
```


### 2. pruebas de persistencia de datos

```bash
#Se detiene y elimina solo el contenedor (el volumen se conserva):
docker compose -f docker-compose.yml down

# Verificar salud de la aplicación
docker compose -f docker-compose.yml down
```




### 3. Endpoints de la Aplicación

| Servicio         | URL                                                                            | Descripción               |
| ---------------- |--------------------------------------------------------------------------------| ------------------------- |
| **API REST**     | [http://localhost:8082](http://localhost:8082)                                 | API principal del sistema |
| **Health Check** | [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health) | Estado de la aplicación   |
| **Swagger UI**   | [http://localhost:8082/swagger-ui](http://localhost:8082/swagger-ui)           | Documentación interactiva |
| **Adminer**      | [http://localhost:8083](http://localhost:8083)                                 | Gestión visual de BD      |

### 4. Probar la API

```bash
# Ejemplo ilustrativo: Crear una solicitud (ajusta el endpoint/DTO según tu API)
curl -X POST http://localhost:8083/api/v1/solicitudes \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2500000.00,
    "term": "2025-12-31",
    "email": "user@crediya.com",
    "identityDocument": "123456789",
    "loanTypeId": 1
  }'
```

## 🗄️ Gestión de la Base de Datos

### Conexión mediante Adminer

1. Abrir [http://localhost:8082](http://localhost:8082)
2. Usar las credenciales:

    * **Sistema**: MySQL
    * **Servidor**: mysql-loan
    * **Usuario**: loan
    * **Contraseña**: (definida en `.env`)
    * **Base de datos**: crediya\_loan

### Conexión Directa

```bash
# Conectarse desde la línea de comandos
docker compose -f compose.yml exec mysql-loan mysql -u loan -p crediya_loan
```

### Migraciones (Liquibase)

Coloca tu **changelog** en `src/main/resources/db/changelog/` y habilita Liquibase en `application*.yml`. Ejemplo de **schema** compartido:

```yaml
databaseChangeLog:
  - changeSet:
      id: create-table-tipo-prestamo
      author: manuel
      changes:
        - createTable:
            tableName: tipo_prestamo
            tableOptions: "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            columns:
              - column:
                  name: id_tipo_prestamo
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    primaryKeyName: pk_tipo_prestamo
                    nullable: false
              - column:
                  name: nombre
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: monto_minimo
                  type: DECIMAL(19,2)
                  constraints:
                    nullable: false
              - column:
                  name: monto_maximo
                  type: DECIMAL(19,2)
                  constraints:
                    nullable: false
              - column:
                  name: tasa_interes
                  type: DECIMAL(5,2)
                  constraints:
                    nullable: false
              - column:
                  name: validacion_automatica
                  type: BOOLEAN
                  constraints:
                    nullable: false

  - changeSet:
      id: create-table-estados
      author: manuel
      changes:
        - createTable:
            tableName: estados
            tableOptions: "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            columns:
              - column:
                  name: id_estado
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    primaryKeyName: pk_estados
                    nullable: false
              - column:
                  name: nombre
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: descripcion
                  type: VARCHAR(255)
              - column:
                  name: codigo
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
        - addUniqueConstraint:
            tableName: estados
            columnNames: codigo
            constraintName: uq_estados_codigo

  - changeSet:
      id: create-table-solicitud
      author: manuel
      changes:
        - createTable:
            tableName: solicitud
            tableOptions: "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            columns:
              - column:
                  name: id_solicitud
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    primaryKeyName: pk_solicitud
                    nullable: false
              - column:
                  name: monto
                  type: DECIMAL(19,2)
                  constraints:
                    nullable: false
              - column:
                  name: plazo
                  type: DATE
                  constraints:
                    nullable: false
              - column:
                  name: email
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: documento_identidad
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
              - column:
                  name: id_estado
                  type: BIGINT
                  constraints:
                    nullable: false
              - column:
                  name: id_tipo_prestamo
                  type: BIGINT
                  constraints:
                    nullable: false
        - createIndex:
            tableName: solicitud
            indexName: idx_solicitud_estado
            columns:
              - column:
                  name: id_estado
        - createIndex:
            tableName: solicitud
            indexName: idx_solicitud_tipo_prestamo
            columns:
              - column:
                  name: id_tipo_prestamo
        - addForeignKeyConstraint:
            baseTableName: solicitud
            baseColumnNames: id_estado
            referencedTableName: estados
            referencedColumnNames: id_estado
            constraintName: fk_solicitud_estado
            onDelete: RESTRICT
            onUpdate: CASCADE
        - addForeignKeyConstraint:
            baseTableName: solicitud
            baseColumnNames: id_tipo_prestamo
            referencedTableName: tipo_prestamo
            referencedColumnNames: id_tipo_prestamo
            constraintName: fk_solicitud_tipo_prestamo
            onDelete: RESTRICT
            onUpdate: CASCADE
```

> Si necesitas **datos iniciales** (catálogos), añade un `changeSet` con `insert` para `estados` y `tipo_prestamo`.

### Backup y Restore

```bash
# Crear backup
docker compose -f compose.yml exec mysql-loan mysqldump -u loan -p crediya_loan > backup.sql

# Restaurar backup
docker compose -f compose.yml exec -T mysql-loan mysql -u loan -p crediya_loan < backup.sql
```

## 🛠️ Comandos Útiles

### Gestión de Contenedores

```bash
# Ver estado de todos los servicios
docker compose -f compose.yml ps

# Detener todos los servicios
docker compose -f compose-.yml down

# Detener y eliminar volúmenes (¡CUIDADO! Elimina datos)
docker compose -f compose.yml down -v

# Reiniciar un servicio específico
docker compose -f compose.yml restart loan-service

# Ver uso de recursos
docker stats
```

### Logs y Debugging

```bash
# Logs de todos los servicios
docker compose -f compose.yml logs -f

# Logs de un servicio específico
docker compose -f compose.yml logs -f mysql-loan

# Logs de las últimas 100 líneas
docker compose -f compose.yml logs --tail=100 loan-service

# Conectarse a un contenedor
docker compose -f compose.yml exec loan-service sh
```

### Limpieza del Sistema

```bash
# Eliminar contenedores, redes y volúmenes del proyecto
docker compose -f compose.yml down -v

# Limpiar imágenes no utilizadas
docker image prune -f

# Limpiar todo el sistema Docker (¡CUIDADO!)
docker system prune -a
```

## 🚨 Troubleshooting

### Problemas Comunes

#### 1) Puerto ya en uso

```bash
Error: bind: address already in use
```

**Solución:**

```bash
# Verificar qué procesos usan los puertos
netstat -tulpn | grep :8083
netstat -tulpn | grep :3308

# Cambiar puertos en .env o detener procesos conflictivos
```

#### 2) La aplicación no puede conectar a la BD

**Verificaciones:**

```bash
# 1) Verificar que MySQL esté healthy
docker compose -f compose.yml ps

# 2) Ver logs de la base de datos
docker compose -f compose.yml logs mysql-loan

# 3) Verificar conectividad de red
docker compose -f compose.yml exec loan-service ping -c 2 mysql-loan || ping -n 2 mysql-loan
```

#### 3) `loan-service` no alcanza `iam-service`

* Confirma que **IAM** esté arriba y unido a `crediya-network`.
* Prueba desde el contenedor Loan: `curl http://iam-service:8080/actuator/health`.
* Revisa que el **nombre de contenedor** de IAM sea `iam-service`.

#### 4) Problemas de memoria

```bash
# Ver uso de memoria
docker stats

# Aumentar memoria disponible para Docker
# (Docker Desktop > Settings > Resources > Memory)
```

#### 5) Permisos en scripts

```bash
ls -la docker/init-scripts/
chmod +x docker/init-scripts/*.sql
```

### Logs de Depuración

```bash
# Habilitar logs detallados
export COMPOSE_LOG_LEVEL=DEBUG

# Levantar con más detalle
docker compose -f compose.yml up --build

# Ver logs específicos de build
docker compose -f compose.yml build --progress=plain
```

## 🔒 Configuración de Producción

### Variables de Entorno de Producción

```env
# .env.production
MYSQL_ROOT_PASSWORD=super_secure_root_password
MYSQL_PASSWORD=production_secure_password
JWT_SECRET=production_jwt_secret_256_bits
ENVIRONMENT=production
LOG_LEVEL=WARN
```

### Docker Compose Override

Crear `docker-compose.override.yml` (en la carpeta **loan**):

```yaml
version: "3.9"

services:
  loan-service:
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: production
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M

  mysql-loan:
    restart: always
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
```

### Comandos de Producción

```bash
# Ejecutar en modo producción
docker compose -f compose.yml -f docker-compose.override.yml up -d

# Monitoreo continuo
docker compose -f compose.yml logs -f --tail=50

# Backup automático
docker compose -f compose.yml exec mysql-loan mysqldump -u loan -p crediya_loan | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

## 📞 Soporte y Contacto

Para issues técnicos:

1. **Verificar logs**: `docker compose -f compose.yml logs -f`
2. **Revisar health checks**: `curl http://localhost:8083/actuator/health`
3. **Consultar documentación**: `http://localhost:8083/swagger-ui`

---
