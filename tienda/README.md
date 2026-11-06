# 🛒 TiendaApp - API REST con Autenticación JWT y Control de Roles

API REST para la plataforma de comercio electrónico **TiendaApp**, desarrollada en Java 17 con Spring Boot. El sistema implementa una arquitectura de seguridad manual (sin el starter completo de Spring Security), gestionando la autenticación mediante tokens **JWT** (JJWT 0.12.5), encriptación de contraseñas con **BCryptPasswordEncoder**, filtro de inspección por petición (`OncePerRequestFilter`), restricción por roles declarativa (`@RequiresRole`) con un `HandlerInterceptor`, y estricto control de propiedad sobre productos y pedidos.

---

## 📋 Tabla de Contenidos
- [Requisitos Técnicos](#-requisitos-técnicos)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Configuración de Base de Datos y Variables de Entorno](#-configuración-de-base-de-datos-y-variables-de-entorno)
- [Credenciales Iniciales de Acceso](#-credenciales-iniciales-de-acceso)
- [Instrucciones de Ejecución](#-instrucciones-de-ejecución)
- [Endpoints y Control de Acceso](#-endpoints-y-control-de-acceso)
- [Evidencias de Casos de Prueba (Rúbrica)](#-evidencias-de-casos-de-prueba-rúbrica)
- [Retos Extra Implementados](#-retos-extra-implementados)
- [Colección de Postman](#-colección-de-postman)

---

## 🛠 Requisitos Técnicos

| Elemento | Especificación |
| :--- | :--- |
| **Lenguaje** | Java 17+ (OpenJDK 21 compatible) |
| **Framework** | Spring Boot (Web MVC + Spring Data JPA) |
| **Base de Datos** | MySQL 5.7+ / 8.0+ / MariaDB (puerto 3306) |
| **JWT** | `io.jsonwebtoken` (jjwt-api, jjwt-impl, jjwt-jackson) versión 0.12.5 |
| **Seguridad / Criptografía** | `spring-security-crypto` (solo `BCryptPasswordEncoder`) |
| **Puerto y Context-Path** | Puerto `9090` \| Context-Path `/api/v1/tienda` |
| **Gestor de dependencias** | Maven (`mvnw` wrapper incluido) |

---

## 📂 Estructura del Proyecto

El código fuente sigue estrictamente la arquitectura modular requerida en el paquete base `com.juanma.tienda`:

```text
com.juanma.tienda
├── config
│   ├── AppConfig.java              # Bean de PasswordEncoder (BCrypt)
│   ├── FilterConfig.java           # Registro de JwtValidationFilter con FilterRegistrationBean
│   └── WebConfig.java              # Registro de RoleInterceptor en InterceptorRegistry
├── controller
│   ├── AuthController.java         # /auth (register, login, refreshtoken)
│   ├── CategoriesController.java   # /categories (CRUD de categorías)
│   ├── OrdersController.java       # /orders (pedidos, ventas, cancelación, estados)
│   ├── ProductsController.java     # /products (catálogo, mis productos, CRUD)
│   └── UsersController.java        # /users (perfil me, listado, creación, cambio de rol/estado)
├── dto
│   ├── HttpGlobalResponseDTO.java  # Envoltorio estándar { message, data }
│   ├── AdminUserRequestDTO.java    # Creación de usuarios con rol
│   ├── AuthResponseDTO.java        # Respuesta con JWT y UserResponseDTO
│   ├── CategoryRequestDTO.java     # Request de categoría
│   ├── CategoryResponseDTO.java    # Response de categoría
│   ├── LoginRequestDTO.java        # Request de login (user y password)
│   ├── OrderRequestDTO.java        # Creación de pedido (productId, quantity)
│   ├── OrderResponseDTO.java       # Detalle de pedido con total calculado
│   ├── OrderStatusUpdateRequestDTO.java # Cambio de estado de pedido
│   ├── ProductRequestDTO.java      # Creación/edición de producto
│   ├── ProductResponseDTO.java     # Detalle de producto con vendedor y categoría
│   ├── RegisterRequestDTO.java     # Registro público de cliente
│   ├── RoleUpdateRequestDTO.java   # Actualización de rol
│   ├── StatusUpdateRequestDTO.java # Activación/desactivación de usuario
│   └── UserResponseDTO.java        # Datos de usuario (sin password)
├── entity
│   ├── Categories.java             # Entidad categories
│   ├── Orders.java                 # Entidad orders
│   ├── Products.java               # Entidad products
│   ├── Roles.java                  # Entidad roles
│   └── Users.java                  # Entidad users
├── enums
│   ├── OrderStatusEnum.java        # PENDIENTE, ENVIADO, ENTREGADO, CANCELADO
│   └── RoleEnum.java               # ADMIN (1), CLIENTE (2), VENDEDOR (3)
├── exception
│   ├── BadRequestException.java    # 400
│   ├── ConflictException.java      # 409
│   ├── ForbiddenException.java     # 403
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice centralizado
│   ├── ResourceNotFoundException.java # 404
│   └── UnauthorizedException.java  # 401
├── filter
│   └── JwtValidationFilter.java    # OncePerRequestFilter para autenticación JWT
├── repository
│   ├── CategoriesRepository.java
│   ├── OrdersRepository.java
│   ├── ProductsRepository.java
│   ├── RolesRepository.java
│   └── UsersRepository.java
├── security
│   ├── RequiresRole.java           # Anotación para métodos y clases
│   └── RoleInterceptor.java        # HandlerInterceptor para validación de roles
├── service
│   ├── AuthService.java            # Login, registro y refresh de token
│   ├── CategoriesService.java      # Lógica de categorías y validación de borrado
│   ├── JwtService.java             # Generación, parseo y validación HMAC de JWT
│   ├── OrdersService.java          # Lógica de pedidos, cálculo de total y stock
│   ├── ProductsService.java        # Catálogo, reglas de propiedad y paginación
│   └── UsersService.java           # Gestión administrativa de usuarios
└── TiendaApplication.java          # Clase principal de Spring Boot
```

---

## ⚙ Configuración de Base de Datos y Variables de Entorno

El archivo `application.yaml` lee las configuraciones desde variables de entorno del sistema con valores por defecto preparados para entorno local:

```yaml
server:
  port: 9090
  servlet:
    context-path: /api/v1/tienda

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tienda_db
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true

jwt:
  secret: ${JWT_SECRET:bWlfY2xhdmVfc2VjcmV0YV9tdXlfbGFyZ2FfcGFyYV90aWVuZGFfYXBwXzIwMjY=}
  expiration: ${JWT_EXPIRATION:600000} # 10 minutos (en milisegundos)
```

### Variables de Entorno Disponibles:
- `DB_USER`: Usuario de la base de datos MySQL (por defecto `root`).
- `DB_PASSWORD`: Contraseña de MySQL (por defecto vacía).
- `JWT_SECRET`: Clave secreta en Base64 con un mínimo de 256 bits (32 bytes).
- `JWT_EXPIRATION`: Tiempo de validez del JWT en milisegundos (`600000` = 10 minutos).

### Configuración en Windows (PowerShell):
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = ""
$env:JWT_SECRET = "bWlfY2xhdmVfc2VjcmV0YV9tdXlfbGFyZ2FfcGFyYV90aWVuZGFfYXBwXzIwMjY="
$env:JWT_EXPIRATION = "600000"
```

---

## 🔑 Credenciales Iniciales de Acceso

El script SQL suministrado (`tienda.sql`) carga los roles del sistema y el superusuario inicial:

- **Rol:** `ADMIN` (id: 1)
- **Usuario:** `admin`
- **Correo:** `admin@tienda.com`
- **Contraseña:** `admin123`
- **Hash BCrypt almacenado:** `$2a$10$NbUom3pEENiqBdp9Y4PnVupsPnt4n9spv4jg9Jzo8aNvQolczCIp2`

---

## 🚀 Instrucciones de Ejecución

### 1. Cargar la Base de Datos
Ejecuta el archivo `tienda.sql` en tu gestor de base de datos MySQL favorito (MySQL Workbench, phpMyAdmin, DBeaver o consola):

```bash
mysql -u root -p < tienda.sql
```

### 2. Compilar y Ejecutar la Aplicación
Desde la raíz del proyecto `tienda`:

```bash
# En Windows (PowerShell / CMD):
.\mvnw.cmd spring-boot:run

# En Linux / macOS:
./mvnw spring-boot:run
```

La aplicación quedará disponible en:
`http://localhost:9090/api/v1/tienda`

---

## 📡 Endpoints y Control de Acceso

Todas las respuestas están estructuradas bajo `HttpGlobalResponseDTO<T>` con los atributos `message` y `data`.

### 1. Autenticación (Público)
| Método | Ruta | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | Público | Registra cliente nuevo (rol `CLIENTE`). Duplicados → `409 Conflict`. |
| `POST` | `/auth/login` | Público | Recibe `user` (username o email) y `password`. Devuelve JWT y datos del usuario. Credenciales inválidas o inactivo → `401 Unauthorized`. |
| `POST` | `/auth/refreshtoken` | Público | Recibe header `Authorization: Bearer <token>` y genera un nuevo token válido. |

### 2. Usuarios
| Método | Ruta | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/users/me` | ADMIN, CLIENTE, VENDEDOR | Retorna los datos del usuario autenticado a partir del `userId` del token. |
| `GET` | `/users` | ADMIN | Lista todos los usuarios registrados. |
| `POST` | `/users` | ADMIN | Crea usuarios con cualquier rol (VENDEDOR u otro ADMIN). |
| `PATCH` | `/users/{id}/role` | ADMIN | Modifica el rol de un usuario existente. |
| `PATCH` | `/users/{id}/status` | ADMIN | Activa o desactiva la cuenta de un usuario. |

### 3. Categorías
| Método | Ruta | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/categories` | ADMIN, CLIENTE, VENDEDOR | Listado de categorías de productos. |
| `POST` | `/categories` | ADMIN | Registra categoría. Nombre repetido → `409 Conflict`. |
| `DELETE` | `/categories/{id}` | ADMIN | Elimina categoría si no tiene productos; si tiene productos asociados → `409 Conflict`. |

### 4. Productos
| Método | Ruta | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/products` | Público | Catálogo general. Soporta filtros opcionales `?categoryId=` y paginación `?page=&size=`. |
| `GET` | `/products/{id}` | Público | Detalle de un producto por ID. |
| `GET` | `/products/mine` | VENDEDOR | Lista los productos pertenecientes al vendedor autenticado. |
| `POST` | `/products` | VENDEDOR | Registra producto. El `seller_id` se extrae automáticamente del token JWT. |
| `PUT` | `/products/{id}` | VENDEDOR, ADMIN | Edita producto. El VENDEDOR solo puede editar si es el dueño; en caso contrario → `403 Forbidden`. |
| `DELETE` | `/products/{id}` | VENDEDOR, ADMIN | Elimina producto bajo la misma regla estricta de propiedad. |

### 5. Pedidos
| Método | Ruta | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/orders` | CLIENTE | Crea pedido con `productId` y `quantity`. Valida stock: si es insuficiente → `400 Bad Request`. Descuenta stock y calcula el `total = price × quantity` en servidor. |
| `GET` | `/orders/mine` | CLIENTE | Lista los pedidos realizados por el cliente autenticado. |
| `GET` | `/orders/sales` | VENDEDOR | Lista los pedidos recibidos sobre productos del vendedor autenticado. |
| `GET` | `/orders` | ADMIN | Lista todos los pedidos globales de la tienda. |
| `PATCH` | `/orders/{id}/status` | VENDEDOR, ADMIN | Actualiza el estado (`PENDIENTE`, `ENVIADO`, `ENTREGADO`, `CANCELADO`). El VENDEDOR solo puede alterar pedidos de sus propios productos; si no → `403 Forbidden`. |
| `PATCH` | `/orders/{id}/cancel` | CLIENTE | Cancela el propio pedido del cliente. Solo permitido si el estado es `PENDIENTE`. Reintegra el stock al producto. |

---

## 🧪 Evidencias de Casos de Prueba (Rúbrica)

A continuación se presentan las respuestas reales obtenidas por la suite de pruebas automatizada:

### Caso 1: Login Correcto
- **Petición:** `POST /api/v1/tienda/auth/login`
- **Body:** `{"user": "admin", "password": "admin123"}`
- **Código HTTP:** `200 OK`
- **Respuesta:**
```json
{
  "message": "Inicio de sesión exitoso",
  "data": {
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sSWQiOjEsImlhdCI6MTc5Mzk3NjM2NCwiZXhwIjoxNzkzOTc2OTY0fQ.hMgeoHhHkRwnOSeOr3-Imk_jmBIyJMCtu2Lni7r56cw",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@tienda.com",
      "active": true,
      "role": "ADMIN"
    }
  }
}
```

---

### Caso 2: Token Ausente
- **Petición:** `GET /api/v1/tienda/users/me`
- **Header:** *(Sin encabezado Authorization)*
- **Código HTTP:** `401 Unauthorized`
- **Respuesta:**
```json
{
  "message": "Acceso no autorizado: token no proporcionado o formato inválido",
  "data": null
}
```

---

### Caso 3: Token Expirado o Inválido
- **Petición:** `GET /api/v1/tienda/users/me`
- **Header:** `Authorization: Bearer token_falso_invalido_o_expirado`
- **Código HTTP:** `401 Unauthorized`
- **Respuesta:**
```json
{
  "message": "Acceso no autorizado: token inválido o expirado",
  "data": null
}
```

---

### Caso 4: Rol No Permitido
- **Petición:** `GET /api/v1/tienda/users`
- **Header:** `Authorization: Bearer <Token_De_CLIENTE>`
- **Código HTTP:** `403 Forbidden`
- **Respuesta:**
```json
{
  "message": "Acceso denegado: no cuenta con el rol requerido para esta acción",
  "data": null
}
```

---

### Caso 5: Vendedor Editando Producto Ajeno (Regla de Propiedad)
- **Petición:** `PUT /api/v1/tienda/products/1` (El producto 1 pertenece a `pedro_vendedor`)
- **Header:** `Authorization: Bearer <Token_De_maria_vendedora>`
- **Código HTTP:** `403 Forbidden`
- **Respuesta:**
```json
{
  "message": "Acceso denegado: no es propietario de este producto",
  "data": null
}
```

---

### Caso 6: Pedido Sin Stock Suficiente
- **Petición:** `POST /api/v1/tienda/orders`
- **Body:** `{"productId": 1, "quantity": 9999}` (Stock existente: 3 unidades)
- **Código HTTP:** `400 Bad Request`
- **Respuesta:**
```json
{
  "message": "Stock insuficiente para el producto 'Monitor 4K'. Stock disponible: 3",
  "data": null
}
```

---

## 🏆 Retos Extra Implementados

1. **Validación de DTOs con Bean Validation:**
   Se incorporó `spring-boot-starter-validation` aplicando `@NotBlank`, `@Email`, `@Size`, `@NotNull`, `@Min` y `@DecimalMin`. Los errores de validación retornan código `400 Bad Request` detallando el campo y mensaje específico.

2. **Manejo Global Centralizado con `@RestControllerAdvice`:**
   La clase `GlobalExceptionHandler` intercepta todas las excepciones (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ConflictException`, `MethodArgumentNotValidException`), eliminando bloques repetitivos de `try/catch` en los controladores y garantizando siempre la estructura uniforme `HttpGlobalResponseDTO`.

3. **Anotación `@RequiresRole` a Nivel de Clase y Método:**
   `@RequiresRole` tiene como objetivo `@Target({ElementType.METHOD, ElementType.TYPE})`. En `RoleInterceptor`, si el método invocado no contiene la anotación directamente, el interceptor inspecciona automáticamente si la clase del controlador la tiene definida.

4. **Paginación en Catálogo de Productos:**
   El endpoint público `GET /products` soporta los parámetros `page` y `size` (por ejemplo `/products?page=0&size=10`), retornando una página con metadata (`Page<ProductResponseDTO>`) gracias a Spring Data JPA.

---

## 📬 Colección de Postman

El repositorio incluye el archivo listo para importar en Postman o Thunder Client:
`tienda_postman_collection.json`

### Características de la Colección:
- **Variables dinámicas:** `{{baseUrl}}`, `{{token}}`, `{{adminToken}}`, `{{vendedorToken}}`, `{{clienteToken}}`, `{{productId}}`, `{{orderId}}`.
- **Scripts de prueba automatizados (Tests):** Al ejecutar el login de cualquier rol, el script guarda automáticamente el JWT recibido en la variable `{{token}}` para que el resto de peticiones funcionen de forma inmediata sin copiar y pegar manualmente.
- **Carpeta de evidencias:** Contiene las peticiones preconfiguradas para reproducir los 6 escenarios de la rúbrica de evaluación.
