# Franchise Management API — Guía de ejecución local

API reactiva para gestión de franquicias construida con **Spring Boot 3.3.6 + WebFlux + DynamoDB**.  
Arquitectura limpia (Hexagonal), despliegue en AWS ECS Fargate.

---

## Requisitos previos

| Herramienta    | Versión mínima | Verificación             |
|----------------|----------------|--------------------------|
| Docker         | 24+            | `docker --version`       |
| Docker Compose | 2.x            | `docker compose version` |
| Git            | 2.x            | `git --version`          |

> No se requiere Java, AWS CLI ni credenciales reales.

---

## Ejecución local

Levanta DynamoDB Local + la aplicación con un solo comando. No necesitas Java instalado localmente.

### 1. Clonar el repositorio

```bash
git clone https://github.com/sebashmp/franchise-api.git
cd franchise-api
```

### 2. Levantar todo

```bash
docker compose up --build
```

Esto ejecuta en orden:
1. **DynamoDB Local** en el puerto `8000`
2. **dynamodb-init** — crea las 3 tablas (`franchises`, `branches`, `products`) con sus GSIs
3. **franchise-api** — compila el JAR y arranca Spring Boot en el puerto `8080`

> La primera vez tarda ~3-5 minutos porque descarga dependencias de Gradle y la imagen de Java.  
> Las siguientes veces son más rápidas gracias al cache de Docker.

### 3. Verificar que está corriendo

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{ "status": "UP" }
```

### 4. Apagar

```bash
docker compose down
```

---

## Endpoints disponibles

Base URL local: `http://localhost:8080`

| Método   | Ruta                                              | Descripción                          |
|----------|---------------------------------------------------|--------------------------------------|
| `POST`   | `/api/v1/franchises`                              | Crear franquicia                     |
| `PATCH`  | `/api/v1/franchises/{franchiseId}`                | Actualizar nombre de franquicia      |
| `GET`    | `/api/v1/franchises/{franchiseId}/top-products`   | Producto con mayor stock por sucursal|
| `POST`   | `/api/v1/franchises/{franchiseId}/branches`       | Agregar sucursal a franquicia        |
| `PATCH`  | `/api/v1/branches/{branchId}`                     | Actualizar nombre de sucursal        |
| `POST`   | `/api/v1/branches/{branchId}/products`            | Agregar producto a sucursal          |
| `DELETE` | `/api/v1/branches/{branchId}/products/{productId}`| Eliminar producto de sucursal        |
| `PATCH`  | `/api/v1/products/{productId}/stock`              | Actualizar stock de producto         |
| `PATCH`  | `/api/v1/products/{productId}`                    | Actualizar nombre de producto        |

---

## Swagger UI

Una vez la aplicación esté corriendo, accede a la documentación interactiva:

```
http://localhost:8080/swagger-ui.html
```

---

## Pruebas con Postman

En la raíz del repositorio se incluyen dos colecciones:

| Archivo                                             | Descripción                              |
|-----------------------------------------------------|------------------------------------------|
| `Franchise Management API — E2E.postman_collection.json` | Apunta a `localhost:8080`           |
| `Franchise Management API — Dev.postman_collection.json` | Apunta al ALB de AWS dev            |

### Pasos para importar

1. Abrir Postman → **Import**
2. Seleccionar el archivo `.postman_collection.json`
3. Ejecutar la colección **en orden** — los scripts de test encadenan los IDs automáticamente entre requests

### Flujo de la colección E2E

```
1. Crear franquicia           → guarda franchiseId
2. Actualizar nombre          → usa franchiseId
3. Crear sucursal 1           → guarda branchId1
4. Crear sucursal 2           → guarda branchId2
5. Agregar producto 1 (s1)    → guarda productId1
6. Agregar producto 2 (s1)    → guarda productId2
7. Agregar producto 3 (s2)    → guarda productId3
8. Actualizar stock           → usa productId1
9. Actualizar nombre producto → usa productId2
10. Eliminar producto         → usa productId3
11. Top productos por sucursal → usa franchiseId
    └── carpeta Errores: casos 400 y 404
```