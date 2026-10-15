import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Lo que devuelve POST /api/auth/login (mismo contrato que usa la web).
class Sesion {
  final String token;
  final String numeroDocumento;
  final String nombre;
  final String apellido;
  final String correo;
  final String rol; // ADMINISTRADOR | PROPIETARIO | FUNCIONARIO | (roles nuevos)
  final Map<String, ({bool ver, bool editar})> permisos;

  const Sesion({
    required this.token,
    required this.numeroDocumento,
    required this.nombre,
    required this.apellido,
    required this.correo,
    required this.rol,
    required this.permisos,
  });

  /// Las pantallas se muestran según los permisos dinámicos del rol (los
  /// mismos de la pantalla "Roles y permisos" de la web), no por nombre de rol.
  bool puedeVer(String codigo) => permisos[codigo]?.ver ?? false;
  bool puedeEditar(String codigo) => permisos[codigo]?.editar ?? false;

  String get rolLegible => rol.isEmpty ? '' : rol[0] + rol.substring(1).toLowerCase();

  factory Sesion.desdeJson(Map<String, dynamic> j) => Sesion(
        token: j['token'] as String,
        numeroDocumento: '${j['numeroDocumento']}',
        nombre: (j['nombre'] ?? '') as String,
        apellido: (j['apellido'] ?? '') as String,
        correo: (j['correo'] ?? '') as String,
        rol: ((j['nombreRol'] ?? '') as String).toUpperCase(),
        permisos: {
          for (final p in (j['permisos'] as List? ?? const []))
            '${p['codigo']}': (ver: p['puedeVer'] == true, editar: p['puedeEditar'] == true),
        },
      );

  Map<String, dynamic> aJson() => {
        'token': token,
        'numeroDocumento': numeroDocumento,
        'nombre': nombre,
        'apellido': apellido,
        'correo': correo,
        'nombreRol': rol,
        'permisos': [
          for (final e in permisos.entries)
            {'codigo': e.key, 'puedeVer': e.value.ver, 'puedeEditar': e.value.editar},
        ],
      };
}

/// El token se guarda cifrado (Keychain en iOS, Keystore en Android),
/// nunca en preferencias planas.
class AlmacenSesion {
  static const _clave = 'aurum_sesion';
  final FlutterSecureStorage _almacen;

  AlmacenSesion([FlutterSecureStorage? almacen]) : _almacen = almacen ?? const FlutterSecureStorage();

  Future<Sesion?> leer() async {
    try {
      final texto = await _almacen.read(key: _clave);
      if (texto == null) return null;
      return Sesion.desdeJson(jsonDecode(texto) as Map<String, dynamic>);
    } catch (_) {
      return null; // dato corrupto o almacenamiento no disponible → volver a iniciar sesión
    }
  }

  Future<void> guardar(Sesion sesion) => _almacen.write(key: _clave, value: jsonEncode(sesion.aJson()));

  Future<void> borrar() => _almacen.delete(key: _clave);
}
