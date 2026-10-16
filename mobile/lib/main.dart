import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'datos/api.dart';
import 'datos/sesion.dart';
import 'pantallas/principal.dart';
import 'pantallas/login.dart';
import 'tema/aurum_tema.dart';
import 'widgets/marca.dart';

void main() {
  runApp(AurumApp(api: Api(), almacen: AlmacenSesion()));
}

class AurumApp extends StatefulWidget {
  final Api api;
  final AlmacenSesion almacen;

  const AurumApp({super.key, required this.api, required this.almacen});

  @override
  State<AurumApp> createState() => _AurumAppState();
}

class _AurumAppState extends State<AurumApp> {
  Sesion? _sesion;
  bool _restaurando = true;
  String? _avisoLogin;

  @override
  void initState() {
    super.initState();
    widget.api.alTerminarSesion = (fin) => _cerrarSesion(
          aviso: fin.cuentaDesactivada
              ? 'Tu cuenta fue desactivada. Contacta a la administración.'
              : 'Tu sesión terminó. Vuelve a iniciar sesión.',
        );
    _restaurar();
  }

  Future<void> _restaurar() async {
    final guardada = await widget.almacen.leer();
    widget.api.sesion = guardada;
    if (mounted) {
      setState(() {
        _sesion = guardada;
        _restaurando = false;
      });
    }
  }

  Future<void> _iniciarSesion(Sesion sesion) async {
    widget.api.sesion = sesion;
    await widget.almacen.guardar(sesion);
    setState(() {
      _sesion = sesion;
      _avisoLogin = null;
    });
  }

  /// Permisos refrescados desde el API: se guardan para la próxima apertura.
  Future<void> _actualizarSesion(Sesion sesion) async {
    widget.api.sesion = sesion;
    await widget.almacen.guardar(sesion);
    if (mounted) setState(() => _sesion = sesion);
  }

  Future<void> _cerrarSesion({String? aviso}) async {
    widget.api.sesion = null;
    await widget.almacen.borrar();
    if (mounted) {
      setState(() {
        _sesion = null;
        _avisoLogin = aviso;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final Widget pantalla;
    if (_restaurando) {
      pantalla = const _Arranque();
    } else if (_sesion == null) {
      pantalla = PantallaLogin(
          key: ValueKey(_avisoLogin), api: widget.api, alIniciarSesion: _iniciarSesion, aviso: _avisoLogin);
    } else {
      pantalla = PantallaPrincipal(
          key: ValueKey(_sesion!.token),
          api: widget.api,
          sesion: _sesion!,
          alCerrarSesion: _cerrarSesion,
          alActualizarSesion: _actualizarSesion);
    }

    return MaterialApp(
      title: 'Aurum Condominios',
      debugShowCheckedModeBanner: false,
      theme: temaAurum(Brightness.light),
      darkTheme: temaAurum(Brightness.dark),
      // Calendarios y textos del sistema en español, sin importar el idioma del teléfono.
      locale: const Locale('es', 'CO'),
      supportedLocales: const [Locale('es', 'CO'), Locale('es')],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
      home: AnimatedSwitcher(duration: const Duration(milliseconds: 250), child: pantalla),
    );
  }
}

class _Arranque extends StatelessWidget {
  const _Arranque();

  @override
  Widget build(BuildContext context) => const Scaffold(
        backgroundColor: Aurum.noche,
        body: Center(child: LogoAurum(alto: 72, vertical: true)),
      );
}
