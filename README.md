# ms-bff - Backend for Frontend

Backend for Frontend construido con Spring Boot para agregación y transformación de datos del sistema PymeTrack.

## 🚀 Características

- ✅ **Agregación de datos** de múltiples microservicios
- ✅ **Transformación de datos** para optimizar respuestas del frontend
- ✅ **WebFlux reactiva** para alto rendimiento
- ✅ **WebClient** para comunicación asíncrona
- ✅ **DTOs especializados** para frontend
- ✅ **Health checking** de microservicios conectados
- ✅ **Documentación** con SpringDoc OpenAPI

## 📋 Requisitos

- Java 17+
- Maven 3.8+
- **ms-admin** corriendo en localhost:8080
- **ms-auth** corriendo en localhost:8085
- **ms-pedidos** corriendo en localhost:8081
- **ms-gateway** corriendo en localhost:8086
- Spring Boot 3.2.0

## 🛠️ Configuración

### Microservicios Conectados
```yaml
services:
  base-url: http://localhost:8086/api/v1
  ms-admin: http://localhost:8080/api/v1
  ms-auth: http://localhost:8085/api/v1
  ms-pedidos: http://localhost:8081/api/v1
```

### Puerto del Servidor
```yaml
server:
  port: 8084
```

## 📚 API Documentation

### Endpoints Principales

#### 1. Dashboard Completo
```http
GET /api/v1/bff/dashboard/{pymeId}
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "pymeInfo": {
    "id": 1,
    "nombrePyme": "TechStore SPA",
    "emailContacto": "contacto@techstore.cl",
    "rutPyme": "76.123.456-7",
    "estado": "ACTIVE"
  },
  "estadisticas": {
    "pedidosTotales": 25,
    "pedidosHoy": 3,
    "productosActivos": 150,
    "stockBajo": 2,
    "ingresosTotales": 2500000.0,
    "ingresosHoy": 150000.0
  },
  "pedidosRecientes": [
    {
      "id": 1,
      "numeroOrden": "ORD-000001",
      "cliente": "Cliente 1",
      "estado": "PENDIENTE",
      "total": 10000.0,
      "fecha": "2024-05-02"
    }
  ],
  "productosActivos": [
    {
      "id": 1,
      "nombreProducto": "Producto 1",
      "codigoSKU": "SKU-0001",
      "stock": 15,
      "precio": 1500.0,
      "categoria": "ELECTRONICA",
      "imagen": "https://via.placeholder.com/100x100"
    }
  ],
  "alertas": [
    {
      "tipo": "STOCK_BAJO",
      "mensaje": "Hay 2 productos con stock bajo",
      "nivel": "WARNING",
      "count": 2
    }
  ],
  "ultimaActualizacion": "2024-05-02T13:45:00"
}
```

#### 2. Estadísticas de PYME
```http
GET /api/v1/bff/pymes/{pymeId}/estadisticas
Authorization: Bearer <token>
```

#### 3. Productos Enriquecidos
```http
GET /api/v1/bff/pymes/{pymeId}/productos?categoria=ELECTRONICA
Authorization: Bearer <token>
```

#### 4. Pedidos Enriquecidos
```http
GET /api/v1/bff/pymes/{pymeId}/pedidos?estado=PENDIENTE
Authorization: Bearer <token>
```

#### 5. Resumen Rápido
```http
GET /api/v1/bff/pymes/{pymeId}/resumen
Authorization: Bearer <token>
```

#### 6. Health Check
```http
GET /api/v1/bff/health
```

## 🔄 Arquitectura de Agregación

### Flujo de Datos
```
Frontend → Gateway (8086) → BFF (8084) → Múltiples Microservicios
                                      ├──→ ms-admin (8080) - Datos PYME
                                      ├──→ ms-pedidos (8081) - Productos/Pedidos
                                      └──→ ms-auth (8085) - Validación (opcional)
```

### Ejemplo de Dashboard
```bash
# Una llamada al BFF reemplaza múltiples llamadas directas:
GET /api/v1/bff/dashboard/1

# Equivalente a las siguientes llamadas directas:
GET /api/v1/pymes/1                    # ms-admin
GET /api/v1/productos/pyme/1           # ms-pedidos  
GET /api/v1/pedidos/pyme/1            # ms-pedidos
# + Procesamiento de datos y cálculos
```

## 🚀 Ejecución

### Desarrollo
```bash
mvn spring-boot:run
```

### Producción
```bash
mvn clean package
java -jar target/ms-bff-0.0.1-SNAPSHOT.jar
```

## 📖 Swagger UI

Accede a la documentación interactiva en:
```
http://localhost:8084/swagger-ui.html
```

## 🗂️ Estructura del Proyecto

```
src/main/java/cl/pymetrack/msbff/
├── dto/                    # DTOs especializados para frontend
│   └── DashboardDTO.java
├── client/                 # Clientes de microservicios
│   └── MicroserviceClient.java
├── service/               # Lógica de agregación
│   └── BffService.java
├── controller/            # Endpoints BFF
│   └── BffController.java
└── MsBffApplication.java
```

## 🔄 Comunicación con Microservicios

### WebClient Configuration
```java
// Cliente reactiva con timeout y error handling
WebClient.builder()
    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
    .build();
```

### Patrones de Comunicación
```java
// Llamadas paralelas para optimizar rendimiento
Mono.zip(pymeData, productos, pedidos)
    .map(tuple -> buildDashboard(tuple.getT1(), tuple.getT2(), tuple.getT3()));
```

## 📊 Optimizaciones Implementadas

### 1. Agregación de Datos
- **Single Call**: Múltiples microservicios en una llamada
- **Parallel Processing**: Llamadas concurrentes con Mono.zip
- **Data Transformation**: DTOs optimizados para frontend

### 2. Caching Strategy
- **Response Caching**: Respuestas cacheadas por 5 minutos
- **Service Health**: Verificación periódica de servicios
- **Fallback Data**: Datos por defecto si servicios caen

### 3. Error Handling
- **Graceful Degradation**: Respuestas parciales si algún servicio falla
- **Timeout Management**: 5 segundos por llamada individual
- **Circuit Breaker**: Protección contra cascadas de fallos

## 📈 Beneficios del BFF

### Para el Frontend
- **Menor Latencia**: Una llamada vs múltiples llamadas
- **Datos Optimizados**: DTOs específicos para UI
- **Menor Complejidad**: Lógica de negocio en backend
- **Consistencia**: Datos consistentes entre vistas

### Para la Arquitectura
- **Desacoplamiento**: Frontend independiente de microservicios
- **Flexibilidad**: Cambios en backend sin afectar frontend
- **Performance**: Agregación y caching en backend
- **Security**: Punto central de validación

## 🐛 Troubleshooting

### Error: Servicio no disponible
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Servicios no disponibles"
}
```

**Solución:** Verificar que los microservicios estén corriendo en sus puertos.

### Error: Timeout
```json
{
  "status": 504,
  "error": "Gateway Timeout",
  "message": "Timeout esperando respuesta de microservicio"
}
```

**Solución:** Verificar rendimiento de microservicios o ajustar timeouts.

### Error: Datos incompletos
```json
{
  "pymeInfo": null,
  "estadisticas": {},
  "alertas": []
}
```

**Solución:** Verificar health status de servicios conectados.

## 📊 Monitoreo

### Health Check
```bash
# Verificar estado del BFF y servicios conectados
curl http://localhost:8084/api/v1/bff/health
```

**Respuesta:**
```json
{
  "services": {
    "ms-admin": true,
    "ms-auth": true,
    "ms-pedidos": true
  },
  "bff": "UP",
  "overall": "UP",
  "activeServices": 3,
  "totalServices": 3,
  "timestamp": "2024-05-02T13:45:00"
}
```

### Actuator Endpoints
```bash
# Métricas del BFF
curl http://localhost:8084/actuator/metrics

# Información del servicio
curl http://localhost:8084/actuator/info
```

## 🤝 Integración con Gateway

El BFF está configurado para ser accedido a través del API Gateway:

```yaml
# En ms-gateway/application.yml
- id: ms-bff
  uri: http://localhost:8084
  predicates:
    - Path=/api/v1/bff/**
```

### Flujo Completo
```
Frontend → Gateway (8086) → BFF (8084) → Microservicios
```

## 📝 Notas de Desarrollo

- **Reactividad**: Construido con WebFlux para máxima concurrencia
- **Timeouts**: 5 segundos por llamada individual
- **Memory**: 1MB max buffer size para respuestas grandes
- **Error Handling**: Graceful degradation con datos parciales
- **Logging**: Nivel DEBUG para entorno de desarrollo

## 🔮 Próximas Mejoras

1. **Redis Caching**: Caching distribuido para mayor rendimiento
2. **GraphQL**: Endpoint GraphQL para consultas flexibles
3. **Event Sourcing**: Actualizaciones en tiempo real con WebSockets
4. **Rate Limiting**: Limitar peticiones por cliente
5. **Data Enrichment**: Enriquecer datos con información externa

## 🎯 Casos de Uso Típicos

### Dashboard Principal
```javascript
// Frontend hace una sola llamada
const dashboard = await fetch('/api/v1/bff/dashboard/1', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// Recibe todo el dato necesario para la vista
// - Info de PYME
// - Estadísticas calculadas
// - Pedidos recientes
// - Productos activos
// - Alertas relevantes
```

### Lista con Filtros
```javascript
// Productos con filtro de categoría
const productos = await fetch('/api/v1/bff/pymes/1/productos?categoria=ELECTRONICA');

// Pedidos con filtro de estado
const pedidos = await fetch('/api/v1/bff/pymes/1/pedidos?estado=PENDIENTE');
```

### Resumen Rápido
```javascript
// Para componentes que necesitan datos mínimos
const resumen = await fetch('/api/v1/bff/pymes/1/resumen');
// Retorna solo datos clave: pedidos totales, productos activos, ingresos hoy
```
