# Aurum Condominios — Roadmap del proyecto

Sistema de gestión administrativa y contable para el conjunto residencial
**"Terrazas de Calicanto"** (Cartagena, Colombia). Nació como proyecto
académico (Tecnología en Desarrollo de Software) y se está llevando más
allá de lo académico — la idea es que termine siendo un producto real.

Este documento existe para que cualquiera (incluida una sesión nueva de
Claude Code, en otra cuenta o en otra máquina) pueda retomar el proyecto
sin perder el hilo de qué se hizo, por qué, y qué falta.

## Arquitectura

Dos proyectos Maven independientes, uno por carpeta:

- **`api/`** (package `gescazone.demo`) — puerto **8080**. Domain/application/
  infrastructure completos (JPA + PostgreSQL + Flyway + JWT + permisos
  dinámicos). Solo expone REST (`presentation/controller/api/*`). Sin
  sesión HTTP, sin Thymeleaf, sin OAuth2 propio — completamente *stateless*.
- **`web/`** (package `gescazone.web`) — puerto **8081**. Solo vistas
  Thymeleaf + Spring Security con sesión (guarda el JWT que emite la Api).
  **Cero acceso a base de datos** — todo pasa por HTTP hacia `api/` vía
  clientes tipados (`AuthApiClient`, `UsuarioApiClient`) y un proxy genérico
  (`ApiProxyController`, reenvía `/api/**` con el JWT de sesión) para que el
  JavaScript de las plantillas llame a las mismas rutas relativas de siempre.

Ambos se ejecutan como dos procesos Spring Boot separados en desarrollo.

## Cómo correr el proyecto en local

Requisitos: JDK 17, PostgreSQL corriendo localmente (base `gescazone`).

```bash
# Terminal 1 — Api (puerto 8080)
cd api
./mvnw.cmd spring-boot:run

# Terminal 2 — Web (puerto 8081)
cd web
./mvnw.cmd spring-boot:run
```

Sin ninguna variable de entorno configurada, todo corre con valores por
defecto pensados **solo para desarrollo local** (contraseña de Postgres,
secreto JWT, clave interna api↔web, credenciales de Google OAuth) — ver
los comentarios en `api/src/main/resources/application.properties` y
`web/src/main/resources/application.properties`. Para producción hay que
fijar las variables de entorno reales (`DB_PASSWORD`, `JWT_SECRET`,
`INTERNAL_API_KEY`, `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`) — nunca
usar los valores de desarrollo fuera de una máquina local.

Usuarios semilla (`DataInitializer`, solo perfil `dev`):

| Rol          | Documento   | Contraseña |
|--------------|-------------|------------|
| Administrador| 123456780   | admin123   |
| Propietario  | 123456781   | prop123    |
| Funcionario  | 123456782   | func123    |

## Decisiones de stack ya tomadas

- **Base de datos:** PostgreSQL (no MySQL, aunque el enunciado académico
  original lo pedía — se prefirió por mejor soporte de reporting/agregación
  para informes financieros, y sobre SQL Server por costo/licenciamiento).
- **Mobile:** Flutter — se hace **al final**, después de que la Api esté
  estable, para no rehacer llamadas si el contrato cambia.
- **Despliegue:** Azure (App Service Linux + Azure Database for PostgreSQL
  Flexible Server). La app Flutter no se despliega en Azure — Play
  Store/App Store o APK directo. Aún no hay nada creado en Azure.
- **Pasarela de pagos (pendiente, ver Bloque J):** se decidió usar una
  pasarela real (no registro manual por el administrador) — recomendación
  dada: **Wompi** (colombiana, checkout hospedado sin tocar números de
  tarjeta, buen sandbox), pendiente de que el usuario cree la cuenta de
  comercio para poder integrarla de verdad.

## Fases completadas

- **Auditoría de código + contraste contra el documento de requerimientos.**
- **Bloque A — Revisión de seguridad:** credenciales a variables de entorno,
  logging con SLF4J, CORS centralizado, roles en un enum, `DataInitializer`
  detrás de `@Profile("dev")`.
- **Bloque B1 — Migración MongoDB → PostgreSQL:** Flyway, 15 entidades JPA,
  eliminación completa del código Mongo. De paso se corrigieron 4 bugs reales
  heredados de Mongo en los repositorios (`findBySalonNumero` y similares
  ignoraban su parámetro).
- **Bloque B2 — Roles y permisos dinámicos:** catálogo de permisos +
  matriz `rol_permiso` editable desde `/rolesYPermisos`, sin tocar código
  para dar de alta un rol nuevo. Adaptado del patrón de un proyecto de
  referencia en ASP.NET Core del propio usuario.
- **Bloque B3 — Suite de pruebas unitarias** (JUnit5 + Mockito). Encontró y
  corrigió un bug real: `JwtTokenProvider` no atrapaba el tipo correcto de
  excepción de firma inválida.
- **Bloque C — Separación en proyectos Api + Web:** ver "Arquitectura"
  arriba. Encontró y arregló un login roto de facto (`fetch('/api/login')`
  a un endpoint que nunca existió) y un `LazyInitializationException` real
  en el primer login end-to-end contra Postgres.
- **Bloque D — Reglas de reservas + solicitudes excepcionales + paquetes:**
  fechas bloqueadas (24/31 dic, 1 ene), reservas limitadas al año en curso
  con una cola de solicitud excepcional para otros años, y módulo nuevo de
  paquetes de recepción.
- **Bloque E — Alta masiva** de apartamentos/parqueaderos/salones con
  numeración consecutiva generada automáticamente.
- **Bloque F — Manejo de errores centralizado:** `@RestControllerAdvice`
  único en la Api, ~81 bloques `catch` duplicados eliminados de los
  controllers, excepciones propias (`NotFoundException`, `ValidationException`).
- **Bloque G (+ G.1 a G.5) — Rediseño visual completo:** paleta azul +
  Plus Jakarta Sans, sidebar unificado en un solo fragmento Thymeleaf,
  interruptor de tema claro/oscuro (persistido en el navegador), rediseño
  de Inicio (sin emoji, íconos de línea propios, accesos rápidos nuevos),
  se quitó la franja azul superior, y varias rondas de corrección de
  contraste en modo oscuro (inputs, botones, calendario de disponibilidad).
- **Bloque H — Registro con aprobación de administrador:** el autoregistro
  ya no crea la cuenta directo — queda como solicitud pendiente hasta que
  un administrador la revisa y asigna el rol. El login recuerda el número
  de documento (nunca la contraseña) en el navegador.
- **Bloque I (+ I.1, I.2) — Iniciar sesión / crear cuenta con Google:**
  login y registro con Google, con aprobación de administrador igual que el
  registro normal (Google solo reemplaza la verificación de identidad, no
  el control de acceso). De paso se quitó un desvío innecesario por Google
  que existía al reservar un salón social (usaba un formulario con
  cualquier documento escrito a mano en vez de la sesión ya iniciada — un
  IDOR real), y se arregló el menú móvil (el botón ☰ nunca funcionó porque
  el archivo JS del que dependía nunca existió en el proyecto).

## Pendiente / próximos pasos

1. **Bloque J — Pagos y Cartera real.** Hoy "Pagos y Cartera" es una
   maqueta 100% estática con datos de ejemplo — no existe `Pago`/`Factura`/
   `Transaccion`/`MetodoPago` en el backend. Falta: (a) pago de la cuota
   mensual de administración, (b) pago de reservas de salón social, ambos
   con una pasarela real (Wompi recomendado). Requiere que el usuario cree
   primero una cuenta de comercio/sandbox.
2. **Credenciales reales de Google OAuth2.** El login/registro con Google
   (Bloque I) está completo en código pero corre con `client-id`/`client-
   secret` de placeholder — hace falta crear un proyecto OAuth2 real en
   Google Cloud Console (`GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`) para
   poder probarlo de punta a punta.
3. **Confirmación visual del modo oscuro y el responsive en móvil.** Varias
   rondas de fixes (Bloque G.2 a G.5, I.2) se verificaron con `curl` y
   revisión de código, pero no con captura de pantalla real — conviene
   darle una pasada visual completa, sobre todo en las páginas que quedaron
   fuera del pulido de sidebar+Inicio (Pagos y Cartera, Perfil, Control de
   Accesos, Paquetes, Roles y Permisos).
4. **Flutter** (mobile) — después de que la Api esté estable.
5. **Despliegue en Azure** — nada creado todavía, hay que empezar desde cero
   cuando llegue esa fase (App Service + Azure Database for PostgreSQL).

## Convenciones útiles para quien retome esto

- Los tests viven en `src/test/java` de cada módulo (no hay un módulo de
  tests separado). Correr con `./mvnw.cmd test` dentro de `api/` (el
  módulo `web/` no tiene tests propios todavía).
- Verificar siempre con una prueba real (curl o navegador) además de los
  tests — varios bugs reales de este proyecto solo aparecieron al probar
  de punta a punta contra Postgres/Hibernate real, no con Mockito.
- El sidebar y el header ("topbar") de las 8 páginas con menú viven en
  fragmentos Thymeleaf únicos (`templates/fragments/sidebar.html` y
  `fragments/topbar.html`) — cualquier cambio de navegación va ahí, no
  repetido en cada plantilla.
- Los tokens de color/tema viven en `web/src/main/resources/static/css/
  gescazone-theme.css` (`:root` para modo claro, `:root.dark` para oscuro).
