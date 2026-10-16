# Despliegue en Render

Aurum se publica en [Render](https://render.com) con el archivo `render.yaml`
(un "Blueprint"): Render crea la base de datos PostgreSQL, la API y la web, y
vuelve a publicar solo cada vez que se hace push a `main`.

| Servicio | Qué es | URL esperada |
|---|---|---|
| `aurum-db` | PostgreSQL (Flyway crea las tablas al arrancar la API) | interna |
| `aurum-condominios-api` | API Spring Boot (`Dockerfile`) | `https://aurum-condominios-api.onrender.com` |
| `aurum-condominios-web` | Web Spring Boot + Thymeleaf (`Dockerfile.web`) | `https://aurum-condominios-web.onrender.com` |

## Primera vez

1. Crear cuenta en render.com con **GitHub** y autorizar el repositorio `PDA`.
2. **New → Blueprint** → elegir el repositorio y la rama `main`. Render lee
   `render.yaml` y muestra los tres recursos.
3. Render pide los valores marcados como secretos. Para la demostración:
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
- La base de datos gratis **vence a los 30 días** (14 días de gracia para
  pasarla a un plan pagado). Para uso continuo: plan pagado de la base y
  `plan: starter` en los servicios.
- 512 MB de memoria por servicio: los Dockerfile ya limitan la JVM.

## Cómo funciona la conexión a la base

Render entrega `DATABASE_URL=postgresql://usuario:clave@host:puerto/db`. El
`Dockerfile` de la API la convierte al formato JDBC
(`jdbc:postgresql://host:puerto/db`) y el usuario y la clave van en
`DB_USER` / `DB_PASSWORD`.
