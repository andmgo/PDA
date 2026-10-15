import 'dart:convert';

import 'package:aurum_condominios/datos/api.dart';
import 'package:aurum_condominios/datos/fechas.dart';
import 'package:aurum_condominios/datos/sesion.dart';
import 'package:aurum_condominios/pantallas/inicio.dart';
import 'package:aurum_condominios/pantallas/paquetes.dart';
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

http.Response texto(String t, [int estado = 200]) => http.Response.bytes(utf8.encode(t), estado);

Widget envolver(Widget pantalla) => MaterialApp(
      theme: temaAurum(Brightness.light),
      locale: const Locale('es', 'CO'),
      supportedLocales: const [Locale('es', 'CO')],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
      home: pantalla,
    );

final haceDosHoras = DateTime.now().subtract(const Duration(hours: 2)).toIso8601String();

Map<String, dynamic> paquete({
  String id = 'p1',
  String? descripcion = 'Caja de Amazon',
  bool entregado = false,
  String? entregadoA,
  String receptor = 'Elena Vásquez',
}) =>
    {
      'id': id,
      'numeroApartamento': '401',
      'nombreReceptor': receptor,
      'cedulaReceptor': '300000001',
      'descripcion': descripcion,
      'fechaHoraLlegada': haceDosHoras,
      'entregado': entregado,
      'fechaHoraEntrega': entregado ? DateTime.now().toIso8601String() : null,
      'entregadoA': entregadoA,
      'recibidoPor': 'Andrea Lozano',
      'entregadoPor': entregado ? 'Andrea Lozano' : null,
    };

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);

  test('hace cuánto y fecha con hora', () {
    final ahora = DateTime(2026, 10, 15, 16, 0);
    expect(haceCuanto(DateTime(2026, 10, 15, 15, 55), ahora), 'hace 5 min');
    expect(haceCuanto(DateTime(2026, 10, 15, 13, 0), ahora), 'hace 3 h');
    expect(haceCuanto(DateTime(2026, 10, 13, 16, 0), ahora), 'hace 2 días');
    expect(fechaHoraCorta(DateTime(2026, 10, 15, 9, 5)), 'jue 15 oct · 09:05');
  });

  testWidgets('mis paquetes: resumen por recoger y separación de entregados', (tester) async {
    final api = Api(cliente: MockClient((r) async => json([
          paquete(),
          paquete(id: 'p2', descripcion: null, entregado: true, entregadoA: 'María (empleada)'),
        ])));
    await tester.pumpWidget(envolver(PantallaMisPaquetes(api: api)));
    await tester.pumpAndSettle();

    expect(find.textContaining('El más antiguo llegó hace 2 h'), findsOneWidget);
    expect(find.text('Caja de Amazon'), findsOneWidget);
    expect(find.text('EN PORTERÍA'), findsOneWidget);
    expect(find.textContaining('recibió Andrea Lozano'), findsOneWidget);
    expect(find.textContaining('María (empleada)'), findsNothing);

    await tester.tap(find.text('Entregados'));
    await tester.pumpAndSettle();
    expect(find.text('Paquete'), findsOneWidget); // sin descripción
    expect(find.textContaining('a María (empleada)'), findsOneWidget);
  });

  group('portería', () {
    testWidgets('registrar envía el número de apartamento y la descripción', (tester) async {
      Map<String, dynamic>? enviado;
      final api = Api(cliente: MockClient((r) async {
        if (r.method == 'POST') {
          enviado = jsonDecode(r.body) as Map<String, dynamic>;
          return texto('Paquete registrado exitosamente', 201);
        }
        return json([]);
      }));
      await tester.pumpWidget(envolver(PantallaPaquetes(api: api)));
      await tester.pumpAndSettle();

      await tester.enterText(find.widgetWithText(TextFormField, 'Apartamento'), '401');
      await tester.enterText(find.widgetWithText(TextFormField, 'Nombre de quien recibe'), 'Elena Vásquez');
      await tester.enterText(find.widgetWithText(TextFormField, 'Cédula de quien recibe'), '300000001');
      await tester.enterText(find.widgetWithText(TextFormField, 'Descripción (opcional)'), 'Sobre');
      await tester.ensureVisible(find.byType(FilledButton));
      await tester.pumpAndSettle();
      await tester.tap(find.byType(FilledButton));
      await tester.pumpAndSettle();

      expect(enviado, {
        'numeroApartamento': '401',
        'nombreReceptor': 'Elena Vásquez',
        'cedulaReceptor': '300000001',
        'descripcion': 'Sobre',
      });
      expect(find.text('Paquete registrado exitosamente → apartamento 401'), findsOneWidget);
    });

    testWidgets('entregar pregunta quién lo retira y lo envía', (tester) async {
      Map<String, dynamic>? entrega;
      String? ruta;
      final api = Api(cliente: MockClient((r) async {
        if (r.method == 'POST') {
          ruta = r.url.path;
          entrega = jsonDecode(r.body) as Map<String, dynamic>;
          return texto('Paquete entregado a María (empleada)');
        }
        return json([paquete(), paquete(id: 'p2', entregado: true)]);
      }));
      await tester.pumpWidget(envolver(PantallaPaquetes(api: api, pestanaInicial: 1)));
      await tester.pumpAndSettle();

      expect(find.text('Caja de Amazon'), findsOneWidget); // solo el pendiente
      await tester.tap(find.widgetWithText(FilledButton, 'Entregar'));
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField).last, 'María (empleada)');
      await tester.tap(find.widgetWithText(FilledButton, 'Entregar').last);
      await tester.pumpAndSettle();

      expect(ruta, '/api/paquetes/p1/entregar');
      expect(entrega, {'entregadoA': 'María (empleada)'});
      expect(find.text('Paquete entregado a María (empleada)'), findsOneWidget);
    });
  });

  for (final ancho in [320.0, 360.0, 412.0]) {
    testWidgets('paquetes sin desbordes en ${ancho.toInt()} px', (tester) async {
      tester.view.physicalSize = Size(ancho * 3, 800 * 3);
      tester.view.devicePixelRatio = 3;
      addTearDown(tester.view.reset);
      final largo = paquete(
          descripcion: 'Caja grande de electrodomésticos de Mercado Libre',
          receptor: 'María Fernanda Castañeda Villamizar');
      final api = Api(cliente: MockClient((r) async => json([
            largo,
            paquete(id: 'p2', entregado: true, entregadoA: 'Juan Sebastián Rodríguez (vecino del 1204)'),
          ])));

      await tester.pumpWidget(envolver(PantallaMisPaquetes(api: api)));
      await tester.pumpAndSettle();
      expect(tester.takeException(), isNull);
      for (final pestana in [1, 2, 0]) {
        await tester.pumpWidget(envolver(PantallaPaquetes(key: ValueKey(pestana), api: api, pestanaInicial: pestana)));
        await tester.pumpAndSettle();
        expect(tester.takeException(), isNull, reason: 'pestaña $pestana');
      }
    });
  }

  testWidgets('inicio del propietario: aviso de paquetes por recoger y tarjeta Mis paquetes', (tester) async {
    final sesion = Sesion.desdeJson({
      'token': 't',
      'numeroDocumento': '300000001',
      'nombre': 'Elena',
      'apellido': 'Vásquez',
      'correo': '',
      'nombreRol': 'PROPIETARIO',
      'permisos': [
        for (final p in ['VISITAS', 'MIS_PAQUETES']) {'codigo': p, 'puedeVer': true, 'puedeEditar': false},
      ],
    });
    final api = Api(cliente: MockClient((r) async => switch (r.url.path) {
          '/api/paquetes/mios' => json([paquete(), paquete(id: 'p2'), paquete(id: 'p3', entregado: true)]),
          _ => json([]),
        }))
      ..sesion = sesion;
    await tester.pumpWidget(envolver(PantallaInicio(api: api, sesion: sesion, alCerrarSesion: () {})));
    await tester.pumpAndSettle();

    expect(find.text('Tienes 2 paquetes por recoger en portería.'), findsOneWidget);
    await tester.tap(find.text('Tienes 2 paquetes por recoger en portería.'));
    await tester.pumpAndSettle();
    expect(find.text('Por recoger'), findsWidgets);
  });
}
