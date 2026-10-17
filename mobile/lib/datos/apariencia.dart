import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Modo claro / oscuro elegido por la persona (o el del sistema). Se guarda
/// en el teléfono para la próxima apertura, igual que el botón luna/sol de
/// la web.
class Apariencia extends ValueNotifier<ThemeMode> {
  static const _clave = 'aurum_tema';
  final FlutterSecureStorage? _almacen;

  /// Sin almacén (pruebas) solo vive en memoria.
  Apariencia([this._almacen]) : super(ThemeMode.system);

  Future<void> cargar() async {
    try {
      value = switch (await _almacen?.read(key: _clave)) {
        'claro' => ThemeMode.light,
        'oscuro' => ThemeMode.dark,
        _ => ThemeMode.system,
      };
    } catch (_) {
      // almacenamiento no disponible: se queda el del sistema
    }
  }

  Future<void> elegir(ThemeMode modo) async {
    value = modo;
    try {
      await _almacen?.write(
        key: _clave,
        value: switch (modo) {
          ThemeMode.light => 'claro',
          ThemeMode.dark => 'oscuro',
          _ => 'sistema',
        },
      );
    } catch (_) {}
  }

  /// Botón rápido (luna/sol): pasa al modo contrario del que se ve ahora.
  Future<void> alternar(Brightness actual) => elegir(actual == Brightness.dark ? ThemeMode.light : ThemeMode.dark);
}

/// Da acceso a [Apariencia] desde cualquier pantalla: `AparienciaAurum.of(context)`.
class AparienciaAurum extends InheritedNotifier<Apariencia> {
  const AparienciaAurum({super.key, required Apariencia apariencia, required super.child})
    : super(notifier: apariencia);

  static Apariencia? of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<AparienciaAurum>()?.notifier;
}
