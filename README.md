# 🛒 Mercadhar

> Tienda online de productos venezolanos con gestión de pedidos por franjas horarias.

---

## Objetivo del producto

Mercadhar es una aplicación web que permite a la comunidad venezolana residente en España comprar productos típicos de su país de forma online. Los clientes pueden explorar el catálogo, crear pedidos y elegir una franja horaria para recoger en tienda o recibir a domicilio. El administrador gestiona el catálogo, las franjas horarias y el estado de cada pedido desde un panel dedicado.

## Problema que resuelve

Las tiendas de productos latinos suelen gestionar sus pedidos por WhatsApp o teléfono, lo que genera desorganización, errores y pérdida de tiempo tanto para el negocio como para el cliente. Mercadhar centraliza todo el proceso: el cliente hace su pedido online, elige cuándo recogerlo y puede seguir su estado en tiempo real; el negocio lo gestiona desde un panel sin depender de mensajería.

---

## Stack tecnológico

### Backend
| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.5 | Framework de aplicación |
| Spring Security | 6 | Autenticación y autorización |
| Spring Data JPA | 3.3.5 | Acceso a datos |
| PostgreSQL | 16 | Base de datos |
| Flyway | — | Migraciones de BD |
| JWT (jjwt) | 0.11.5 | Tokens de autenticación |
| Lombok | 1.18.46 | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.6.0 | Documentación Swagger |
| JUnit 5 + Mockito | — | Tests unitarios e integración |
| H2 | — | BD en memoria para tests |

### Frontend
| Tecnología | Versión | Uso |
|---|---|---|
| React | 18 | Interfaz de usuario |
| React Router | 6 | Navegación SPA |
| Axios | — | Peticiones HTTP |
| Vite | — | Bundler y servidor de desarrollo |
| CSS Modules | — | Estilos encapsulados |

### Infraestructura
| Tecnología | Uso |
|---|---|
| Docker + Docker Compose | Contenedores para BD, backend y frontend |
| Maven | Gestión de dependencias y build del backend |
| npm | Gestión de dependencias del frontend |

---

## Arquitectura

```
mercadhar/
├── src/                        # Backend Spring Boot
│   ├── controller/             # Endpoints REST (AuthController, OrderController…)
│   ├── service/                # Lógica de negocio
│   ├── repository/             # Acceso a datos (JPA)
│   ├── model/                  # Entidades JPA (User, Order, Product…)
│   ├── dto/                    # Objetos de transferencia (request/response)
│   ├── security/               # JwtService, UserDetailsServiceImpl
│   ├── config/                 # SecurityConfig, SwaggerConfig
│   ├── exception/              # Excepciones personalizadas
│   └── resources/
│       └── db/migration/       # Scripts SQL de Flyway (V1, V2, V3…)
│
└── frontend/                   # React + Vite
    └── src/
        ├── pages/              # Vistas (CatalogPage, OrderPage, Admin…)
        ├── components/         # Componentes reutilizables (Navbar, Modal…)
        ├── api/                # Clientes Axios (orderApi, productApi…)
        ├── hooks/              # Hooks personalizados (useProducts, useOrders…)
        ├── context/            # AuthContext (estado global de sesión)
        ├── routes/             # AppRouter, ProtectedRoute
        └── utils/              # Formatters (formatDate, translateStatus…)
```

El backend sigue una arquitectura en capas clásica: **Controller → Service → Repository**. El frontend es una SPA que consume la API REST del backend mediante Axios, con el JWT almacenado en `localStorage` y adjuntado automáticamente en cada petición a través de un interceptor de Axios.

---

## Modelo de autenticación y roles

### Flujo de autenticación

1. El usuario se registra (`POST /api/auth/register`) o inicia sesión (`POST /api/auth/login`).
2. El backend valida las credenciales y devuelve un **JWT firmado** con validez de 24 horas.
3. El frontend almacena el token y lo incluye en la cabecera `Authorization: Bearer <token>` de cada petición.
4. El `JwtAuthenticationFilter` intercepta cada petición, valida el token y carga el usuario en el contexto de seguridad de Spring.

### Roles

| Rol | Descripción | Acceso |
|---|---|---|
| `ROLE_USER` | Cliente registrado | Ver catálogo, crear pedidos, ver y cancelar sus propios pedidos |
| `ROLE_ADMIN` | Administrador | Todo lo anterior + gestionar productos, categorías, franjas horarias y cambiar el estado de cualquier pedido |

El usuario administrador por defecto se crea automáticamente con la migración `V1__init_schema.sql`:

```
Email:    admin@mercadhar.com
Password: password
```

> ⚠️ Cambia esta contraseña en producción.

---

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/mercadhar_db` |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `postgres` |
| `JWT_SECRET` | Clave secreta para firmar los tokens (mín. 32 caracteres) | `mercadhar-secret-key-must-be-at-least-32-characters-long-2024` |
| `JWT_EXPIRATION` | Duración del token en milisegundos | `86400000` (24 h) |

En desarrollo local estas variables se leen de `application.properties`. En Docker se pasan directamente en `docker-compose.yml` bajo la clave `environment`.

---

## Instalación y ejecución

### Prerrequisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en ejecución.
- Git para clonar el repositorio.

### Con Docker (recomendado)

```bash
# 1. Clona el repositorio
git clone https://github.com/tu-usuario/mercadhar.git
cd mercadhar

# 2. Construye y levanta los tres servicios (BD, backend, frontend)
docker compose up --build

# 3. Accede a la aplicación
#    Frontend  → http://localhost:5174
#    Backend   → http://localhost:8080
#    Swagger   → http://localhost:8080/swagger-ui.html
```

Para detener todos los servicios:

```bash
docker compose down
```

Para detener y eliminar también los datos persistidos en la base de datos:

```bash
docker compose down --volumes
```

### En local sin Docker

**Backend:**

```bash
# Necesitas PostgreSQL corriendo en localhost:5432 con la base de datos mercadhar_db

cd mercadhar   # carpeta raíz del proyecto Spring Boot
./mvnw spring-boot:run
```

**Frontend:**

```bash
cd frontend
npm install
npm run dev
```

---

## Migraciones de base de datos

Las migraciones se gestionan con **Flyway** y se aplican **automáticamente al arrancar el backend**, tanto en local como en Docker. No es necesario ejecutar ningún comando manual.

Los scripts se encuentran en `src/main/resources/db/migration/` y se ejecutan en orden:

| Script | Contenido |
|---|---|
| `V1__init_schema.sql` | Crea todas las tablas y el usuario administrador por defecto |
| `V2__seed_data.sql` | Inserta categorías y productos de ejemplo |
| `V3__add_product_images.sql` | Añade URLs de imágenes a los productos existentes |

Si añades una nueva migración, nómbrala `V4__descripcion.sql` y Flyway la detectará y ejecutará en el siguiente arranque.

---

## Documentación Swagger / OpenAPI

### En local

Una vez arrancado el backend, accede a:

```
http://localhost:8080/swagger-ui.html
```

El JSON de la especificación OpenAPI está disponible en:

```
http://localhost:8080/api-docs
```

### Cómo autenticarse desde Swagger UI para probar endpoints protegidos

1. Llama al endpoint `POST /api/auth/login` desde Swagger con las credenciales del admin (o de cualquier usuario registrado). La respuesta incluye un campo `token`.
2. Copia el valor del token (sin comillas).
3. Haz clic en el botón **Authorize 🔒** en la parte superior derecha de Swagger UI.
4. En el campo que aparece, escribe exactamente:
   ```
   Bearer <pega aquí el token>
   ```
5. Haz clic en **Authorize** y cierra el diálogo.
6. A partir de ese momento todos los endpoints que ejecutes desde Swagger incluirán el token automáticamente.

---

## Tests

El proyecto incluye tres suites de tests ubicadas en `src/test/java/mercadhar/`:

### Tests unitarios de servicio — con Mockito

Usan `@ExtendWith(MockitoExtension.class)` para aislar la capa de servicio mockeando los repositorios. No necesitan base de datos.

- **`AuthServiceTest`** — registro e inicio de sesión: credenciales correctas, email duplicado, contraseña incorrecta.
- **`ProductServiceTest`** — CRUD de productos: creación, búsqueda por ID, producto no encontrado.
- **`OrderServiceTest`** — creación de pedidos, cambio de estado, restricciones por rol.

### Tests de integración de controlador — con MockMvc

Usan `@SpringBootTest` + `@AutoConfigureMockMvc` con perfil `test` (base de datos H2 en memoria). Prueban el endpoint completo incluyendo Spring Security.

- **`AuthControllerTest`** — `POST /api/auth/register` y `POST /api/auth/login`: respuestas HTTP, estructura del JSON, manejo de errores.
- **`ProductControllerTest`** — `GET /api/products`, `POST /api/products`: acceso anónimo, acceso con token de usuario, acceso con token de admin.

### Cómo ejecutar los tests

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar solo una clase
./mvnw test -Dtest=OrderServiceTest

# Ejecutar con informe de cobertura (JaCoCo)
./mvnw verify
# El informe se genera en target/site/jacoco/index.html
```

---

## Flujo de trabajo: issues, ramas y pull requests

El desarrollo sigue un flujo **GitHub Flow** adaptado:

1. **Issue**: cada funcionalidad o bug se registra como un issue en el tablero del proyecto con su etiqueta (`feature`, `bug`, `enhancement`).
2. **Rama**: se crea una rama desde `main` con el formato `feature/nombre-corto` o `fix/nombre-corto`.
3. **Desarrollo**: los commits se hacen sobre esa rama con mensajes descriptivos siguiendo el formato `tipo(scope): descripción` (ej. `feat(orders): add pagination to admin orders endpoint`).
4. **Pull Request**: al terminar, se abre un PR hacia `main` con referencia al issue (`Closes #12`). Se revisa el código antes de hacer merge.
5. **Merge**: se hace merge con squash para mantener el historial limpio.
6. **Cierre**: el issue se cierra automáticamente al hacer merge del PR.

---

## Funcionalidades implementadas

### Clientes
- [x] Registro e inicio de sesión con JWT
- [x] Exploración del catálogo con filtros por categoría
- [x] Creación de pedidos en 3 pasos (productos → entrega → confirmación)
- [x] Elección de franja horaria disponible
- [x] Selección de tipo de entrega: recogida en tienda o delivery
- [x] Seguimiento del estado del pedido con tracker visual
- [x] Cancelación de pedidos propios (en estado Pendiente o Confirmado)

### Administrador
- [x] Gestión de productos: crear, editar, activar/desactivar
- [x] Gestión de categorías
- [x] Gestión de franjas horarias con control de ocupación
- [x] Visualización de todos los pedidos con filtros por estado
- [x] Cambio de estado de cualquier pedido (Pendiente → Confirmado → Listo → Completado / Cancelado)

### Técnicas
- [x] Autenticación stateless con JWT
- [x] Migraciones de BD automáticas con Flyway
- [x] Documentación interactiva con Swagger / OpenAPI
- [x] Tests unitarios y de integración
- [x] Despliegue completo con Docker Compose (BD + backend + frontend)
- [x] CORS configurado para desarrollo local

---

## Tablero de User Stories

> 🔗 [Tablero del proyecto en GitHub Projects](https://github.com/tu-usuario/mercad
> *(https://github.com/users/carlasalmeron/projects/4)*

---

## Autora
**Carla Salmerón**