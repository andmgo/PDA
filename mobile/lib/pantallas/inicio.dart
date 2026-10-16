import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/sesion.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';
import 'lector_placas.dart';
import 'mis_visitas.dart';
import 'paquetes.dart';
import 'porteria.dart';

/// Inicio según los PERMISOS del rol (los mismos de "Roles y permisos" en la
/// web), no según el nombre del rol: un rol nuevo como VIGILANTE con permiso
/// CONTROL_ACCESOS ve la sección de portería sin cambiar la app.
class PantallaInicio extends StatefulWidget {
  final Api api;
  final Sesion sesion;
  final VoidCallback alCerrarSesion;

  /// Se llama cuando el API devuelve permisos distintos a los guardados.
  final void Function(Sesion)? alActualizarSesion;

  const PantallaInicio(
      {super.key, required this.api, required this.sesion, required this.alCerrarSesion, this.alActualizarSesion});

  @override
  State<PantallaInicio> createState() => _PantallaInicioState();
}

class _PantallaInicioState extends State<PantallaInicio> {
  List<dynamic>? _hogar; // GET /api/propiedad/mia
  List<dynamic>? _dentro; // GET /api/accesos/visitas/dentro
  int? _paquetesPendientes; // GET /api/paquetes/pendientes
  int? _misPaquetes; // GET /api/paquetes/mios (por recoger)
  String? _error;
  bool _cargando = true;

  // Arranca con la sesión guardada y se refresca con GET /api/auth/sesion en
  // cada carga: si el administrador cambió los permisos del rol (o se creó
  // un permiso nuevo), el inicio se ajusta sin volver a iniciar sesión.
  late Sesion _s = widget.sesion;
  bool get _porteria => _s.puedeVer('CONTROL_ACCESOS');
  bool get _paquetes => _s.puedeVer('PAQUETES');
  bool get _visitas => _s.puedeVer('VISITAS');
  bool get _misPaquetesPermiso => _s.puedeVer('MIS_PAQUETES');

  /// Abre una pantalla y al volver refresca el inicio (pudo cambiar quién está dentro).
  Future<void> _abrir(Widget pantalla) async {
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => pantalla));
    if (mounted) _cargar();
  }

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  Future<void> _cargar() async {
    setState(() {
      _cargando = true;
      _error = null;
    });
    try {
      await _refrescarSesion();
      final resultados = await Future.wait([
        widget.api.get('/api/propiedad/mia'),
        if (_porteria) widget.api.get('/api/accesos/visitas/dentro'),
        if (_paquetes) widget.api.get('/api/paquetes/pendientes'),
        if (_misPaquetesPermiso) widget.api.get('/api/paquetes/mios'),
      ]);
      var i = 0;
      _hogar = resultados[i++] as List<dynamic>;
      if (_porteria) _dentro = resultados[i++] as List<dynamic>;
      if (_paquetes) _paquetesPendientes = (resultados[i++] as List<dynamic>).length;
      if (_misPaquetesPermiso) {
        _misPaquetes = (resultados[i++] as List<dynamic>).where((p) => p['entregado'] != true).length;
      }
    } on ApiError catch (e) {
      _error = e.mensaje;
    } on SesionTerminada {
      return; // la app ya volvió al inicio de sesión
    }
    if (mounted) setState(() => _cargando = false);
  }

  Future<void> _refrescarSesion() async {
    try {
      final actual = await widget.api.get('/api/auth/sesion');
      if (actual is! Map<String, dynamic>) return;
      final nueva = _s.actualizadaCon(actual);
      _s = nueva;
      widget.alActualizarSesion?.call(nueva);
    } on ApiError {
      // sin conexión o API vieja: se siguen usando los permisos guardados
    }
  }

  Future<void> _confirmarSalir() async {
    final salir = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('Cerrar sesión'),
        content: const Text('¿Quieres salir de Aurum en este dispositivo?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Cancelar')),
          FilledButton(
              style: FilledButton.styleFrom(minimumSize: const Size(96, 44)),
              onPressed: () => Navigator.pop(c, true),
              child: const Text('Salir')),
        ],
      ),
    );
    if (salir == true) widget.alCerrarSesion();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: RefreshIndicator(
          color: Aurum.oro,
          onRefresh: _cargar,
          child: ListView(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
            children: [
              EncabezadoAurum(
                rotulo: 'Terrazas de Calicanto · ${_s.rolLegible}',
                titulo: Wrap(children: [const Text('Hola, '), TextoOro(_s.nombre)]),
                descripcion: 'Tu conjunto, en un solo lugar.',
                accion: IconButton(
                  tooltip: 'Cerrar sesión',
                  onPressed: _confirmarSalir,
                  icon: const Icon(Icons.logout_rounded, color: Aurum.blanco),
                ),
                pie: Row(children: [
                  const Icon(Icons.calendar_today_outlined, size: 15, color: Aurum.oroClaro),
                  const SizedBox(width: 8),
                  Flexible(
                    child: Text(_hoy(),
                        style: GoogleFonts.montserrat(fontSize: 13, color: Aurum.blanco.withValues(alpha: .65))),
                  ),
                ]),
              ),
              const SizedBox(height: 16),
              if (_cargando && _hogar == null)
                const Padding(
                  padding: EdgeInsets.all(40),
                  child: Center(child: CircularProgressIndicator(color: Aurum.oro)),
                )
              else if (_error != null)
                _tarjetaError()
              else ...[
                if (_porteria) ...[_seccionPorteria(), const SizedBox(height: 14)],
                if (_paquetes) ...[_resumenPaquetes(), const SizedBox(height: 14)],
                if (_hogar!.isNotEmpty || !_porteria) ...[_seccionHogar(), const SizedBox(height: 14)],
                if ((_misPaquetes ?? 0) > 0) ...[_avisoMisPaquetes(), const SizedBox(height: 14)],
              ],
              const SizedBox(height: 4),
              const Rotulo('Accesos'),
              const SizedBox(height: 10),
              _accesos(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _tarjetaError() => Card(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(children: [
            EstadoVacio(icono: Icons.wifi_off_rounded, texto: _error!),
            OutlinedButton(onPressed: _cargar, child: const Text('Reintentar')),
          ]),
        ),
      );

  Widget _seccionPorteria() {
    final dentro = _dentro ?? const [];
    return SeccionAurum(
      rotulo: 'Portería · en este momento',
      titulo: 'Dentro del conjunto',
      contador: Contador(dentro.length),
      hijo: dentro.isEmpty
          ? const EstadoVacio(icono: Icons.verified_user_outlined, texto: 'No hay visitantes dentro del conjunto.')
          : Column(children: [for (final v in dentro.take(5)) _filaVisita(v)]),
    );
  }

  Widget _filaVisita(dynamic v) {
    final tema = Theme.of(context);
    final nombre = '${v['nombreCompleto'] ?? '—'}';
    final entrada = '${v['entrada'] ?? ''}';
    final hora = entrada.length >= 16 ? entrada.substring(11, 16) : '';
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(children: [
        AvatarIniciales(nombre),
        const SizedBox(width: 12),
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(nombre,
                overflow: TextOverflow.ellipsis,
                style: GoogleFonts.montserrat(
                    fontWeight: FontWeight.w700, fontSize: 14, color: tema.colorScheme.onSurface)),
            const SizedBox(height: 4),
            Wrap(spacing: 6, runSpacing: 4, crossAxisAlignment: WrapCrossAlignment.center, children: [
              ChipApto('${v['numeroApartamento']}'),
              if (v['placa'] != null) ChipPlaca('${v['placa']}') else _dato('A pie'),
            ]),
          ]),
        ),
        Text(hora, style: GoogleFonts.montserrat(fontWeight: FontWeight.w700, color: tema.colorScheme.onSurface)),
      ]),
    );
  }

  Widget _resumenPaquetes() {
    final n = _paquetesPendientes ?? 0;
    return SeccionAurum(
      rotulo: 'Recepción',
      titulo: 'Paquetes pendientes',
      contador: Contador(n),
      hijo: Text(
        n == 0 ? 'No hay paquetes por entregar.' : 'Hay $n ${n == 1 ? 'paquete' : 'paquetes'} esperando entrega.',
        style: GoogleFonts.montserrat(fontSize: 13.5),
      ),
    );
  }

  /// Propietario: "Tienes N paquetes en portería" (toca para ver el detalle).
  Widget _avisoMisPaquetes() {
    final n = _misPaquetes!;
    return InkWell(
      borderRadius: BorderRadius.circular(10),
      onTap: () => _abrir(PantallaMisPaquetes(api: widget.api)),
      child: AvisoAurum('',
          contenido: Row(children: [
            const Icon(Icons.inventory_2_outlined, color: Aurum.oro, size: 22),
            const SizedBox(width: 10),
            Expanded(
              child: Text(n == 1 ? 'Tienes 1 paquete por recoger en portería.' : 'Tienes $n paquetes por recoger en portería.',
                  style: GoogleFonts.montserrat(
                      fontSize: 13.5,
                      fontWeight: FontWeight.w600,
                      color: Theme.of(context).brightness == Brightness.dark ? Aurum.blanco : Aurum.noche)),
            ),
            const Icon(Icons.chevron_right_rounded, color: Aurum.oro),
          ])),
    );
  }

  Widget _seccionHogar() {
    final aptos = _hogar ?? const [];
    return SeccionAurum(
      rotulo: 'Mi hogar',
      titulo: aptos.length == 1 ? 'Mi apartamento' : 'Mis apartamentos',
      hijo: aptos.isEmpty
          ? const EstadoVacio(
              icono: Icons.home_outlined,
              texto: 'Aún no tienes apartamentos vinculados.\nLa administración los asigna desde la web.')
          : Column(children: [
              for (final a in aptos)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 6),
                  child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    ChipApto('${a['numeroApartamento']}'),
                    const SizedBox(width: 12),
                    Expanded(child: _vehiculos(a['vehiculos'] as List<dynamic>? ?? const [])),
                  ]),
                ),
            ]),
    );
  }

  Widget _vehiculos(List<dynamic> vehiculos) {
    final activos = vehiculos.where((v) => v['activo'] == true).toList();
    if (activos.isEmpty) return _dato('Sin vehículos registrados');
    return Wrap(spacing: 6, runSpacing: 6, children: [for (final v in activos) ChipPlaca('${v['placa']}')]);
  }

  Widget _dato(String t) => Text(t, style: GoogleFonts.montserrat(fontSize: 12.5, color: Aurum.grisTenue));

  Widget _accesos() {
    // Mismo orden y nombres que el menú de la web: Mi hogar → Portería → Comunidad.
    final tarjetas = <Widget>[
      if (_visitas)
        TarjetaAccion(
            icono: Icons.event_available_outlined,
            titulo: 'Mis visitas',
            subtitulo: 'Autorizar visitantes',
            alTocar: () => _abrir(PantallaMisVisitas(api: widget.api))),
      if (_misPaquetesPermiso)
        TarjetaAccion(
            icono: Icons.markunread_mailbox_outlined,
            titulo: 'Mis paquetes',
            subtitulo: 'Por recoger',
            alTocar: () => _abrir(PantallaMisPaquetes(api: widget.api))),
      if (_porteria) ...[
        TarjetaAccion(
            icono: Icons.how_to_reg_outlined,
            titulo: 'Control de accesos',
            subtitulo: 'Ingreso de visitantes',
            alTocar: () => _abrir(PantallaPorteria(api: widget.api))),
        TarjetaAccion(
            icono: Icons.document_scanner_outlined,
            titulo: 'Lector de placas',
            subtitulo: 'Con la cámara',
            alTocar: () => _abrir(PantallaLectorPlacas(api: widget.api))),
      ],
      if (_paquetes)
        TarjetaAccion(
            icono: Icons.inventory_2_outlined,
            titulo: 'Recepción de paquetes',
            subtitulo: 'Portería',
            alTocar: () => _abrir(PantallaPaquetes(api: widget.api))),
      if (_s.puedeVer('RESERVAS'))
        const TarjetaAccion(
            icono: Icons.calendar_month_outlined,
            titulo: 'Reservas',
            subtitulo: 'Salón social',
            proximamente: 'próxima fase'),
    ];
    return LayoutBuilder(builder: (context, c) {
      final columnas = c.maxWidth > 560 ? 3 : 2;
      final ancho = (c.maxWidth - (columnas - 1) * 12) / columnas;
      return Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [for (final t in tarjetas) SizedBox(width: ancho, child: t)],
      );
    });
  }

  static String _hoy() {
    const dias = ['lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado', 'domingo'];
    const meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre',
      'octubre', 'noviembre', 'diciembre'];
    final h = DateTime.now();
    final texto = '${dias[h.weekday - 1]}, ${h.day} de ${meses[h.month - 1]} de ${h.year}';
    return texto[0].toUpperCase() + texto.substring(1);
  }
}
