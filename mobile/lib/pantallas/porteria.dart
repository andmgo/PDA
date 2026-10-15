import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/fechas.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';

/// Portería en el celular (Fase 6), mismos endpoints que Control de Accesos
/// en la web (/api/accesos/visitas/**):
///  · Ingreso: documento → si ya vino se autocompleta; si un propietario lo
///    autorizó para hoy, se avisa y se asocia la autorización.
///  · Esperados: visitas autorizadas para hoy; un toque llena el formulario.
///  · Dentro: quién está en el conjunto, con su salida.
class PantallaPorteria extends StatefulWidget {
  final Api api;
  final int pestanaInicial;
  const PantallaPorteria({super.key, required this.api, this.pestanaInicial = 0});

  @override
  State<PantallaPorteria> createState() => _PantallaPorteriaState();
}

class _PantallaPorteriaState extends State<PantallaPorteria> with SingleTickerProviderStateMixin {
  late final TabController _pestanas =
      TabController(length: 3, vsync: this, initialIndex: widget.pestanaInicial);

  final _form = GlobalKey<FormState>();
  final _documento = TextEditingController();
  final _nombre = TextEditingController();
  final _apellido = TextEditingController();
  final _apto = TextEditingController();
  final _celular = TextEditingController();
  final _placa = TextEditingController();
  bool _vehiculo = false;
  String _parqueadero = '';
  List<String> _libres = const [];

  Map<String, dynamic>? _autorizacion; // la que se usará en el ingreso
  ({String texto, bool error})? _estado; // aviso bajo el documento
  String? _resultado; // mensaje del último ingreso
  bool _resultadoError = false;
  bool _enviando = false;
  Timer? _busqueda;

  List<dynamic>? _esperados;
  List<dynamic>? _dentro;

  @override
  void initState() {
    super.initState();
    _cargarListas();
  }

  @override
  void dispose() {
    _busqueda?.cancel();
    _pestanas.dispose();
    for (final c in [_documento, _nombre, _apellido, _apto, _celular, _placa]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<T?> _llamar<T>(Future<T> Function() f) async {
    try {
      return await f();
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
      return null;
    }
  }

  Future<void> _cargarListas() async {
    try {
      final r = await _llamar(() => Future.wait([
            widget.api.get('/api/accesos/visitas/esperadas'),
            widget.api.get('/api/accesos/visitas/dentro'),
          ]));
      if (r == null || !mounted) return;
      setState(() {
        _esperados = r[0] as List<dynamic>;
        _dentro = r[1] as List<dynamic>;
      });
    } on ApiError catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.mensaje)));
    }
  }

  Future<void> _cargarLibres() async {
    try {
      final r = await _llamar(() => widget.api.get('/api/accesos/visitas/parqueaderos-libres'));
      if (r != null && mounted) setState(() => _libres = [for (final p in r as List) '$p']);
    } on ApiError {
      // sin la lista igual se puede asignar automáticamente
    }
  }

  void _elegirVehiculo(bool vehiculo) {
    setState(() => _vehiculo = vehiculo);
    if (vehiculo) _cargarLibres();
  }

  // ── Documento: autorización o visitante frecuente ──────────────────────

  void _alEscribirDocumento(String texto) {
    _busqueda?.cancel();
    final doc = texto.toUpperCase().replaceAll(RegExp('[^A-Z0-9]'), '');
    if (_autorizacion != null && _autorizacion!['numeroDocumento'] != doc) _autorizacion = null;
    if (doc.length < 5) {
      setState(() => _estado = null);
      return;
    }
    _busqueda = Timer(const Duration(milliseconds: 400), () => _buscar(doc));
  }

  Future<void> _buscar(String doc) async {
    try {
      final autorizada = await _llamar(() => widget.api.get('/api/accesos/visitas/autorizada/$doc'));
      if (!mounted) return;
      if (autorizada is Map<String, dynamic>) {
        _usar(autorizada, desdeLista: false);
        return;
      }
    } on ApiError {
      // sigue con la búsqueda normal
    }
    try {
      final v = await _llamar(() => widget.api.get('/api/accesos/visitas/visitante/$doc'));
      if (v is! Map || !mounted) return;
      _nombre.text = '${v['nombre'] ?? ''}';
      _apellido.text = '${v['apellido'] ?? ''}';
      if (v['celular'] != null) _celular.text = '${v['celular']}';
      if (v['ultimaPlaca'] != null && _placa.text.isEmpty) _placa.text = '${v['ultimaPlaca']}';
      final veces = v['visitasPrevias'] == 1 ? '1 visita previa' : '${v['visitasPrevias']} visitas previas';
      setState(() => _estado = v['bloqueado'] == true
          ? (texto: '${v['nombre']} ${v['apellido']} tiene el ingreso bloqueado.', error: true)
          : (texto: 'Visitante frecuente · ${v['nombre']} ${v['apellido']} · $veces', error: false));
    } on ApiError {
      if (mounted) setState(() => _estado = (texto: 'Primera visita: escribe su nombre y apellido.', error: true));
    }
  }

  /// Llena el formulario con la autorización del propietario.
  void _usar(Map<String, dynamic> a, {required bool desdeLista}) {
    if (desdeLista) {
      _limpiar();
      _documento.text = '${a['numeroDocumento'] ?? ''}';
    }
    _autorizacion = a;
    _nombre.text = '${a['nombreVisitante']}';
    _apellido.text = '${a['apellidoVisitante']}';
    _apto.text = '${a['numeroApartamento']}';
    if (a['placa'] != null) {
      _placa.text = '${a['placa']}';
      _elegirVehiculo(true);
    }
    final nota = a['nota'] != null ? ' · “${a['nota']}”' : '';
    setState(() {
      _resultado = null;
      _estado = (
        texto: 'Visita autorizada · ${a['nombreVisitante']} ${a['apellidoVisitante']} → apto ${a['numeroApartamento']}'
            ' · autorizó ${a['autorizadaPor']}$nota'
            '${a['numeroDocumento'] == null ? '\nEl propietario no dejó el documento: pídeselo y escríbelo.' : ''}',
        error: false,
      );
    });
    if (desdeLista) _pestanas.animateTo(0);
  }

  void _limpiar() {
    for (final c in [_documento, _nombre, _apellido, _apto, _celular, _placa]) {
      c.clear();
    }
    _autorizacion = null;
    _vehiculo = false;
    _parqueadero = '';
    _estado = null;
  }

  Future<void> _registrar() async {
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    try {
      final mensaje = await _llamar(() => widget.api.post('/api/accesos/visitas/ingreso', {
            'numeroDocumento': _documento.text.trim(),
            'nombre': _nombre.text.trim(),
            'apellido': _apellido.text.trim(),
            'celular': _celular.text.trim(),
            'numeroApartamento': _apto.text.trim(),
            'conVehiculo': _vehiculo,
            'placa': _placa.text.trim(),
            'numeroParqueadero': _parqueadero,
            'idAutorizacion': _autorizacion?['id'],
          }));
      if (mensaje == null || !mounted) return;
      setState(() {
        _limpiar();
        _resultado = '$mensaje';
        _resultadoError = false;
      });
      FocusScope.of(context).unfocus();
      _cargarListas();
    } on ApiError catch (e) {
      if (mounted) {
        setState(() {
          _resultado = e.mensaje;
          _resultadoError = true;
        });
      }
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  Future<void> _salida(dynamic v) async {
    try {
      final m = await _llamar(() => widget.api.put('/api/accesos/visitas/${v['tipo']}/${v['id']}/salida'));
      if (m == null || !mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$m')));
      _cargarListas();
    } on ApiError catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.mensaje)));
    }
  }

  // ── Interfaz ───────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final pendientes = (_esperados ?? const []).where((a) => (a['ingresos'] ?? 0) == 0).length;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Control de accesos'),
        bottom: TabBar(
          controller: _pestanas,
          indicatorColor: Aurum.oro,
          labelStyle: GoogleFonts.montserrat(fontWeight: FontWeight.w700, fontSize: 13.5),
          labelPadding: const EdgeInsets.symmetric(horizontal: 4),
          // Los conteos van en una insignia: "Esperados (1)" no cabía en 360 px.
          tabs: [
            const Tab(text: 'Ingreso'),
            Tab(child: _conInsignia('Esperados', pendientes)),
            Tab(child: _conInsignia('Dentro', _dentro?.length ?? 0)),
          ],
        ),
      ),
      body: TabBarView(
        controller: _pestanas,
        children: [_formulario(), _listaEsperados(), _listaDentro()],
      ),
    );
  }

  Widget _conInsignia(String texto, int n) => Badge(
        isLabelVisible: n > 0,
        label: Text('$n'),
        backgroundColor: Aurum.oro,
        textColor: Aurum.noche,
        offset: const Offset(14, -6),
        child: Text(texto),
      );

  Widget _formulario() {
    return Form(
      key: _form,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          if (_resultado != null) ...[AvisoAurum(_resultado!, error: _resultadoError), const SizedBox(height: 14)],
          TextFormField(
            controller: _documento,
            keyboardType: TextInputType.number,
            maxLength: 12,
            style: GoogleFonts.ibmPlexMono(fontSize: 18, letterSpacing: 1.5),
            decoration: const InputDecoration(labelText: 'Documento', counterText: ''),
            onChanged: _alEscribirDocumento,
            validator: (v) => (v == null || v.trim().isEmpty) && _autorizacion?['numeroDocumento'] == null
                ? 'Escribe el documento'
                : null,
          ),
          if (_estado != null) ...[const SizedBox(height: 8), AvisoAurum(_estado!.texto, error: _estado!.error)],
          const SizedBox(height: 12),
          Row(children: [
            Expanded(
              child: TextFormField(
                controller: _nombre,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Nombre'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: _apellido,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Apellido'),
              ),
            ),
          ]),
          const SizedBox(height: 12),
          Row(children: [
            Expanded(
              child: TextFormField(
                controller: _apto,
                textCapitalization: TextCapitalization.characters,
                decoration: const InputDecoration(labelText: 'Apartamento'),
                validator: (v) => (v == null || v.trim().isEmpty) ? 'Obligatorio' : null,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: _celular,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: 'Celular (opc.)'),
              ),
            ),
          ]),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            child: SegmentedButton<bool>(
              showSelectedIcon: false,
              segments: const [
                ButtonSegment(value: false, icon: Icon(Icons.directions_walk_rounded), label: Text('A pie')),
                ButtonSegment(value: true, icon: Icon(Icons.directions_car_outlined), label: Text('En vehículo')),
              ],
              selected: {_vehiculo},
              onSelectionChanged: (s) => _elegirVehiculo(s.first),
            ),
          ),
          if (_vehiculo) ...[
            const SizedBox(height: 12),
            TextFormField(
              controller: _placa,
              textCapitalization: TextCapitalization.characters,
              maxLength: 7,
              style: GoogleFonts.ibmPlexMono(fontSize: 16, letterSpacing: 1.5),
              inputFormatters: [
                FilteringTextInputFormatter.allow(RegExp('[a-zA-Z0-9]')),
                TextInputFormatter.withFunction((_, n) => n.copyWith(text: n.text.toUpperCase())),
              ],
              decoration: const InputDecoration(labelText: 'Placa', counterText: ''),
              validator: (v) => _vehiculo && (v == null || v.trim().length < 5) ? 'Escribe la placa' : null,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _parqueadero,
              isExpanded: true,
              decoration: const InputDecoration(labelText: 'Parqueadero'),
              items: [
                DropdownMenuItem(
                    value: '',
                    child: Text(_libres.isEmpty ? 'Automático' : 'Automático · ${_libres.length} libres',
                        overflow: TextOverflow.ellipsis)),
                for (final p in _libres) DropdownMenuItem(value: p, child: Text(p)),
              ],
              onChanged: (v) => setState(() => _parqueadero = v ?? ''),
            ),
          ],
          const SizedBox(height: 20),
          FilledButton.icon(
            onPressed: _enviando ? null : _registrar,
            icon: _enviando
                ? const SizedBox(
                    width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Aurum.blanco))
                : const Icon(Icons.login_rounded),
            label: const Text('Registrar ingreso'),
          ),
        ],
      ),
    );
  }

  Widget _listaEsperados() {
    final lista = _esperados;
    return RefreshIndicator(
      color: Aurum.oro,
      onRefresh: _cargarListas,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          if (lista == null)
            const Padding(
                padding: EdgeInsets.all(40), child: Center(child: CircularProgressIndicator(color: Aurum.oro)))
          else if (lista.isEmpty)
            const EstadoVacio(
                icono: Icons.how_to_reg_outlined, texto: 'Ningún propietario ha autorizado visitas para hoy.')
          else
            for (final a in [...lista]..sort((x, y) => ((x['ingresos'] ?? 0) > 0 ? 1 : 0) - ((y['ingresos'] ?? 0) > 0 ? 1 : 0)))
              _tarjetaEsperado(a as Map<String, dynamic>),
        ],
      ),
    );
  }

  Widget _tarjetaEsperado(Map<String, dynamic> a) {
    final tema = Theme.of(context);
    final nombre = '${a['nombreVisitante']} ${a['apellidoVisitante']}';
    final ingresos = (a['ingresos'] as num?)?.toInt() ?? 0;
    final llego = ingresos > 0;
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            AvatarIniciales(nombre, apagado: llego),
            const SizedBox(width: 12),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(nombre,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.montserrat(
                        fontWeight: FontWeight.w700, fontSize: 15, color: tema.colorScheme.onSurface)),
                const SizedBox(height: 6),
                Wrap(spacing: 6, runSpacing: 6, crossAxisAlignment: WrapCrossAlignment.center, children: [
                  ChipApto('${a['numeroApartamento']}'),
                  if (a['placa'] != null) ChipPlaca('${a['placa']}'),
                  Text('${a['numeroDocumento'] ?? 'Sin documento'}',
                      style: GoogleFonts.ibmPlexMono(fontSize: 12, color: Aurum.grisTenue)),
                ]),
                const SizedBox(height: 6),
                Text('Autorizó ${a['autorizadaPor']}',
                    style: GoogleFonts.montserrat(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: tema.brightness == Brightness.dark ? Aurum.oroClaro : Aurum.oroTexto)),
                if (a['nota'] != null)
                  Text('“${a['nota']}”',
                      style: GoogleFonts.montserrat(
                          fontSize: 12.5, fontStyle: FontStyle.italic, color: Aurum.grisTenue)),
              ]),
            ),
          ]),
          const SizedBox(height: 10),
          EtiquetaEstado(llego ? (ingresos == 1 ? 'Ya ingresó' : '$ingresos ingresos') : 'Por llegar',
              destacada: !llego, apagada: llego),
          const SizedBox(height: 10),
          // Botón en su propia fila y a todo el ancho: junto a la etiqueta no cabe en 320–412 px.
          SizedBox(
            width: double.infinity,
            child: llego
                ? OutlinedButton(
                    style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(44)),
                    onPressed: () => _usar(a, desdeLista: true),
                    child: const Text('Registrar otro ingreso'))
                : FilledButton(
                    style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(44)),
                    onPressed: () => _usar(a, desdeLista: true),
                    child: const Text('Registrar ingreso')),
          ),
        ]),
      ),
    );
  }

  Widget _listaDentro() {
    final lista = _dentro;
    final tema = Theme.of(context);
    return RefreshIndicator(
      color: Aurum.oro,
      onRefresh: _cargarListas,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          if (lista == null)
            const Padding(
                padding: EdgeInsets.all(40), child: Center(child: CircularProgressIndicator(color: Aurum.oro)))
          else if (lista.isEmpty)
            const EstadoVacio(icono: Icons.verified_user_outlined, texto: 'No hay visitantes dentro del conjunto.')
          else
            for (final v in lista)
              Card(
                margin: const EdgeInsets.only(bottom: 10),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(14, 12, 8, 12),
                  child: Row(children: [
                    AvatarIniciales('${v['nombreCompleto'] ?? '—'}'),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                        Text('${v['nombreCompleto'] ?? '—'}',
                            overflow: TextOverflow.ellipsis,
                            style: GoogleFonts.montserrat(
                                fontWeight: FontWeight.w700, fontSize: 14.5, color: tema.colorScheme.onSurface)),
                        const SizedBox(height: 5),
                        Wrap(spacing: 6, runSpacing: 4, crossAxisAlignment: WrapCrossAlignment.center, children: [
                          ChipApto('${v['numeroApartamento']}'),
                          if (v['placa'] != null) ChipPlaca('${v['placa']}'),
                          Text('Entró ${horaDe(v['entrada'])}',
                              style: GoogleFonts.montserrat(fontSize: 12, color: Aurum.grisTenue)),
                        ]),
                        if (v['autorizadaPor'] != null)
                          Padding(
                            padding: const EdgeInsets.only(top: 4),
                            child: Text('Autorizó ${v['autorizadaPor']}',
                                style: GoogleFonts.montserrat(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w600,
                                    color: tema.brightness == Brightness.dark ? Aurum.oroClaro : Aurum.oroTexto)),
                          ),
                      ]),
                    ),
                    TextButton(onPressed: () => _salida(v), child: const Text('Salida')),
                  ]),
                ),
              ),
        ],
      ),
    );
  }
}
