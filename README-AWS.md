# LOAN-SERVICE - Configuración AWS

Este documento describe la configuración del LOAN-SERVICE para despliegue en AWS ECS con Aurora MySQL y comunicación con IAM-SERVICE.

## 🐳 Configuración Docker

### Variables de Entorno (AWS ECS)

Las siguientes variables se configuran automáticamente por Terraform:

```bash
# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080

# Database Configuration (Aurora MySQL)
DB_HOST={aurora-cluster-endpoint}
DB_PORT=3306
DB_NAME=loan_db
ADAPTERS_R2DBC_HOST={aurora-cluster-endpoint}
ADAPTERS_R2DBC_PORT=3306
ADAPTERS_R2DBC_DATABASE=loan_db

# Credenciales vía AWS Secrets Manager
DB_USERNAME={from-secrets-manager}
DB_PASSWORD={from-secrets-manager}
JWT_SECRET={from-secrets-manager}

# RestConsumer Configuration (Comunicación con IAM-SERVICE)
ADAPTER_RESTCONSUMER_URL=http://{nlb-dns}:80

# Swagger UI Configuration
SPRINGDOC_API_DOCS_PATH=/api/v1/api-docs
SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html
SPRINGDOC_SWAGGER_UI_ENABLED=true
SPRINGDOC_API_DOCS_ENABLED=true

# CORS Configuration
CORS_ALLOWED_ORIGINS=*

# Health Check Configuration
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info
MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true
```

## 🔗 Comunicación con IAM-SERVICE

El LOAN-SERVICE se comunica con el IAM-SERVICE a través de:

### En AWS (Producción)
- **URL**: `http://{nlb-internal-dns}:80` (configurado automáticamente)
- **Protocolo**: HTTP interno via NLB
- **Autenticación**: JWT tokens

### En Local (Desarrollo)
- **URL**: `http://iam-service:8080` (via docker network)
- **Red**: `crediya-network`

## 🚀 Construcción y Despliegue

### 1. Construir imagen local
```bash
docker build -t crediya-loan-service .
```

### 2. Probar localmente
```bash
# Asegúrate de que el IAM-SERVICE esté ejecutándose
# Crear red compartida si no existe
docker network create crediya-network || true

# Ejecutar con docker-compose
docker-compose up
```

### 3. Desplegar a AWS ECR
```bash
# Obtener URL del repositorio
ECR_URI=$(aws ecr describe-repositories --repository-names crediya-loan-prod --query 'repositories[0].repositoryUri' --output text)

# Construir para AWS
docker build -t $ECR_URI:latest .

# Login a ECR
aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin $ECR_URI

# Subir imagen
docker push $ECR_URI:latest
```

## 🌐 Endpoints

Una vez desplegado en AWS, el servicio estará disponible en:

### Via API Gateway (Producción)
- Base URL: `https://{api-gateway-id}.execute-api.us-east-2.amazonaws.com/loan`
- Health Check: `/loan/actuator/health`
- Swagger UI: `/loan/swagger-ui.html`
- API Docs: `/loan/api/v1/api-docs`

### Via ALB (Testing)
- Base URL: `http://{alb-dns}/loan`
- Health Check: `/loan/actuator/health`
- Swagger UI: `/loan/swagger-ui.html`

## 🔍 Monitoreo

### Ver logs en CloudWatch
```bash
aws logs tail /ecs/crediya-prod-loan --follow
```

### Health Check
```bash
curl https://{api-gateway-url}/loan/actuator/health
```

### Verificar comunicación con IAM
```bash
# Ver logs para verificar llamadas a IAM-SERVICE
aws logs filter-log-events --log-group-name /ecs/crediya-prod-loan --filter-pattern "RestConsumer"
```

### Ver métricas ECS
```bash
aws ecs describe-services --cluster crediya-prod --services crediya-prod-loan
```

## 🛠️ Configuración de Base de Datos

### Conexión a Aurora
- **Host**: Configurado automáticamente via variable `DB_HOST`
- **Puerto**: 3306
- **Base de datos**: `loan_db`
- **Credenciales**: Gestionadas por AWS Secrets Manager

### Obtener credenciales manualmente
```bash
aws secretsmanager get-secret-value --secret-id crediya-prod-loan/db
```

## 📋 Configuraciones Spring Boot Requeridas

Asegúrate de tener en tu `application-docker.yml`:

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  r2dbc:
    url: r2dbc:mysql://${ADAPTERS_R2DBC_HOST}:${ADAPTERS_R2DBC_PORT}/${ADAPTERS_R2DBC_DATABASE}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# RestConsumer Configuration para comunicación con IAM-SERVICE
adapter:
  restconsumer:
    url: ${ADAPTER_RESTCONSUMER_URL:http://localhost:8080}
    timeout: 30000
    retry:
      max-attempts: 3
      delay: 1000

springdoc:
  api-docs:
    path: ${SPRINGDOC_API_DOCS_PATH:/api/v1/api-docs}
    enabled: ${SPRINGDOC_API_DOCS_ENABLED:true}
  swagger-ui:
    path: ${SPRINGDOC_SWAGGER_UI_PATH:/swagger-ui.html}
    enabled: ${SPRINGDOC_SWAGGER_UI_ENABLED:true}

management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info}
  endpoint:
    health:
      probes:
        enabled: ${MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED:true}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:*}
```

## 🔄 Testing de Comunicación Entre Servicios

### 1. Verificar que ambos servicios están ejecutándose
```bash
aws ecs describe-services --cluster crediya-prod --services crediya-prod-iam crediya-prod-loan
```

### 2. Probar conectividad interna
```bash
# Ejecutar comando dentro del contenedor LOAN
aws ecs execute-command --cluster crediya-prod --task {loan-task-id} --container crediya-prod-loan --interactive --command "/bin/sh"

# Dentro del contenedor, probar conectividad
curl -f http://{nlb-dns}:80/actuator/health
```

### 3. Verificar logs de RestConsumer
```bash
aws logs filter-log-events --log-group-name /ecs/crediya-prod-loan --filter-pattern "RestConsumer"
```

## 🔧 Troubleshooting

### Problema: Servicio no arranca
1. Verificar logs: `aws logs tail /ecs/crediya-prod-loan --follow`
2. Verificar que Aurora está disponible
3. Verificar secrets en AWS Secrets Manager
4. Verificar conectividad con IAM-SERVICE

### Problema: No puede conectar a base de datos
1. Verificar security groups
2. Verificar que Aurora está en VPC correcta
3. Verificar credenciales en Secrets Manager

### Problema: No puede comunicarse con IAM-SERVICE
1. Verificar que IAM-SERVICE está ejecutándose
2. Verificar configuración de NLB
3. Verificar security groups (permitir tráfico interno)
4. Verificar variable `ADAPTER_RESTCONSUMER_URL`

### Problema: Health check falla
1. Verificar que `/actuator/health` está habilitado
2. Verificar que el puerto 8080 está expuesto
3. Verificar configuración del target group
4. Verificar dependencias (IAM-SERVICE debe estar saludable)

## 🌐 Red Local para Desarrollo

Para testing local, asegúrate de crear la red compartida:

```bash
# Crear red externa para comunicación entre servicios
docker network create crediya-network || true

# Verificar que ambos servicios están en la misma red
docker network inspect crediya-network
```

## 📊 Métricas y Alertas

### CloudWatch Metrics a monitorear:
- `ECSService/CPUUtilization`
- `ECSService/MemoryUtilization`
- `ApplicationELB/TargetResponseTime`
- `ApplicationELB/HealthyHostCount`

### Alertas recomendadas:
- Alta utilización de CPU/Memoria
- Falla en health checks
- Errores en comunicación con IAM-SERVICE
- Errores de conectividad a Aurora