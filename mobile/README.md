# Aurum Condominios — app móvil (Flutter)

App para Android e iOS que consume el mismo API que la web (`../api`). Usa la
misma cuenta, los mismos permisos dinámicos (pantalla "Roles y permisos") y la
identidad visual del Manual de Marca (azul noche, oro, Montserrat).

## Qué hace hoy (Fase 5 — base)

- Inicio de sesión con documento o correo; el token se guarda cifrado
  (Keystore en Android, Keychain en iOS) y la sesión se recuerda.
- Inicio según los **permisos** del rol:
  - `CONTROL_ACCESOS` → quién está dentro del conjunto ahora.
  - `PAQUETES` → paquetes pendientes de entrega.
  - Todos → "Mi hogar": apartamentos y placas vinculados (Fase 2).
- Si un administrador desactiva la cuenta, la app cierra la sesión sola.
- Accesos marcados "Próximamente" según la fase en que llegan
  (visitas autorizadas: Fase 6, paquetes: Fase 7, lector de placas: Fase 8).

## Correrla

Con el API corriendo en el PC (`../api`, puerto 8080):

```bash
flutter pub get
flutter run                     # emulador Android: usa http://10.0.2.2:8080
flutter run -d chrome           # vista previa web
flutter run --dart-define=API_URL=http://192.168.1.50:8080   # celular real en la misma red
```

Para la vista previa web el API debe permitir ese origen:
`APP_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:<puerto>`.
La app nativa no necesita CORS.

HTTP sin cifrar solo se permite hacia el servidor de desarrollo
(`10.0.2.2`/`localhost`, ver `android/app/src/main/res/xml/network_security_config.xml`);
en producción el API va por HTTPS.

## Pendiente en este equipo para compilar Android

`flutter doctor` indica que al SDK de Android le falta:

1. **Android SDK Command-line Tools**: Android Studio → Settings → Languages & Frameworks →
   Android SDK → pestaña *SDK Tools* → marcar *Android SDK Command-line Tools (latest)* → Apply.
2. Aceptar licencias: `flutter doctor --android-licenses` (responder `y`).
3. Crear un emulador (Device Manager) o conectar un celular con *Depuración USB*.

Después: `flutter build apk --debug` genera `build/app/outputs/flutter-apk/app-debug.apk`.

## Estructura

```
lib/
  main.dart              arranque: restaura sesión → login o inicio
  tema/aurum_tema.dart   paleta y tipografía de marca (claro y oscuro)
  datos/api.dart         cliente HTTP (token, errores, sesión terminada)
  datos/sesion.dart      sesión + almacenamiento cifrado
  widgets/marca.dart     logo, encabezado azul noche, placas, tarjetas
  pantallas/             login, inicio
test/widget_test.dart    pruebas con cliente HTTP simulado
```

Ícono de la app: `flutter_launcher_icons.yaml` (regenerar con `dart run flutter_launcher_icons`).
