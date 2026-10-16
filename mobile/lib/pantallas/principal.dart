import 'package:flutter/material.dart';

import '../datos/api.dart';
import '../datos/sesion.dart';
import '../tema/aurum_tema.dart';
import 'inicio.dart';
import 'mis_visitas.dart';
import 'perfil.dart';
import 'porteria.dart';
import 'reservas.dart';

/// Navegación por rol (Fase 9): barra inferior cuyas pestañas salen de los
/// PERMISOS del rol, igual que el menú de la web.
///   Propietario:   Inicio · Visitas · Reservas · Perfil
///   Funcionario:   Inicio · Portería · Reservas · Perfil
///   Administrador: Inicio · Portería · Visitas · Reservas · Perfil
/// Cada pestaña se crea la primera vez que se abre (no se piden datos de
/// pantallas que la persona no visita) y conserva su estado al cambiar.
class PantallaPrincipal extends StatefulWidget {
  final Api api;
  final Sesion sesion;
  final VoidCallback alCerrarSesion;
  final void Function(Sesion)? alActualizarSesion;

  const PantallaPrincipal(
      {super.key, required this.api, required this.sesion, required this.alCerrarSesion, this.alActualizarSesion});

  @override
  State<PantallaPrincipal> createState() => _PantallaPrincipalState();
}

class _Pestana {
  final String clave;
  final String texto;
  final IconData icono;
  final IconData iconoActivo;
  const _Pestana(this.clave, this.texto, this.icono, this.iconoActivo);
}

class _PantallaPrincipalState extends State<PantallaPrincipal> {
  String _actual = 'inicio';
  final Set<String> _visitadas = {'inicio'};

  List<_Pestana> get _pestanas {
    final s = widget.sesion;
    return [
      const _Pestana('inicio', 'Inicio', Icons.home_outlined, Icons.home_rounded),
      if (s.puedeVer('CONTROL_ACCESOS'))
        const _Pestana('porteria', 'Portería', Icons.verified_user_outlined, Icons.verified_user_rounded),
      if (s.puedeVer('VISITAS'))
        const _Pestana('visitas', 'Visitas', Icons.event_available_outlined, Icons.event_available_rounded),
      if (s.puedeVer('RESERVAS'))
        const _Pestana('reservas', 'Reservas', Icons.calendar_month_outlined, Icons.calendar_month_rounded),
      const _Pestana('perfil', 'Perfil', Icons.person_outline_rounded, Icons.person_rounded),
    ];
  }

  /// Desde las tarjetas del inicio: si esa sección es una pestaña, se cambia a ella.
  bool _irA(String clave) {
    if (!_pestanas.any((p) => p.clave == clave)) return false;
    setState(() {
      _actual = clave;
      _visitadas.add(clave);
    });
    return true;
  }

  Widget _pantalla(String clave) => switch (clave) {
        'porteria' => PantallaPorteria(api: widget.api),
        'visitas' => PantallaMisVisitas(api: widget.api),
        'reservas' => PantallaReservas(api: widget.api),
        'perfil' => PantallaPerfil(api: widget.api, sesion: widget.sesion, alCerrarSesion: widget.alCerrarSesion),
        _ => PantallaInicio(
            api: widget.api,
            sesion: widget.sesion,
            alCerrarSesion: widget.alCerrarSesion,
            alActualizarSesion: widget.alActualizarSesion,
            alIrAPestana: _irA,
          ),
      };

  @override
  Widget build(BuildContext context) {
    final pestanas = _pestanas;
    // Si un cambio de permisos quitó la pestaña abierta, vuelve al inicio.
    if (!pestanas.any((p) => p.clave == _actual)) _actual = 'inicio';
    final indice = pestanas.indexWhere((p) => p.clave == _actual);

    return PopScope(
      // "Atrás" en otra pestaña vuelve al inicio en vez de cerrar la app.
      canPop: _actual == 'inicio',
      onPopInvokedWithResult: (salio, _) {
        if (!salio) setState(() => _actual = 'inicio');
      },
      child: Scaffold(
        body: IndexedStack(
          index: indice,
          children: [
            for (final p in pestanas)
              _visitadas.contains(p.clave) ? KeyedSubtree(key: ValueKey(p.clave), child: _pantalla(p.clave)) : const SizedBox(),
          ],
        ),
        bottomNavigationBar: NavigationBar(
          selectedIndex: indice,
          indicatorColor: Aurum.oro.withValues(alpha: .25),
          labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
          onDestinationSelected: (i) => setState(() {
            _actual = pestanas[i].clave;
            _visitadas.add(_actual);
          }),
          destinations: [
            for (final p in pestanas)
              NavigationDestination(icon: Icon(p.icono), selectedIcon: Icon(p.iconoActivo), label: p.texto),
          ],
        ),
      ),
    );
  }
}
