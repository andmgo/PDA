import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import 'sesion.dart';

/// Dirección del API (gescazone-api). Se puede cambiar al compilar:
///   flutter run --dart-define=API_URL=https://api.aurumcondominios.co
/// Por defecto: el emulador de Android ve el PC como 10.0.2.2; la versión web
/// local y el simulador de iOS lo ven como localhost.
String get urlApi {
  const definida = String.fromEnvironment('API_URL');
  if (definida.isNotEmpty) return definida;
  if (kIsWeb || defaultTargetPlatform == TargetPlatform.iOS) return 'http://localhost:8080';
  return 'http://10.0.2.2:8080';
}

/// Error del API con el mensaje que ya viene redactado para mostrar
/// (el API responde texto plano en sus errores).
class ApiError implements Exception {
  final int estado;
  final String mensaje;
  const ApiError(this.estado, this.mensaje);

  @override
  String toString() => mensaje;
}

/// La sesión ya no sirve: token vencido o cuenta desactivada por un
/// administrador. La app vuelve al inicio de sesión.
class SesionTerminada implements Exception {
  final bool cuentaDesactivada;
  const SesionTerminada({this.cuentaDesactivada = false});
}

class Api {
  final http.Client _cliente;
  Sesion? sesion;

  /// Se llama cuando el API rechaza la sesión, para que la app cierre sesión.
  void Function(SesionTerminada)? alTerminarSesion;

  Api({http.Client? cliente}) : _cliente = cliente ?? http.Client();

  static const _espera = Duration(seconds: 15);

  Future<Sesion> iniciarSesion(String documentoOCorreo, String contrasena) async {
    final http.Response r;
    try {
      r = await _cliente
          .post(Uri.parse('$urlApi/api/auth/login'),
              headers: {'Content-Type': 'application/json'},
              body: jsonEncode({'numeroDocumento': documentoOCorreo.trim(), 'contrasena': contrasena}))
          .timeout(_espera);
    } on TimeoutException {
      throw const ApiError(0, 'El servidor no respondió. Revisa tu conexión e inténtalo de nuevo.');
    } catch (_) {
      throw const ApiError(0, 'No se pudo conectar con Aurum. Revisa tu conexión.');
    }
    if (r.statusCode == 200) {
      return Sesion.desdeJson(jsonDecode(utf8.decode(r.bodyBytes)) as Map<String, dynamic>);
    }
    if (r.statusCode == 401) throw const ApiError(401, 'Documento, correo o contraseña incorrectos.');
    throw ApiError(r.statusCode, _texto(r).isNotEmpty ? _texto(r) : 'No se pudo iniciar sesión.');
  }

  Future<dynamic> get(String ruta) => _enviar(() => _cliente.get(_uri(ruta), headers: _cabeceras()));

  Future<dynamic> post(String ruta, [Object? cuerpo]) =>
      _enviar(() => _cliente.post(_uri(ruta), headers: _cabeceras(json: true), body: jsonEncode(cuerpo ?? {})));

  Future<dynamic> put(String ruta, [Object? cuerpo]) =>
      _enviar(() => _cliente.put(_uri(ruta), headers: _cabeceras(json: true), body: jsonEncode(cuerpo ?? {})));

  Uri _uri(String ruta) => Uri.parse('$urlApi$ruta');

  Map<String, String> _cabeceras({bool json = false}) => {
        if (sesion != null) 'Authorization': 'Bearer ${sesion!.token}',
        if (json) 'Content-Type': 'application/json',
      };

  Future<dynamic> _enviar(Future<http.Response> Function() peticion) async {
    final http.Response r;
    try {
      r = await peticion().timeout(_espera);
    } on TimeoutException {
      throw const ApiError(0, 'El servidor no respondió. Desliza hacia abajo para reintentar.');
    } catch (_) {
      throw const ApiError(0, 'Sin conexión con Aurum.');
    }
    final texto = _texto(r);
    if (r.statusCode == 401 || (r.statusCode == 403 && sesion == null)) {
      final fin = SesionTerminada(cuentaDesactivada: texto.contains('CUENTA_DESACTIVADA'));
      alTerminarSesion?.call(fin);
      throw fin;
    }
    if (r.statusCode >= 400) {
      throw ApiError(r.statusCode, r.statusCode == 403
          ? 'Tu rol no tiene permiso para esta acción.'
          : (texto.isNotEmpty && texto.length < 300 ? texto : 'Ocurrió un error (${r.statusCode}).'));
    }
    if (texto.isEmpty) return null;
    try {
      return jsonDecode(texto);
    } catch (_) {
      return texto; // respuestas de texto plano ("Ingreso registrado…")
    }
  }

  static String _texto(http.Response r) => utf8.decode(r.bodyBytes).trim();
}
