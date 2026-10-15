import 'dart:convert';

import 'package:aurum_condominios/datos/api.dart';
import 'package:aurum_condominios/datos/fechas.dart';
import 'package:aurum_condominios/datos/sesion.dart';
import 'package:aurum_condominios/pantallas/inicio.dart';
import 'package:aurum_condominios/pantallas/mis_visitas.dart';
import 'package:aurum_condominios/pantallas/porteria.dart';
import 'package:aurum_condominios/tema/aurum_tema.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

http.Response json(Object? cuerpo, [int estado = 200]) => http.Response.bytes(
    utf8.encode(cuerpo is String ? cuerpo : jsonEncode(cuerpo)), estado,
    headers: {'content-type': 'application/json; charset=utf-8'});

Widget envolver(Widget pantalla) => MaterialApp(
      theme: temaAurum(Brightness.light),
      locale: const Locale('es', 'CO'),
      supportedLocales: const [Locale('es', 'CO')],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
      home: pantalla,
    );

final hoy = fechaIso(DateTime.now());

Map<String, dynamic> autorizacion({
  String id = 'aut-1',
  String nombre = 'Carla',
  String apellido = 'Mejía',
  String? documento = '5551234',
  String? placa,
  String estado = 'VIGENTE',
  int ingresos = 0,
  String? nota = 'Viene a almorzar',
}) =>
    {
      'id': id,
      'numeroApartamento': '401',
      'nombreVisitante': nombre,
      'apellidoVisitante': apellido,
      'numeroDocumento': documento,
      'placa': placa,
      'fechaDesde': hoy,
      'fechaHasta': hoy,
      'nota': nota,
      'estado': estado,
      'ingresos': ingresos,
      'autorizadaPor': 'Elena Vásquez',
    };

void tamano(WidgetTester tester, double ancho) {
  tester.view.physicalSize = Size(ancho * 3, 800 * 3);
  tester.view.devicePixelRatio = 3;
  addTearDown(tester.view.reset);
}

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);

  test('fechas en español y en formato del API', () {
    final d = DateTime(2026, 10, 15);
    expect(fechaIso(d), '2026-10-15');
    expect(fechaCorta(d), 'jue 15 oct');
    expect(rangoFechas(d, DateTime(2026, 10, 16)), 'jue 15 oct → vie 16 oct');
    expect(rangoFechas(d, d), 'jue 15 oct');
    expect(horaDe('2026-10-15 11:40:47'), '11:40');
  });

  group('Mis visitas (propietario)', () {
    Api apiPropietario(List<Map<String, dynamic>> autorizaciones, {List<String>? pedidas}) => Api(
          cliente: MockClient((r) async {
            pedidas?.add('${r.method} ${r.url.path}');
            switch (r.url.path) {
              case '/api/visitas-autorizadas/mias':
                return json(autorizaciones);
              case '/api/propiedad/mia':
                return json([
                  {'idApartamento': 'apto-401', 'numeroApartamento': '401', 'vehiculos': []}
                ]);
              case '/api/visitas-autorizadas/aut-1/cancelar':
                return http.Response.bytes(utf8.encode('Autorización de Carla Mejía cancelada'), 200);
            }
            return json([]);
          }),
        );

    testWidgets('separa próximas e historial y permite cancelar las próximas', (tester) async {
      final pedidas = <String>[];
      final api = apiPropietario([
        autorizacion(ingresos: 1),
        autorizacion(id: 'aut-2', nombre: 'Tomás', apellido: 'Ruiz', documento: null, estado: 'CANCELADA', nota: null),
      ], pedidas: pedidas);
      await tester.pumpWidget(envolver(PantallaMisVisitas(api: api)));
      await tester.pumpAndSettle();

      expect(find.text('Carla Mejía'), findsOneWidget);
      expect(find.text('HOY'), findsOneWidget);
      expect(find.textContaining('1 ingreso'), findsOneWidget);
      expect(find.text('Tomás Ruiz'), findsNothing);

      await tester.tap(find.text('Historial'));
      await tester.pumpAndSettle();
      expect(find.text('Tomás Ruiz'), findsOneWidget);
      expect(find.text('CANCELADA'), findsOneWidget);
      expect(find.text('Cancelar'), findsNothing); // las pasadas no se cancelan

      await tester.tap(find.text('Próximas'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Cancelar'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Cancelar visita'));
      await tester.pumpAndSettle();
      expect(pedidas, contains('PUT /api/visitas-autorizadas/aut-1/cancelar'));
    });

    testWidgets('autorizar envía apartamento, datos normalizados y fechas AAAA-MM-DD', (tester) async {
      Map<String, dynamic>? enviado;
      final api = Api(cliente: MockClient((r) async {
        if (r.method == 'POST') {
          enviado = jsonDecode(r.body) as Map<String, dynamic>;
          return json(autorizacion(nombre: 'Julián', apellido: 'Ospina'), 201);
        }
        return json([]);
      }));
      await tester.pumpWidget(envolver(PantallaNuevaVisita(api: api, apartamentos: const [
        {'idApartamento': 'apto-401', 'numeroApartamento': '401'}
      ])));

      await tester.scrollUntilVisible(find.byType(FilledButton), 200, scrollable: find.byType(Scrollable).first);
      await tester.tap(find.byType(FilledButton));
      await tester.pumpAndSettle();
      expect(find.text('Obligatorio'), findsNWidgets(2)); // nombre y apellido
      expect(enviado, isNull);

      await tester.enterText(find.widgetWithText(TextFormField, 'Nombre'), 'Julián');
      await tester.enterText(find.widgetWithText(TextFormField, 'Apellido'), 'Ospina');
      await tester.enterText(find.widgetWithText(TextFormField, 'Placa (si viene en carro)'), 'hjk45f');
      await tester.scrollUntilVisible(find.byType(FilledButton), 200, scrollable: find.byType(Scrollable).first);
      await tester.tap(find.byType(FilledButton));
      await tester.pumpAndSettle();

      expect(enviado!['idApartamento'], 'apto-401');
      expect(enviado!['nombreVisitante'], 'Julián');
      expect(enviado!['placa'], 'HJK45F');
      expect(enviado!['fechaDesde'], hoy);
      expect(enviado!['fechaHasta'], hoy);
    });

    testWidgets('el error del API se muestra en el formulario', (tester) async {
      final api = Api(cliente: MockClient((r) async =>
          http.Response.bytes(utf8.encode('Solo puedes autorizar visitas a tus apartamentos'), 400)));
      await tester.pumpWidget(envolver(PantallaNuevaVisita(api: api, apartamentos: const [
        {'idApartamento': 'apto-401', 'numeroApartamento': '401'}
      ])));
      await tester.enterText(find.widgetWithText(TextFormField, 'Nombre'), 'Ana');
      await tester.enterText(find.widgetWithText(TextFormField, 'Apellido'), 'Gil');
      await tester.scrollUntilVisible(find.byType(FilledButton), 200, scrollable: find.byType(Scrollable).first);
      await tester.tap(find.byType(FilledButton));
      await tester.pumpAndSettle();
      expect(find.text('Solo puedes autorizar visitas a tus apartamentos'), findsOneWidget);
    });
  });

  group('Portería', () {
    Api apiPorteria({void Function(Map<String, dynamic>)? alIngresar}) => Api(cliente: MockClient((r) async {
          switch (r.url.path) {
            case '/api/accesos/visitas/esperadas':
              return json([
                autorizacion(ingresos: 1, nota: null),
                autorizacion(id: 'aut-2', nombre: 'Julián', apellido: 'Ospina', documento: '1047888999', placa: 'HJK45F'),
              ]);
            case '/api/accesos/visitas/dentro':
              return json([
                {
                  'id': 'r1',
                  'tipo': 'PEATON',
                  'nombreCompleto': 'Carla Mejía',
                  'numeroApartamento': '401',
                  'entrada': '$hoy 15:46:27',
                  'autorizadaPor': 'Elena Vásquez',
                }
              ]);
            case '/api/accesos/visitas/parqueaderos-libres':
              return json(['V-01', 'V-02']);
            case '/api/accesos/visitas/autorizada/1047888999':
              return json(autorizacion(id: 'aut-2', nombre: 'Julián', apellido: 'Ospina', documento: '1047888999'));
            case '/api/accesos/visitas/ingreso':
              alIngresar?.call(jsonDecode(r.body) as Map<String, dynamic>);
              return http.Response.bytes(
                  utf8.encode('Ingreso registrado: Julián Ospina (HJK45F) → apartamento 401, parqueadero V-01'), 201);
          }
          return http.Response('', 404);
        }));

    testWidgets('esperados: un toque llena el formulario y el ingreso lleva la autorización', (tester) async {
      Map<String, dynamic>? ingreso;
      await tester.pumpWidget(envolver(PantallaPorteria(api: apiPorteria(alIngresar: (b) => ingreso = b))));
      await tester.pumpAndSettle();

      // Insignias: solo cuenta los que no han llegado
      final insignias = tester.widgetList<Badge>(find.byType(Badge)).toList();
      expect(insignias.where((b) => b.isLabelVisible).length, 2);
      expect(find.descendant(of: find.byType(Badge).at(0), matching: find.text('1')), findsOneWidget);

      await tester.tap(find.text('Esperados'));
      await tester.pumpAndSettle();
      expect(find.text('Julián Ospina'), findsOneWidget);
      expect(find.text('POR LLEGAR'), findsOneWidget);
      expect(find.text('YA INGRESÓ'), findsOneWidget);

      // El que no ha llegado aparece primero
      await tester.tap(find.text('Registrar ingreso').first);
      await tester.pumpAndSettle();

      expect(find.textContaining('Visita autorizada · Julián Ospina'), findsOneWidget);
      expect(find.text('1047888999'), findsOneWidget);
      expect(find.text('HJK45F'), findsOneWidget); // pasó a "En vehículo" con la placa

      await tester.ensureVisible(find.widgetWithText(FilledButton, 'Registrar ingreso'));
      await tester.pumpAndSettle();
      await tester.tap(find.widgetWithText(FilledButton, 'Registrar ingreso'));
      await tester.pumpAndSettle();
      expect(ingreso!['idAutorizacion'], 'aut-2');
      expect(ingreso!['conVehiculo'], isTrue);
      expect(ingreso!['numeroApartamento'], '401');
      expect(find.textContaining('Ingreso registrado: Julián Ospina'), findsOneWidget);
    });

    testWidgets('al escribir un documento autorizado avisa y completa los datos', (tester) async {
      await tester.pumpWidget(envolver(PantallaPorteria(api: apiPorteria())));
      await tester.pumpAndSettle();
      await tester.enterText(find.widgetWithText(TextFormField, 'Documento'), '1047888999');
      await tester.pump(const Duration(milliseconds: 500));
      await tester.pumpAndSettle();
      expect(find.textContaining('autorizó Elena Vásquez'), findsOneWidget);
      expect(find.widgetWithText(TextFormField, 'Julián'), findsOneWidget);
    });

    testWidgets('dentro: muestra quién autorizó', (tester) async {
      await tester.pumpWidget(envolver(PantallaPorteria(api: apiPorteria(), pestanaInicial: 2)));
      await tester.pumpAndSettle();
      expect(find.text('Carla Mejía'), findsOneWidget);
      expect(find.text('Autorizó Elena Vásquez'), findsOneWidget);
      expect(find.text('Salida'), findsOneWidget);
    });
  });

  // Mismo cuidado que en el inicio: nada debe desbordarse en celulares angostos.
  for (final ancho in [320.0, 360.0, 412.0]) {
    testWidgets('sin desbordes en ${ancho.toInt()} px: mis visitas, formulario y portería', (tester) async {
      tamano(tester, ancho);
      final larga = autorizacion(
          nombre: 'María Fernanda', apellido: 'Castañeda Villamizar', placa: 'ABC12D',
          nota: 'Viene con dos niños y trae una torta para la reunión del sábado', ingresos: 3);
      final api = Api(cliente: MockClient((r) async => switch (r.url.path) {
            '/api/visitas-autorizadas/mias' => json([larga]),
            '/api/propiedad/mia' => json([
                {'idApartamento': 'a', 'numeroApartamento': '401'}
              ]),
            '/api/accesos/visitas/esperadas' => json([larga, autorizacion()]),
            '/api/accesos/visitas/dentro' => json([
                {'id': 'r', 'tipo': 'VEHICULO', 'nombreCompleto': 'María Fernanda Castañeda Villamizar',
                  'numeroApartamento': '1204', 'placa': 'ABC12D', 'entrada': '$hoy 10:00:00',
                  'autorizadaPor': 'Elena Vásquez Restrepo'}
              ]),
            _ => json([]),
          }));

      await tester.pumpWidget(envolver(PantallaMisVisitas(api: api)));
      await tester.pumpAndSettle();
      expect(tester.takeException(), isNull);

      await tester.pumpWidget(envolver(PantallaNuevaVisita(api: api, apartamentos: const [
        {'idApartamento': 'a', 'numeroApartamento': '401'},
        {'idApartamento': 'b', 'numeroApartamento': '1204'},
      ])));
      await tester.pumpAndSettle();
      expect(tester.takeException(), isNull);

      for (final pestana in [1, 2, 0]) {
        await tester.pumpWidget(envolver(PantallaPorteria(key: ValueKey(pestana), api: api, pestanaInicial: pestana)));
        await tester.pumpAndSettle();
        expect(tester.takeException(), isNull, reason: 'pestaña $pestana');
      }
      await tester.tap(find.text('En vehículo'));
      await tester.pumpAndSettle();
      expect(tester.takeException(), isNull);
    });
  }

  testWidgets('inicio: Mis visitas y Registrar ingreso ya abren sus pantallas', (tester) async {
    final sesion = Sesion.desdeJson({
      'token': 't',
      'numeroDocumento': '1',
      'nombre': 'Andrea',
      'apellido': 'L',
      'correo': '',
      'nombreRol': 'ADMINISTRADOR',
      'permisos': [
        for (final p in ['CONTROL_ACCESOS', 'VISITAS']) {'codigo': p, 'puedeVer': true, 'puedeEditar': true},
      ],
    });
    final api = Api(cliente: MockClient((r) async => json([])))..sesion = sesion;
    await tester.pumpWidget(envolver(PantallaInicio(api: api, sesion: sesion, alCerrarSesion: () {})));
    await tester.pumpAndSettle();

    await tester.scrollUntilVisible(find.text('Mis visitas'), 200);
    await tester.tap(find.text('Mis visitas'));
    await tester.pumpAndSettle();
    expect(find.text('Próximas'), findsOneWidget);
    Navigator.of(tester.element(find.text('Próximas'))).pop();
    await tester.pumpAndSettle();

    await tester.tap(find.text('Control de accesos'));
    await tester.pumpAndSettle();
    expect(find.text('Ingreso'), findsOneWidget); // pestañas de portería
  });
}
