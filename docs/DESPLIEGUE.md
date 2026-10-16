# Despliegue: Render + Neon (gratis, sin vencimiento)

- **Base de datos PostgreSQL → [Neon](https://neon.com)**: plan gratis
  permanente (no es una prueba, sin tarjeta), 1 GB. Se suspende tras 5 min
  sin uso y despierta solo en la siguiente consulta.
- **API y web → [Render](https://render.com)** con `render.yaml` (un
  "Blueprint"): plan gratis que no vence; vuelve a publicar solo con cada
  push a `main`.

| Servicio | Qué es | URL esperada |
|---|---|---|
| Neon `neondb` | PostgreSQL (Flyway crea las tablas al arrancar la API) | la da Neon |
| `aurum-condominios-api` | API Spring Boot (`Dockerfile`) | `https://aurum-condominios-api.onrender.com` |
| `aurum-condominios-web` | Web Spring Boot + Thymeleaf (`Dockerfile.web`) | `https://aurum-condominios-web.onrender.com` |

## Primera vez

### A. Base de datos en Neon

1. Crear cuenta en neon.com (con GitHub o Google).
2. Crear un proyecto: nombre **aurum**, Postgres 16 o superior, región
   **AWS US West 2 (Oregon)** (la misma de Render, para que sea rápida).
3. En el proyecto, **Connect**: copiar la cadena de conexión
   (`postgresql://neondb_owner:…@ep-….neon.tech/neondb?sslmode=require`).
   No pegarla en ningún archivo del repositorio.

### B. API y web en Render

1. Crear cuenta en render.com con **GitHub** y autorizar el repositorio `PDA`.
2. **New → Blueprint** → elegir el repositorio y la rama `main`. Render lee
   `render.yaml` y muestra los dos servicios.
3. Render pide los valores marcados como secretos. Para la demostración:
   - `DATABASE_URL`: la cadena de conexión de Neon (paso A.3).
   - `WOMPI_PUBLIC_KEY`, `WOMPI_INTEGRITY_SECRET`, `WOMPI_EVENTS_SECRET`,
     `WOMPI_PRIVATE_KEY`: las llaves **de prueba** de Wompi (las mismas de
     `api/config/wompi.properties`). Se pueden dejar vacías: el pago en línea
     queda deshabilitado.
   - `MAIL_USERNAME`, `MAIL_PASSWORD`: cuenta de Gmail y su contraseña de
     aplicación (avisos por correo). Se pueden dejar vacías.
4. **Apply**. La primera publicación tarda ~10 minutos (compila con Maven).
5. Revisar las URL que Render asignó a cada servicio. Si la de la API **no** es
   `https://aurum-condominios-api.onrender.com` (el nombre ya estaba usado),
   cambiar en la web la variable `API_BASE_URL`, y en la API
   `APP_CORS_ALLOWED_ORIGINS` con la URL real de la web.

Con `SPRING_PROFILES_ACTIVE=dev` la API carga la demostración la primera vez:
usuarios de prueba (admin `123456780`/`admin123`, Elena `300000001`/`pro001`,
Andrea `200000001`/`fun001`…), 20 apartamentos con propietarios, salones,
parqueaderos, vehículos y la cartera de los dos últimos meses
(`api/src/main/resources/db/demo/conjunto-demo.sql`).

> Las contraseñas de prueba son públicas (están en el código). Para un
> conjunto real: `SPRING_PROFILES_ACTIVE=prod` con una base nueva y cambiar
> la contraseña del administrador.

## Después de publicar

- **Wompi**: en el panel del comercio, URL de eventos de prueba:
  `https://aurum-condominios-api.onrender.com/api/pagos/wompi/eventos`.
  Con https, Wompi devuelve a Aurum al terminar el pago.
- **App móvil**: compilar apuntando a la API publicada:
  `flutter build apk --dart-define=API_URL=https://aurum-condominios-api.onrender.com`
- **Google (opcional)**: si se usa el inicio con Google, registrar en Google
  Cloud Console el redirect `https://aurum-condominios-web.onrender.com/login/oauth2/code/google`
  y agregar `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` a la web.

## Plan gratis: qué esperar

- Cada servicio se **duerme tras 15 minutos sin uso**; la primera visita
  después tarda ~1 minuto en despertar (la web y luego la API).
- Render da **750 horas gratis al mes** por cuenta; como los servicios se
  duermen cuando nadie los usa, para una demostración alcanza. Si se agotan,
  se pausan hasta el mes siguiente.
- Neon gratis: 1 GB de datos y 100 horas de cómputo al mes (solo cuenta
  mientras hay consultas).
- 512 MB de memoria por servicio: los Dockerfile ya limitan la JVM.

## Cómo funciona la conexión a la base

Neon entrega `DATABASE_URL=postgresql://usuario:clave@host/db?sslmode=require`.
`api/docker-entrypoint.sh` la convierte al formato JDBC
(`jdbc:postgresql://host/db?sslmode=require`) y saca de ella el usuario y la
clave (`DB_USER` / `DB_PASSWORD`).
