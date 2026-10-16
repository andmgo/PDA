import 'dart:convert';

import 'package:aurum_condominios/datos/api.dart';
import 'package:aurum_condominios/pantallas/lector_placas.dart';
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

final lecturas = <String, Map<String, dynamic>>{
  'ABC123': {
    'placa': 'ABC123',
    'resultado': 'RESIDENTE',
    'residente': {
      'numeroApartamento': '401',
      'vehiculo': {'tipo': 'CARRO', 'marca': 'Mazda', 'color': 'Gris'},
      'propietarios': [
        {'nombre': 'Elena', 'apellido': 'Vásquez'}
      ],
    },
  },
  'HJK45F': {
    'placa': 'HJK45F',
    'resultado': 'VISITANTE_DENTRO',
    'visitaDentro': {
      'id': 'r1',
      'tipo': 'VEHICULO',
      'nombreCompleto': 'Julián Ospina',
      'numeroApartamento': '401',
      'numeroParqueadero': 'P-98',
      'entrada': '2026-10-16 09:30:00',
    },
  },
  'XYZ987': {
    'placa': 'XYZ987',
    'resultado': 'AUTORIZADA',
    'autorizacion': {
      'id': 'aut-9',
      'numeroApartamento': '401',
      'nombreVisitante': 'Sofía',
      'apellidoVisitante': 'Pardo',
      'numeroDocumento': '33445566',
      'placa': 'XYZ987',
      'autorizadaPor': 'Elena Vásquez',
      'nota': 'Trae torta',
      'ingresos': 0,
    },
  },
  'QWE456': {'placa': 'QWE456', 'resultado': 'DESCONOCIDA'},
};

Api apiLector({List<String>? pedidas}) => Api(cliente: MockClient((r) async {
      pedidas?.add('${r.method} ${r.url.path}');
      final placa = r.url.pathSegments.last;
      if (r.url.path.startsWith('/api/accesos/visitas/placa/')) {
        return lecturas.containsKey(placa) ? json(lecturas[placa]) : json(lecturas['QWE456']!..['placa'] = placa);
      }
      if (r.method == 'PUT') {
        return http.Response.bytes(utf8.encode('Salida registrada: Julián Ospina'), 200);
      }
      return json([]);
    }));

Future<void> consultar(WidgetTester tester, String placa) async {
  await tester.enterText(find.byType(TextField), placa);
  await tester.tap(find.text('Consultar'));
  await tester.pumpAndSettle();
}

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);

  testWidgets('residente: muestra apartamento, vehículo y propietaria', (tester) async {
    final pedidas = <String>[];
    await tester.pumpWidget(envolver(PantallaLectorPlacas(api: apiLector(pedidas: pedidas), usarCamara: false)));
    await consultar(tester, 'abc123');

    expect(pedidas, ['GET /api/accesos/visitas/placa/ABC123']); // en mayúscula
    expect(find.text('Vehículo de residente'), findsOneWidget);
    expect(find.text('Carro · Mazda · Gris'), findsOneWidget);
    expect(find.text('Elena Vásquez'), findsOneWidget);
    expect(find.text('Registrar ingreso'), findsNothing);
  });

  testWidgets('visitante dentro: registrar salida', (tester) async {
    final pedidas = <String>[];
    await tester.pumpWidget(envolver(PantallaLectorPlacas(api: apiLector(pedidas: pedidas), usarCamara: false)));
    await consultar(tester, 'HJK45F');

    expect(find.textContaining('entró 09:30'), findsOneWidget);
    await tester.tap(find.text('Registrar salida'));
    await tester.pumpAndSettle();
    expect(pedidas.last, 'PUT /api/accesos/visitas/VEHICULO/r1/salida');
    expect(find.text('Salida registrada: Julián Ospina'), findsOneWidget);
    expect(find.text('Registrar salida'), findsNothing); // no se puede repetir
  });

  testWidgets('autorizada: registrar ingreso abre portería con la autorización cargada', (tester) async {
    await tester.pumpWidget(envolver(PantallaLectorPlacas(api: apiLector(), usarCamara: false)));
    await consultar(tester, 'XYZ987');

    expect(find.text('Visita autorizada por Elena Vásquez'), findsOneWidget);
    await tester.tap(find.text('Registrar ingreso'));
    await tester.pumpAndSettle();
    expect(find.textContaining('Visita autorizada · Sofía Pardo'), findsOneWidget);
    expect(find.text('33445566'), findsOneWidget);
  });

  testWidgets('desconocida: registrar ingreso abre portería en vehículo con la placa', (tester) async {
    await tester.pumpWidget(envolver(PantallaLectorPlacas(api: apiLector(), usarCamara: false)));
    await consultar(tester, 'QWE456');

    expect(find.text('Placa no registrada'), findsOneWidget);
    await tester.tap(find.text('Registrar ingreso'));
    await tester.pumpAndSettle();
    expect(find.text('QWE456'), findsOneWidget);
    expect(find.text('Placa'), findsOneWidget); // campo de placa visible = modo vehículo
  });

  testWidgets('leer otra placa vuelve a la búsqueda', (tester) async {
    await tester.pumpWidget(envolver(PantallaLectorPlacas(api: apiLector(), usarCamara: false)));
    await consultar(tester, 'ABC123');
    await tester.tap(find.text('Leer otra placa'));
    await tester.pumpAndSettle();
    expect(find.text('Consultar'), findsOneWidget);
  });

  for (final ancho in [320.0, 360.0, 412.0]) {
    testWidgets('lector sin desbordes en ${ancho.toInt()} px', (tester) async {
      tester.view.physicalSize = Size(ancho * 3, 700 * 3);
      tester.view.devicePixelRatio = 3;
      addTearDown(tester.view.reset);
      await tester.pumpWidget(envolver(PantallaLectorPlacas(api: apiLector(), usarCamara: false)));
      for (final placa in lecturas.keys) {
        await consultar(tester, placa);
        expect(tester.takeException(), isNull, reason: placa);
        await tester.tap(find.text('Leer otra placa'));
        await tester.pumpAndSettle();
      }
    });
  }
}
