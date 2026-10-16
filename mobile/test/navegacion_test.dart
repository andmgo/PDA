import 'dart:convert';

import 'package:aurum_condominios/datos/api.dart';
import 'package:aurum_condominios/datos/fechas.dart';
import 'package:aurum_condominios/datos/sesion.dart';
import 'package:aurum_condominios/pantallas/perfil.dart';
import 'package:aurum_condominios/pantallas/principal.dart';
import 'package:aurum_condominios/pantallas/reservas.dart';
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

Sesion sesion(String rol, List<String> permisos) => Sesion.desdeJson({
      'token': 't',
      'numeroDocumento': '300000001',
      'nombre': 'Elena',
      'apellido': 'Vásquez',
      'correo': 'elena@correo.com',
      'nombreRol': rol,
      'permisos': [
        for (final p in permisos) {'codigo': p, 'puedeVer': true, 'puedeEditar': true},
      ],
    });

final manana = DateTime.now().add(const Duration(days: 1));
final salones = [
  {'id': 's10', 'numero': 'S-10', 'activo': true},
  {'id': 's11', 'numero': 'S-11', 'activo': true},
  {'id': 'sx', 'numero': 'S-VIEJO', 'activo': false},
];

/// Pantalla de celular alta: las listas no liberan los campos que quedan fuera de vista.
void celularAlto(WidgetTester tester) {
  tester.view.physicalSize = const Size(412 * 3, 1400 * 3);
  tester.view.devicePixelRatio = 3;
  addTearDown(tester.view.reset);
}

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);

  group('pestañas según el rol', () {
    Future<List<String>> pestanas(WidgetTester tester, Sesion s) async {
      final api = Api(cliente: MockClient((r) async => json([])))..sesion = s;
      await tester.pumpWidget(envolver(PantallaPrincipal(api: api, sesion: s, alCerrarSesion: () {})));
      await tester.pumpAndSettle();
      return [
        for (final d in tester.widgetList<NavigationDestination>(find.byType(NavigationDestination))) d.label,
      ];
    }

    testWidgets('propietario', (tester) async {
      expect(await pestanas(tester, sesion('PROPIETARIO', ['VISITAS', 'MIS_PAQUETES', 'RESERVAS'])),
          ['Inicio', 'Visitas', 'Reservas', 'Perfil']);
    });

    testWidgets('funcionario', (tester) async {
      expect(await pestanas(tester, sesion('FUNCIONARIO', ['CONTROL_ACCESOS', 'PAQUETES', 'RESERVAS'])),
          ['Inicio', 'Portería', 'Reservas', 'Perfil']);
    });

    testWidgets('administrador', (tester) async {
      expect(
          await pestanas(
              tester, sesion('ADMINISTRADOR', ['CONTROL_ACCESOS', 'PAQUETES', 'VISITAS', 'MIS_PAQUETES', 'RESERVAS'])),
          ['Inicio', 'Portería', 'Visitas', 'Reservas', 'Perfil']);
    });
  });

  testWidgets('la tarjeta Reservas del inicio cambia de pestaña (no abre otra copia)', (tester) async {
    celularAlto(tester);
    final s = sesion('PROPIETARIO', ['RESERVAS']);
    final api = Api(cliente: MockClient((r) async => switch (r.url.path) {
          '/api/salones/todos' => json(salones),
          _ => json([]),
        }))
      ..sesion = s;
    await tester.pumpWidget(envolver(PantallaPrincipal(api: api, sesion: s, alCerrarSesion: () {})));
    await tester.pumpAndSettle();
    final tarjeta = find.widgetWithText(InkWell, 'Salón social');
    await tester.ensureVisible(tarjeta);
    await tester.pumpAndSettle();
    await tester.tap(tarjeta);
    await tester.pumpAndSettle();

    final barra = tester.widget<NavigationBar>(find.byType(NavigationBar));
    expect(barra.selectedIndex, 1); // Inicio, Reservas, Perfil
    expect(find.text('Nueva reserva'), findsOneWidget);
    expect(find.byType(BackButton), findsNothing);
  });

  group('reservas', () {
    testWidgets('solo salones activos; reservar envía salón y fecha ISO; cancelar la propia', (tester) async {
      Map<String, dynamic>? creada;
      celularAlto(tester);
      final pedidas = <String>[];
      final api = Api(cliente: MockClient((r) async {
        pedidas.add('${r.method} ${r.url.path}');
        switch ('${r.method} ${r.url.path}') {
          case 'GET /api/salones/todos':
            return json(salones);
          case 'GET /api/reservas/mias':
            return json([
              {'id': 'r1', 'numeroSalon': 'S-10', 'fechaYHora': '${fechaIso(manana)}T18:00:00', 'pasada': false},
              {'id': 'r0', 'numeroSalon': 'S-11', 'fechaYHora': '2026-01-10T18:00:00', 'pasada': true},
            ]);
          case 'GET /api/reservas/salon/s10/ocupados':
          case 'GET /api/reservas/salon/s11/ocupados':
            return json([fechaIso(manana)]);
          case 'POST /api/reservas/crear-propia':
            creada = jsonDecode(r.body) as Map<String, dynamic>;
            return texto('Reserva creada exitosamente', 201);
          case 'DELETE /api/reservas/eliminar/r1':
            return texto('Reserva cancelada');
        }
        return json([]);
      }));
      await tester.pumpWidget(envolver(PantallaReservas(api: api)));
      await tester.pumpAndSettle();

      expect(find.text('S-VIEJO'), findsNothing); // desactivado
      expect(find.text('PASADA'), findsOneWidget);
      expect(pedidas, contains('GET /api/reservas/salon/s10/ocupados'));

      // Elegir día en el calendario: mañana está ocupado, se elige otro día
      await tester.tap(find.widgetWithText(ChoiceChip, 'S-11'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Elegir'));
      await tester.pumpAndSettle();
      await tester.tap(find.textContaining(RegExp('aceptar', caseSensitive: false)));
      await tester.pumpAndSettle();

      await tester.ensureVisible(find.text('Reservar'));
      await tester.tap(find.text('Reservar'));
      await tester.pumpAndSettle();
      expect(creada!['idSalon'], 's11');
      expect(creada!['fechaYHoraReserva'], matches(RegExp(r'^\d{4}-\d{2}-\d{2}T18:00:00$')));
      expect(creada!['fechaYHoraReserva'], isNot(startsWith(fechaIso(manana)))); // no el día ocupado
      expect(find.textContaining('Reserva creada exitosamente'), findsOneWidget);

      await tester.scrollUntilVisible(find.text('Cancelar').first, 200, scrollable: find.byType(Scrollable).first);
      await tester.tap(find.text('Cancelar').first);
      await tester.pumpAndSettle();
      await tester.tap(find.widgetWithText(FilledButton, 'Cancelar reserva'));
      await tester.pumpAndSettle();
      expect(pedidas, contains('DELETE /api/reservas/eliminar/r1'));
    });
  });

  group('perfil', () {
    testWidgets('cambiar contraseña valida y envía los tres campos', (tester) async {
      celularAlto(tester);
      Map<String, dynamic>? enviado;
      final api = Api(cliente: MockClient((r) async {
        if (r.method == 'POST') {
          enviado = jsonDecode(r.body) as Map<String, dynamic>;
          return texto('Contraseña actualizada correctamente');
        }
        return json([
          {'numeroApartamento': '401', 'vehiculos': [
            {'placa': 'ABC123', 'activo': true}
          ]}
        ]);
      }));
      var salio = false;
      await tester.pumpWidget(envolver(PantallaPerfil(
          api: api, sesion: sesion('PROPIETARIO', []), alCerrarSesion: () => salio = true)));
      await tester.pumpAndSettle();

      expect(find.text('Elena Vásquez'), findsOneWidget);
      expect(find.text('300000001'), findsOneWidget);
      expect(find.text('ABC123'), findsOneWidget);

      await tester.enterText(find.widgetWithText(TextFormField, 'Contraseña actual'), 'pro001');
      await tester.enterText(find.widgetWithText(TextFormField, 'Contraseña nueva'), 'corta');
      await tester.enterText(find.widgetWithText(TextFormField, 'Repite la contraseña nueva'), 'otra');
      await tester.ensureVisible(find.widgetWithText(FilledButton, 'Cambiar contraseña'));
      await tester.tap(find.widgetWithText(FilledButton, 'Cambiar contraseña'));
      await tester.pumpAndSettle();
      expect(find.text('Mínimo 6 caracteres'), findsWidgets);
      expect(find.text('No coincide con la nueva'), findsOneWidget);
      expect(enviado, isNull);

      await tester.enterText(find.widgetWithText(TextFormField, 'Contraseña nueva'), 'nueva123');
      await tester.enterText(find.widgetWithText(TextFormField, 'Repite la contraseña nueva'), 'nueva123');
      await tester.tap(find.widgetWithText(FilledButton, 'Cambiar contraseña'));
      await tester.pumpAndSettle();
      expect(enviado, {'contrasenaActual': 'pro001', 'contrasenaNueva': 'nueva123', 'confirmarContrasena': 'nueva123'});
      expect(find.text('Contraseña actualizada correctamente'), findsOneWidget);

      await tester.scrollUntilVisible(find.widgetWithText(OutlinedButton, 'Cerrar sesión'), 200,
          scrollable: find.byType(Scrollable).first);
      await tester.tap(find.widgetWithText(OutlinedButton, 'Cerrar sesión'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Salir'));
      await tester.pumpAndSettle();
      expect(salio, isTrue);
    });
  });

  for (final ancho in [320.0, 360.0, 412.0]) {
    testWidgets('fase 9 sin desbordes en ${ancho.toInt()} px', (tester) async {
      tester.view.physicalSize = Size(ancho * 3, 800 * 3);
      tester.view.devicePixelRatio = 3;
      addTearDown(tester.view.reset);
      final s = sesion('ADMINISTRADOR', ['CONTROL_ACCESOS', 'PAQUETES', 'VISITAS', 'MIS_PAQUETES', 'RESERVAS']);
      final api = Api(cliente: MockClient((r) async => switch (r.url.path) {
            '/api/salones/todos' => json([
                for (var i = 1; i <= 8; i++) {'id': 's$i', 'numero': 'SALÓN-PRINCIPAL-$i', 'activo': true}
              ]),
            '/api/reservas/mias' => json([
                {'id': 'r1', 'numeroSalon': 'SALÓN-PRINCIPAL-1', 'fechaYHora': '${fechaIso(manana)}T18:00:00', 'pasada': false}
              ]),
            _ => json([]),
          }))
        ..sesion = s;
      await tester.pumpWidget(envolver(PantallaPrincipal(api: api, sesion: s, alCerrarSesion: () {})));
      await tester.pumpAndSettle();
      for (final pestana in ['Portería', 'Visitas', 'Reservas', 'Perfil', 'Inicio']) {
        await tester.tap(find.descendant(of: find.byType(NavigationBar), matching: find.text(pestana)));
        await tester.pumpAndSettle();
        expect(tester.takeException(), isNull, reason: pestana);
      }
    });
  }
}
