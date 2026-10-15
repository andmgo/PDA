import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/fechas.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';

/// "Mis visitas" del propietario (Fase 6): autoriza de antemano a quien lo
/// visita y portería lo deja entrar sin llamarlo. Mismos endpoints que la web:
///   GET  /api/visitas-autorizadas/mias
///   POST /api/visitas-autorizadas
///   PUT  /api/visitas-autorizadas/{id}/cancelar
class PantallaMisVisitas extends StatefulWidget {
  final Api api;
  const PantallaMisVisitas({super.key, required this.api});

  @override
  State<PantallaMisVisitas> createState() => _PantallaMisVisitasState();
}

class _PantallaMisVisitasState extends State<PantallaMisVisitas> {
  List<dynamic>? _autorizaciones;
  List<dynamic> _apartamentos = const [];
  String? _error;
  bool _proximas = true;

  static bool _esProxima(dynamic a) => a['estado'] == 'VIGENTE' || a['estado'] == 'PROGRAMADA';

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  Future<void> _cargar() async {
    try {
      final r = await Future.wait([
        widget.api.get('/api/visitas-autorizadas/mias'),
        widget.api.get('/api/propiedad/mia'),
      ]);
      if (!mounted) return;
      setState(() {
        _autorizaciones = r[0] as List<dynamic>;
        _apartamentos = r[1] as List<dynamic>;
        _error = null;
      });
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    }
  }

  Future<void> _nueva() async {
    final creada = await Navigator.of(context).push<Map<String, dynamic>>(
      MaterialPageRoute(builder: (_) => PantallaNuevaVisita(api: widget.api, apartamentos: _apartamentos)),
    );
    if (creada == null || !mounted) return;
    setState(() => _proximas = true);
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text('${creada['nombreVisitante']} ${creada['apellidoVisitante']} quedó autorizado(a). '
            'Portería ya lo ve.')));
    _cargar();
  }

  Future<void> _cancelar(dynamic a) async {
    final si = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('Cancelar autorización'),
        content: Text('Portería ya no dejará entrar a ${a['nombreVisitante']} ${a['apellidoVisitante']} '
            'con esta autorización.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Volver')),
          FilledButton(
              style: FilledButton.styleFrom(minimumSize: const Size(96, 44)),
              onPressed: () => Navigator.pop(c, true),
              child: const Text('Cancelar visita')),
        ],
      ),
    );
    if (si != true || !mounted) return;
    try {
      final mensaje = await widget.api.put('/api/visitas-autorizadas/${a['id']}/cancelar');
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$mensaje')));
      _cargar();
    } on ApiError catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.mensaje)));
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    }
  }

  @override
  Widget build(BuildContext context) {
    final todas = _autorizaciones;
    final lista = todas == null
        ? const []
        : (todas.where((a) => _proximas ? _esProxima(a) : !_esProxima(a)).toList()
          ..sort((x, y) => _proximas
              ? '${x['fechaDesde']}'.compareTo('${y['fechaDesde']}')
              : '${y['fechaDesde']}'.compareTo('${x['fechaDesde']}')));

    return Scaffold(
      appBar: AppBar(title: const Text('Mis visitas')),
      floatingActionButton: _apartamentos.isEmpty
          ? null
          : FloatingActionButton.extended(
              onPressed: _nueva,
              backgroundColor: Aurum.noche,
              foregroundColor: Aurum.oroClaro,
              icon: const Icon(Icons.person_add_alt_1_outlined),
              label: Text('Autorizar', style: GoogleFonts.montserrat(fontWeight: FontWeight.w700)),
            ),
      body: RefreshIndicator(
        color: Aurum.oro,
        onRefresh: _cargar,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 96),
          children: [
            Text('Autoriza a quien te visita y portería lo deja entrar sin llamarte.',
                style: GoogleFonts.montserrat(fontSize: 13.5, color: Aurum.grisTenue)),
            const SizedBox(height: 14),
            SizedBox(
              width: double.infinity,
              child: SegmentedButton<bool>(
                showSelectedIcon: false,
                segments: const [
                  ButtonSegment(value: true, label: Text('Próximas')),
                  ButtonSegment(value: false, label: Text('Historial')),
                ],
                selected: {_proximas},
                onSelectionChanged: (s) => setState(() => _proximas = s.first),
              ),
            ),
            const SizedBox(height: 14),
            if (_error != null)
              AvisoAurum(_error!)
            else if (todas == null)
              const Padding(
                padding: EdgeInsets.all(40),
                child: Center(child: CircularProgressIndicator(color: Aurum.oro)),
              )
            else if (_apartamentos.isEmpty && todas.isEmpty)
              const EstadoVacio(
                  icono: Icons.home_outlined,
                  texto: 'Aún no tienes apartamentos vinculados.\nLa administración los asigna desde la web.')
            else if (lista.isEmpty)
              EstadoVacio(
                  icono: Icons.how_to_reg_outlined,
                  texto: _proximas ? 'No tienes visitas autorizadas próximas.' : 'Aún no hay visitas pasadas.')
            else
              for (final a in lista) _TarjetaAutorizacion(a, alCancelar: _esProxima(a) ? () => _cancelar(a) : null),
          ],
        ),
      ),
    );
  }
}

class _TarjetaAutorizacion extends StatelessWidget {
  final dynamic a;
  final VoidCallback? alCancelar;
  const _TarjetaAutorizacion(this.a, {this.alCancelar});

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final nombre = '${a['nombreVisitante']} ${a['apellidoVisitante']}';
    final estado = '${a['estado']}';
    final activa = alCancelar != null;
    final ingresos = (a['ingresos'] as num?)?.toInt() ?? 0;
    final desde = leerIso(a['fechaDesde']), hasta = leerIso(a['fechaHasta']);
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Opacity(
        opacity: activa ? 1 : .7,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(14, 14, 14, 8),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
              AvatarIniciales(nombre, apagado: !activa),
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
                    Text(a['numeroDocumento'] != null ? '${a['numeroDocumento']}' : 'Sin documento',
                        style: GoogleFonts.ibmPlexMono(fontSize: 12, color: Aurum.grisTenue)),
                    if (ingresos > 0)
                      Text('$ingresos ingreso${ingresos == 1 ? '' : 's'}',
                          style: GoogleFonts.montserrat(fontSize: 12, color: Aurum.grisTenue)),
                  ]),
                  if (a['nota'] != null) ...[
                    const SizedBox(height: 6),
                    Text('“${a['nota']}”',
                        style: GoogleFonts.montserrat(
                            fontSize: 12.5, fontStyle: FontStyle.italic, color: Aurum.grisTenue)),
                  ],
                ]),
              ),
            ]),
            const SizedBox(height: 10),
            Row(children: [
              EtiquetaEstado(
                switch (estado) {
                  'VIGENTE' => 'Hoy',
                  'PROGRAMADA' => 'Programada',
                  'VENCIDA' => 'Vencida',
                  _ => 'Cancelada',
                },
                destacada: estado == 'VIGENTE',
                apagada: !activa,
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  desde != null && hasta != null ? rangoFechas(desde, hasta) : '',
                  overflow: TextOverflow.ellipsis,
                  style: GoogleFonts.montserrat(fontSize: 12, color: Aurum.grisTenue),
                ),
              ),
              if (activa) TextButton(onPressed: alCancelar, child: const Text('Cancelar')),
            ]),
            if (!activa) const SizedBox(height: 6),
          ]),
        ),
      ),
    );
  }
}

/// Formulario para autorizar una visita.
class PantallaNuevaVisita extends StatefulWidget {
  final Api api;
  final List<dynamic> apartamentos;
  const PantallaNuevaVisita({super.key, required this.api, required this.apartamentos});

  @override
  State<PantallaNuevaVisita> createState() => _PantallaNuevaVisitaState();
}

class _PantallaNuevaVisitaState extends State<PantallaNuevaVisita> {
  final _form = GlobalKey<FormState>();
  final _nombre = TextEditingController();
  final _apellido = TextEditingController();
  final _documento = TextEditingController();
  final _placa = TextEditingController();
  final _nota = TextEditingController();
  late String _apartamento = '${widget.apartamentos.first['idApartamento']}';
  late DateTime _desde = soloFecha(DateTime.now());
  late DateTime _hasta = _desde;
  bool _enviando = false;
  String? _error;

  /// Mismas reglas que el API (VisitaAutorizadaModel): hasta 30 días por
  /// autorización y hasta 90 días de anticipación.
  static const _maximoDias = 30, _anticipacion = 90;

  @override
  void dispose() {
    for (final c in [_nombre, _apellido, _documento, _placa, _nota]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _elegir(bool esDesde) async {
    final hoy = soloFecha(DateTime.now());
    final elegida = await showDatePicker(
      context: context,
      initialDate: esDesde ? _desde : _hasta,
      firstDate: esDesde ? hoy : _desde,
      lastDate: esDesde ? hoy.add(const Duration(days: _anticipacion)) : _desde.add(const Duration(days: _maximoDias - 1)),
      helpText: esDesde ? 'Desde qué día' : 'Hasta qué día',
    );
    if (elegida == null) return;
    setState(() {
      if (esDesde) {
        _desde = elegida;
        if (_hasta.isBefore(_desde) || _hasta.difference(_desde).inDays >= _maximoDias) _hasta = _desde;
      } else {
        _hasta = elegida;
      }
    });
  }

  Future<void> _enviar() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _enviando = true;
      _error = null;
    });
    try {
      final creada = await widget.api.post('/api/visitas-autorizadas', {
        'idApartamento': _apartamento,
        'nombreVisitante': _nombre.text.trim(),
        'apellidoVisitante': _apellido.text.trim(),
        'numeroDocumento': _documento.text.trim(),
        'placa': _placa.text.trim(),
        'fechaDesde': fechaIso(_desde),
        'fechaHasta': fechaIso(_hasta),
        'nota': _nota.text.trim(),
      });
      if (mounted) Navigator.of(context).pop(creada as Map<String, dynamic>);
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  String? _requerido(String? v) => (v == null || v.trim().isEmpty) ? 'Obligatorio' : null;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Autorizar visita')),
      body: SafeArea(
        child: Form(
          key: _form,
          child: ListView(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
            children: [
              const Rotulo('Nueva autorización'),
              const SizedBox(height: 4),
              Text('Con el documento, portería lo registra en un toque.',
                  style: GoogleFonts.montserrat(fontSize: 13.5, color: Aurum.grisTenue)),
              const SizedBox(height: 16),
              if (_error != null) ...[AvisoAurum(_error!), const SizedBox(height: 14)],
              if (widget.apartamentos.length > 1) ...[
                DropdownButtonFormField<String>(
                  initialValue: _apartamento,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Apartamento'),
                  items: [
                    for (final a in widget.apartamentos)
                      DropdownMenuItem(
                          value: '${a['idApartamento']}', child: Text('Apartamento ${a['numeroApartamento']}')),
                  ],
                  onChanged: (v) => setState(() => _apartamento = v!),
                ),
                const SizedBox(height: 12),
              ] else ...[
                Row(children: [
                  Text('Visita al apartamento', style: GoogleFonts.montserrat(fontSize: 13.5)),
                  const SizedBox(width: 8),
                  ChipApto('${widget.apartamentos.first['numeroApartamento']}'),
                ]),
                const SizedBox(height: 14),
              ],
              TextFormField(
                controller: _nombre,
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'Nombre'),
                validator: _requerido,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _apellido,
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(labelText: 'Apellido'),
                validator: _requerido,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _documento,
                keyboardType: TextInputType.number,
                textInputAction: TextInputAction.next,
                maxLength: 20,
                decoration: const InputDecoration(labelText: 'Documento (recomendado)', counterText: ''),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _placa,
                textCapitalization: TextCapitalization.characters,
                maxLength: 7,
                inputFormatters: [
                  FilteringTextInputFormatter.allow(RegExp('[a-zA-Z0-9]')),
                  TextInputFormatter.withFunction((_, n) => n.copyWith(text: n.text.toUpperCase())),
                ],
                decoration: const InputDecoration(labelText: 'Placa (si viene en carro)', counterText: ''),
              ),
              const SizedBox(height: 12),
              Row(children: [
                Expanded(child: _botonFecha('Desde', _desde, () => _elegir(true))),
                const SizedBox(width: 12),
                Expanded(child: _botonFecha('Hasta', _hasta, () => _elegir(false))),
              ]),
              const SizedBox(height: 12),
              TextFormField(
                controller: _nota,
                maxLength: 300,
                decoration: const InputDecoration(labelText: 'Nota para portería (opcional)', counterText: ''),
              ),
              const SizedBox(height: 20),
              FilledButton.icon(
                onPressed: _enviando ? null : _enviar,
                icon: _enviando
                    ? const SizedBox(
                        width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Aurum.blanco))
                    : const Icon(Icons.check_rounded),
                label: const Text('Autorizar visita'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _botonFecha(String rotulo, DateTime fecha, VoidCallback alTocar) => InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: alTocar,
        child: InputDecorator(
          decoration: InputDecoration(labelText: rotulo, suffixIcon: const Icon(Icons.event_outlined, size: 20)),
          child: Text(fechaCorta(fecha), overflow: TextOverflow.ellipsis),
        ),
      );
}
