# Mercadhar Backend - API REST

Mercadhar es una plataforma digital de venta de productos típicos venezolanos, diseñada para conectar a la comunidad venezolana en el exterior y a entusiastas gastronómicos con un catálogo de productos tradicionales. La plataforma gestiona el flujo completo desde la exploración de productos, selección de slots de entrega, hasta el procesamiento de pedidos por parte de clientes y su preparación por parte de administradores.

Este repositorio contiene la implementación del **Backend**, desarrollado como una API REST robusta y segura utilizando Spring Boot.

---

## 🚀 Stack Tecnológico
* **Core**: Java 21 & Spring Boot 3.3.5
* **Seguridad**: Spring Security & JSON Web Tokens (JWT) para autenticación y autorización.
* **Persistencia**: PostgreSQL 16.
* **Migraciones de Base de Datos**: Flyway.
* **Documentación**: OpenAPI 3 & Swagger UI via `springdoc-openapi`.
* **Pruebas**: JUnit 5, MockMvc, AssertJ y Jacoco para reporte de cobertura.
* **Contenedores**: Docker & Docker Compose.
* **Gestión de Dependencias**: Maven.

---

## 🏗️ Arquitectura del Proyecto
El backend sigue una arquitectura por capas clásica con una separación limpia de responsabilidades:
* `config`: Configuración de Spring Security, Swagger y CORS.
* `controller`: Controladores REST expuestos que manejan las solicitudes HTTP y respuestas.
* `service`: Lógica de negocio principal y validaciones.
* `repository`: Capa de persistencia (Spring Data JPA) para interactuar con PostgreSQL.
* `model`: Entidades JPA de base de datos y enums.
* `dto`: Objetos de Transferencia de Datos (DTO) para solicitudes y respuestas estructuradas.
* `security`: Componentes específicos de seguridad JWT (filtros, servicio generador y extractor de tokens).
* `exception`: Manejo global de excepciones (`GlobalExceptionHandler`) para devolver respuestas HTTP consistentes.

---

## 🔐 Modelo de Autenticación y Roles

La seguridad está basada en tokens **JWT sin estado**. Una vez autenticado, el cliente debe incluir el token en la cabecera HTTP `Authorization: Bearer <token>` para todas las peticiones a endpoints protegidos.

### Roles de Usuario
El sistema implementa dos roles con permisos diferenciados:
1. **ROLE_USER (Clientes)**:
   * Ver catálogo de productos y categorías.
   * Consultar bloques de horario disponibles.
   * Crear pedidos de compra (Pick-up o Delivery).
   * Ver su propio historial de pedidos.
   * Cancelar sus propios pedidos.
2. **ROLE_ADMIN (Administrador)**:
   * CRUD completo de productos (crear, editar, borrar y alternar disponibilidad).
   * CRUD completo de categorías de productos.
   * Crear y eliminar bloques de horario (`timeslots`).
   * Consultar todos los pedidos de la plataforma.
   * Actualizar el estado de cualquier pedido (PENDING, CONFIRMED, READY, COMPLETED, CANCELLED).

---

## ⚙️ Configuración del Entorno y Variables

El backend está configurado mediante propiedades parametrizadas por variables de entorno (con valores por defecto seguros para desarrollo local).

### Variables de Entorno Disponibles (`.env`)
El proyecto incluye un archivo `.env` en la raíz con la siguiente configuración preestablecida:
```env
DB_URL=jdbc:postgresql://localhost:5432/mercadhar_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=mercadhar-secret-key-must-be-at-least-32-characters-long
JWT_EXPIRATION=86400000 # 24 horas en milisegundos
```

---

## 🛠️ Instalación y Ejecución

### Requisitos Previos
* Java 21 JDK instalado.
* Docker y Docker Desktop iniciados.
* Maven 3+ instalado (o usar el wrapper `./mvnw` incluido).

### Paso 1: Levantar la Infraestructura (Docker)
Para iniciar la base de datos PostgreSQL, ejecuta el comando desde la raíz del proyecto:
```bash
docker-compose up -d
```
Esto iniciará un contenedor PostgreSQL expuesto en el puerto `5432`. El estado de salud se puede comprobar con `docker ps`.

### Paso 2: Ejecutar las Migraciones y la Aplicación
Las migraciones de Flyway están configuradas para ejecutarse automáticamente al iniciar el servidor. No requiere ninguna intervención manual.

Para arrancar el backend en modo de desarrollo local:
```bash
# En Windows (CMD/PowerShell)
mvnw.cmd spring-boot:run

# En Linux/macOS
./mvnw spring-boot:run
```
La aplicación arrancará por defecto en `http://localhost:8080`.

---

## 📖 Documentación de la API (Swagger UI)

Una vez iniciada la aplicación, se puede explorar la documentación OpenAPI interactiva a través del navegador:
* **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Documentación JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Cómo Autenticarse desde Swagger UI
1. Ve al endpoint `/api/auth/login` (o `/api/auth/register` si es un usuario nuevo).
2. Ejecuta la petición con las credenciales correspondientes.
3. Copia el token JWT de la propiedad `"token"` del JSON de respuesta.
4. Sube al inicio de la página de Swagger, haz clic en el botón verde **Authorize**.
5. Pega el token en el campo de texto (sin prefijos) y haz clic en **Authorize**.
6. A partir de ese momento, todas las peticiones a endpoints protegidos incluirán la cabecera correspondiente.

---

## 🧪 Pruebas Automatizadas

El backend incluye pruebas unitarias para la capa de servicios e integración/aceptación (MockMvc) para la capa de controladores.

### Ejecutar las Pruebas
Los tests se ejecutan sobre una base de datos **H2 en memoria** con el perfil de pruebas activo (`test`). Para ejecutarlos y generar el reporte de cobertura con Jacoco, utiliza:
```bash
# Ejecutar tests
./mvnw clean test
```

### Reporte de Cobertura (Jacoco)
Tras ejecutar los tests, el reporte de cobertura HTML se generará en:
```text
target/site/jacoco/index.html
```
* **Cobertura de instrucciones actual**: **66.30%** (Supera el 60% obligatorio del MVP).

---

## 🔄 Flujo de Trabajo en Git y Kanban

* **Tablero Kanban**: Se gestiona mediante un [Tablero de GitHub Projects / Kanban](#) donde las User Stories se priorizan y se desglosan en tareas técnicas.
* **Estrategia de Ramas**: 
  * La rama `main` contiene el código estable desplegado.
  * Cada User Story o tarea técnica se desarrolla en una rama separada (ej: `feature/auth-jwt`, `bugfix/category-delete`).
  * Una vez finalizado el desarrollo, se abre una *Pull Request* (PR) hacia `main`, se ejecutan las pruebas automatizadas y se procede al merge.
