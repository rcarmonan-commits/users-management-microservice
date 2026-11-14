# CRUD de Usuarios y Control de Gastos (#6) — Arquitectura Hexagonal, DDD y CQRS en Spring Boot

Fork del repositorio ejemplo [`arrietajohn/users-management-microservice`](https://github.com/arrietajohn/users-management-microservice),
ampliado con el flujo **CRUDL completo de la entidad asignada `Gasto`** sobre la misma
arquitectura hexagonal del proyecto base.

---

## Datos de la actividad

| Campo | Detalle |
|---|---|
| **Universidad** | Universidad de Cartagena |
| **Programa** | Ingeniería de Sistemas |
| **Asignatura** | Desarrollo Web |
| **Unidad** | Unidad 2 |
| **Semestre** | 2026-I |
| **Actividad** | CRUD de Usuarios con Arquitectura Hexagonal, DDD y CQRS en Spring Boot |
| **Ejercicio asignado** | **N.º 6 — Entidad `Gasto`** |
| **Atributos de negocio** | `fecha`, `valorTotalSinIVA`, `ivaTotal`, `valorTotalConIVA`, `nombreUsuario`, `lugar`, `descripcion` |
| **Estudiante** | Rosary Eilyn Carmona Navarro |
| **Correo institucional** | `rcarmonan@unicartagena.edu.co` |
| **Repositorio (fork)** | https://github.com/rcarmonan-commits/users-management-microservice |
| **Despliegue** | https://users-gastos-spring-boot-6.onrender.com |
| **Swagger UI** | https://users-gastos-spring-boot-6.onrender.com/swagger-ui.html |
| **Base de datos** | MySQL en Alwaysdata |

---

## Tabla de contenidos

1. [Arquitectura](#1-arquitectura)
2. [El agregado Gasto y su invariante fiscal](#2-el-agregado-gasto-y-su-invariante-fiscal)
3. [Segregación CQRS](#3-segregación-cqrs)
4. [API REST](#4-api-rest)
5. [Reportes analíticos](#5-reportes-analíticos)
6. [Seguridad aplicada](#6-seguridad-aplicada)
7. [Ejecución local y Docker](#7-ejecución-local-y-docker)
8. [Pruebas automatizadas](#8-pruebas-automatizadas)
9. [Despliegue e integración continua](#9-despliegue-e-integración-continua)
10. [Base de datos compartida](#10-base-de-datos-compartida)

---

## 1. Arquitectura

La dependencia apunta **siempre hacia adentro**: la infraestructura conoce al dominio, nunca al
revés. El dominio no importa una sola clase de Spring, JDBC ni Jakarta.

```
┌─────────────────────────────────────────────────────────────────┐
│  ENTRYPOINTS (primarios)                                        │
│  GastoRestController · GastoReportRestController · advice       │
│  DTO request/response · mappers · OpenAPI                       │
└───────────────────────────┬─────────────────────────────────────┘
                            │ implementa / invoca
┌───────────────────────────▼─────────────────────────────────────┐
│  APLICACIÓN                                                     │
│  Puertos IN  → CreateGasto, UpdateGasto, DeleteGasto,           │
│                GetGastoById, GetAllGastos, GetGastoReports      │
│  Servicios   → orquestan: validan, persisten, invalidan caché,  │
│                publican eventos                                 │
│  CQRS        → Commands · Queries · Response DTO · mappers      │
│  Puertos OUT → persistencia, cache, cola de eventos, REST       │
└───────────────────────────┬─────────────────────────────────────┘
                            │ depende de abstracciones
┌───────────────────────────▼─────────────────────────────────────┐
│  DOMINIO  (núcleo puro, sin dependencias externas)              │
│  GastoModel · GastoIvaRate · Value Objects · eventos            │
│  excepciones de negocio                                         │
└─────────────────────────────────────────────────────────────────┘
                            ▲
┌───────────────────────────┴─────────────────────────────────────┐
│  ADAPTADORES (secundarios)                                      │
│  GastoRepositoryMySQL · GastoRepositoryInMemory                 │
│  InMemoryCacheAdapter · InMemoryDomainEventPublisher            │
│  TaxIndicatorRestAdapter                                        │
└─────────────────────────────────────────────────────────────────┘
```

Los cuatro tipos de adaptador que pide la guía —**caché, base de datos, colas y REST**— están
implementados y cada uno detrás de su propio puerto.

**Spring es el único composition root.** No hay contenedor de dependencias propio: el cableado se
resuelve por `@Component`, `@Service`, `@Repository` y `@Configuration`, con inyección por
constructor.

---

## 2. El agregado Gasto y su invariante fiscal

`GastoModel` es un **modelo rico**, no un contenedor de datos. Custodia la regla que da sentido al
negocio:

```
valorTotalSinIVA + ivaTotal ≡ valorTotalConIVA   (± 0.005)
```

La validación vive en el constructor privado del agregado, de modo que **no existe forma de tener
en memoria un gasto descuadrado**. Ni la capa de aplicación ni la de persistencia revalidan: si el
objeto existe, cuadra.

La tolerancia de medio centavo absorbe el redondeo que llega desde el cliente; por encima de ella
se lanza `InvalidGastoIvaConsistencyException`, que la API traduce a **HTTP 422**.

### Decisiones de diseño

| Decisión | Motivo |
|---|---|
| Importes en `BigDecimal`, escala 2, `HALF_UP` | `double` arrastra errores de representación binaria que acaban rompiendo la invariante |
| `GastoIvaRate` como enumeración | El dominio solo puede expresar tarifas legales: 19%, 5% y exenta |
| `GastoOwner` como Value Object propio | Documenta que `gastos.nombre_usuario` guarda el **identificador** de cuenta, no el nombre de presentación |
| `GastoId.notAssigned()` | Un gasto sin persistir carece de identidad; se dice explícitamente en vez de pasear ceros o nulos |
| Tres factorías (`create`, `createApplyingRate`, `reconstitute`) | Enviar una tarifa hace que el dominio liquide el IVA y los totales cuadren por construcción |

---

## 3. Segregación CQRS

**Lado Command** — `CreateGastoCommand`, `UpdateGastoCommand`, `DeleteGastoCommand`. Validados con
Jakarta Validation en la frontera; las reglas de negocio quedan en el dominio. Validar el formato
no es validar el negocio.

**Lado Query** — `GetGastoByIdQuery` y `GastoFilterQuery`, que alimenta tanto el listado como las
siete proyecciones analíticas.

Todas las lecturas masivas pasan por `GetAllGastosUseCase`, que se apoya en el `CachePort` con un
TTL de 60 segundos. Una pantalla que pida los siete reportes **golpea la base una sola vez**. Toda
escritura invalida la caché, así que el listado nunca queda obsoleto.

---

## 4. API REST

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/login` | Obtiene el JWT |
| `GET` | `/health` | Público. Estado y versión del servicio |
| `POST` | `/api/gastos` | Registra un gasto |
| `GET` | `/api/gastos` | Lista con 8 filtros opcionales y combinables |
| `GET` | `/api/gastos/{id}` | Consulta por identidad |
| `PUT` | `/api/gastos/{id}` | Modifica |
| `DELETE` | `/api/gastos/{id}` | Elimina |

### Ejemplo: el dominio liquida el IVA

```bash
curl -X POST https://users-gastos-spring-boot-6.onrender.com/api/gastos \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"fecha":"2026-05-10","valorTotalSinIVA":100000,
       "nombreUsuario":"admin","lugar":"Ferreteria Norte","tarifaIva":"GENERAL"}'
```

```json
{
  "id": 7, "fecha": "2026-05-10",
  "valorTotalSinIVA": 100000.00, "ivaTotal": 19000.00, "valorTotalConIVA": 119000.00,
  "nombreUsuario": "admin", "lugar": "Ferreteria Norte", "tarifaEfectiva": 19.00
}
```

### Ejemplo: totales incoherentes

Enviar base `100000`, IVA `19000` y total `500000` devuelve **422**, no 400: la petición es
sintácticamente válida pero semánticamente imposible.

### Códigos devueltos

`201` creado · `204` eliminado · `400` datos malformados · `401` sin token ·
`403` rol insuficiente · `404` no existe · `422` invariante fiscal violada · `429` demasiados
intentos de login

Colección Postman lista para importar en
[`postman/Ejercicio6_Gasto_SpringBoot.postman_collection.json`](postman/Ejercicio6_Gasto_SpringBoot.postman_collection.json):
19 peticiones que guardan el token automáticamente y verifican la invariante, el 422 y las
cabeceras de seguridad.

---

## 5. Reportes analíticos

Siete proyecciones de solo lectura. **Cada una recibe su propio juego de filtros**: afinar una no
altera a las demás.

| # | Ruta | Filtros propios | CSV |
|---|---|---|---|
| 1 | `/api/reports/iva` | `desde`, `hasta`, `tarifaIva` | sí |
| 2 | `/api/reports/lugares` | `lugar`, `desde`, `hasta` | sí |
| 3 | `/api/reports/usuarios` | `nombreUsuario`, `montoMinimo`, `montoMaximo` | sí |
| 4 | `/api/reports/mensual` | `desde`, `hasta` | sí |
| 5 | `/api/reports/top` | `limite`, `lugar` | — |
| 6 | `/api/reports/detalle` | los 6 criterios combinados | sí |
| 7 | `/api/reports/roles` | — | — |

Añadiendo `?formato=csv` se descarga la proyección.

---

## 6. Seguridad aplicada

| Amenaza | Mitigación |
|---|---|
| **A01** Control de acceso roto | RBAC por método y ruta: escribir gastos exige ADMIN o MEMBER; borrar, solo ADMIN; los reportes, ADMIN o REVIEWER |
| **A02** Fallos criptográficos | JWT HS256 con clave de 256 bits mínimo; contraseñas con BCrypt |
| **A03** Inyección | Todo el acceso a datos usa `PreparedStatement` parametrizado: no se concatena entrada de usuario en SQL |
| **A05** Mala configuración | CSP restrictiva, `X-Frame-Options: DENY`, `nosniff`, `Referrer-Policy`, `Permissions-Policy` y HSTS |
| **A07** Fallos de identificación | Límite de 5 intentos fallidos por origen en ventana de 15 minutos → **429** |
| **A10** SSRF | El cliente REST solo admite `https` absoluto: rechaza `file://`, `http://` y direcciones de metadatos |
| **CSV Injection** | Las celdas que empiezan por `=`, `+`, `-`, `@`, tabulador o retorno se neutralizan con apóstrofo; BOM UTF-8 |

Sesiones **stateless**: no hay estado de sesión que secuestrar.

---

## 7. Ejecución local y Docker

```bash
export JAVA_HOME=/ruta/al/jdk-21-o-superior

./mvnw clean test          # suite completa
./mvnw clean package       # genera el jar ejecutable
java -jar target/users-management-2.2.0.jar
```

En Windows: `mvnw.cmd`.

La configuración local se toma de un archivo `.env` en la raíz —**ignorado por Git**— con
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_MODE`, los valores `SMTP_*`
y `JWT_SECRET`.

> `JWT_SECRET` debe ser **Base64 válido que decodifique a 32 bytes o más**. El servicio hace
> `Base64.getDecoder().decode(secret)` y HS256 exige 256 bits; un valor más corto impide el
> arranque.

```bash
docker compose up --build
```

---

## 8. Pruebas automatizadas

```
./mvnw clean test
Tests run: 232, Failures: 0, Errors: 0, Skipped: 0
```

**173 clases de producción y 41 de prueba.** La suite se ejecuta **sin base de datos**: los
adaptadores InMemory y los dobles de prueba cubren dominio, aplicación y entrypoints. Por eso el
runner de integración continua no necesita acceso a Alwaysdata.

Qué se verifica, más allá del camino feliz:

- La invariante fiscal rechaza totales incoherentes y tolera medio centavo de redondeo
- Los filtros acotan de forma **independiente** por fecha, usuario, lugar, tarifa y monto
- La caché expira, calcula una sola vez y se invalida al escribir
- La cola de eventos respeta su tope de memoria
- El cliente REST rechaza `file://` y la IP de metadatos `169.254.169.254`
- El exportador CSV neutraliza `=cmd|'/c calc'!A1` y `=HYPERLINK(...)`
- El limitador bloquea al sexto fallo, aísla por origen y respeta `X-Forwarded-For`
- La API responde 400, 404 y 422 en los casos correspondientes

---

## 9. Despliegue e integración continua

**Render** con runtime Docker y redespliegue automático en cada commit a `main`. El blueprint está
en [`render.yaml`](render.yaml); las credenciales se introducen en el panel y **nunca** viajan en
el repositorio.

**GitHub Actions** en [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml): compila con
JDK 21, ejecuta las 232 pruebas, publica el informe, empaqueta el jar y valida la construcción de
la imagen Docker. Admite ejecución manual con `workflow_dispatch`.

> El plan gratuito de Render bloquea por firewall los puertos SMTP salientes 25, 465 y 587. Si la
> entrega de correo resulta obligatoria, hay que anteponer una pasarela HTTPS por el puerto 443.

---

## 10. Base de datos compartida

La instancia MySQL de Alwaysdata está **compartida con otros ejercicios del curso**. De ahí dos
reglas que el código respeta sin excepción:

1. **La aplicación nunca ejecuta DDL.** No hay `ddl-auto` —el proyecto excluye
   `DataSourceAutoConfiguration`— ni scripts destructivos. El adaptador se limita a leer y escribir
   filas.
2. **El script de migración es idempotente.** [`migration-gastos.sql`](src/main/resources/db/migration-gastos.sql)
   usa `CREATE TABLE IF NOT EXISTS` e `INSERT IGNORE`: puede ejecutarse las veces que haga falta
   sin alterar datos ajenos.

`gastos.nombre_usuario` es clave foránea contra `usuarios(id)`, de modo que todo gasto pertenece a
un usuario existente. Esa columna almacena el **identificador de cuenta**, no el nombre de
presentación: confundirlos produce consultas que nunca devuelven resultados.
