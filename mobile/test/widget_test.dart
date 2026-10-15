import 'dart:convert';

import 'package:aurum_condominios/datos/api.dart';
import 'package:aurum_condominios/datos/sesion.dart';
import 'package:aurum_condominios/pantallas/inicio.dart';
import 'package:aurum_condominios/pantallas/login.dart';
import 'package:aurum_condominios/tema/aurum_tema.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

/// Respuesta de login tal como la devuelve POST /api/auth/login.
Map<String, dynamic> loginJson({required String rol, required List<String> permisosVer}) => {
      'token': 'jwt-de-prueba',
      'numeroDocumento': '300000001',
      'nombre': 'Elena',
      'apellido': 'Vásquez',
      'correo': 'elena@correo.com',
      'nombreRol': rol,
      'permisos': [
        for (final p in permisosVer) {'codigo': p, 'puedeVer': true, 'puedeEditar': false},
      ],
    };

http.Response json(Object cuerpo, [int estado = 200]) => http.Response.bytes(
    utf8.encode(jsonEncode(cuerpo)), estado, headers: {'content-type': 'application/json; charset=utf-8'});

Widget envolver(Widget pantalla) => MaterialApp(theme: temaAurum(Brightness.light), home: pantalla);

void main() {
  // Sin red en los tests: Montserrat cae a la fuente por defecto.
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);

  group('Sesion', () {
    test('lee los permisos dinámicos del rol', () {
      final s = Sesion.desdeJson(loginJson(rol: 'Funcionario', permisosVer: ['CONTROL_ACCESOS', 'PAQUETES']));
      expect(s.rol, 'FUNCIONARIO');
      expect(s.rolLegible, 'Funcionario');
      expect(s.puedeVer('CONTROL_ACCESOS'), isTrue);
      expect(s.puedeEditar('CONTROL_ACCESOS'), isFalse);
      expect(s.puedeVer('GESTION_DATOS'), isFalse);
    });

    test('se guarda y se recupera igual', () {
      final s = Sesion.desdeJson(loginJson(rol: 'PROPIETARIO', permisosVer: ['RESERVAS']));
      final copia = Sesion.desdeJson(s.aJson());
      expect(copia.token, s.token);
      expect(copia.puedeVer('RESERVAS'), isTrue);
    });
  });

  testWidgets('login: credenciales incorrectas muestran el mensaje y no inician sesión', (tester) async {
    var inicioSesion = false;
    final api = Api(cliente: MockClient((r) async => http.Response('Credenciales inválidas', 401)));
    await tester.pumpWidget(envolver(PantallaLogin(api: api, alIniciarSesion: (_) => inicioSesion = true)));

    await tester.enterText(find.byType(TextFormField).at(0), '300000001');
    await tester.enterText(find.byType(TextFormField).at(1), 'clave-mala');
    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();

    expect(find.text('Documento, correo o contraseña incorrectos.'), findsOneWidget);
    expect(inicioSesion, isFalse);
  });

  testWidgets('inicio de propietario: muestra Mi hogar con apartamento y placa, sin portería', (tester) async {
    final sesion = Sesion.desdeJson(loginJson(rol: 'PROPIETARIO', permisosVer: ['RESERVAS']));
    final pedidas = <String>[];
    final api = Api(cliente: MockClient((r) async {
      pedidas.add(r.url.path);
      expect(r.headers['Authorization'], 'Bearer jwt-de-prueba');
      return json([
        {
          'numeroApartamento': '401',
          'vehiculos': [
            {'placa': 'ABC123', 'activo': true},
            {'placa': 'OLD999', 'activo': false},
          ],
        }
      ]);
    }))
      ..sesion = sesion;

    await tester.pumpWidget(envolver(PantallaInicio(api: api, sesion: sesion, alCerrarSesion: () {})));
    await tester.pumpAndSettle();

    expect(find.text('Mi apartamento'), findsOneWidget);
    expect(find.text('401'), findsOneWidget);
    expect(find.text('ABC123'), findsOneWidget);
    expect(find.text('OLD999'), findsNothing); // los vehículos desactivados no se muestran
    expect(find.text('Dentro del conjunto'), findsNothing);
    expect(pedidas, ['/api/propiedad/mia']); // no pide datos de portería sin permiso
  });

  testWidgets('inicio de portería: muestra quién está dentro y paquetes pendientes', (tester) async {
    final sesion = Sesion.desdeJson(loginJson(rol: 'FUNCIONARIO', permisosVer: ['CONTROL_ACCESOS', 'PAQUETES']));
    final api = Api(cliente: MockClient((r) async {
      switch (r.url.path) {
        case '/api/accesos/visitas/dentro':
          return json([
            {
              'nombreCompleto': 'Pedro Salas',
              'numeroApartamento': '402',
              'placa': 'JKL456',
              'entrada': '2026-10-15 11:40:47',
            }
          ]);
        case '/api/paquetes/pendientes':
          return json([{}, {}]);
        default:
          return json([]);
      }
    }))
      ..sesion = sesion;

    await tester.pumpWidget(envolver(PantallaInicio(api: api, sesion: sesion, alCerrarSesion: () {})));
    await tester.pumpAndSettle();

    expect(find.text('Dentro del conjunto'), findsOneWidget);
    expect(find.text('Pedro Salas'), findsOneWidget);
    expect(find.text('JKL456'), findsOneWidget);
    expect(find.text('11:40'), findsOneWidget);
    expect(find.text('Hay 2 paquetes esperando entrega.'), findsOneWidget);
  });

  // Reportado en un Samsung A55: "right overflowed by 2.7 pixels" en las
  // tarjetas de Accesos. Flutter hace fallar el test si algo se desborda.
  for (final ancho in [320.0, 360.0, 412.0]) {
    testWidgets('inicio sin desbordes en pantalla de ${ancho.toInt()} px de ancho', (tester) async {
      tester.view.physicalSize = Size(ancho * 3, 800 * 3);
      tester.view.devicePixelRatio = 3;
      addTearDown(tester.view.reset);

      final sesion = Sesion.desdeJson(
          loginJson(rol: 'ADMINISTRADOR', permisosVer: ['CONTROL_ACCESOS', 'PAQUETES', 'RESERVAS']));
      final api = Api(cliente: MockClient((r) async => json([])))..sesion = sesion;

      await tester.pumpWidget(envolver(PantallaInicio(api: api, sesion: sesion, alCerrarSesion: () {})));
      await tester.pumpAndSettle();

      expect(tester.takeException(), isNull);
      expect(find.text('Lector de placas'), findsOneWidget);
    });
  }

  test('cuenta desactivada: el API avisa a la app para cerrar la sesión', () async {
    SesionTerminada? recibido;
    final api = Api(cliente: MockClient((r) async => http.Response('CUENTA_DESACTIVADA', 401)))
      ..sesion = Sesion.desdeJson(loginJson(rol: 'PROPIETARIO', permisosVer: []))
      ..alTerminarSesion = (fin) => recibido = fin;

    await expectLater(api.get('/api/propiedad/mia'), throwsA(isA<SesionTerminada>()));
    expect(recibido?.cuentaDesactivada, isTrue);
  });
}
