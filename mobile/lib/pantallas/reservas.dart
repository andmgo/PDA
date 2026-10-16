import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/fechas.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';

/// Reservas del salón social (Fase 9), mismas reglas que la web:
///  · una reserva por salón por día, solo dentro del año actual;
///  · nunca el 24 y 31 de diciembre ni el 1 de enero;
///  · para el año siguiente se envía una solicitud que aprueba la administración.
/// Endpoints: GET /api/salones/todos, GET /api/reservas/salon/{id}/ocupados,
/// POST /api/reservas/crear-propia, GET /api/reservas/mias,
/// DELETE /api/reservas/eliminar/{id}, POST /api/solicitudes-reserva.
class PantallaReservas extends StatefulWidget {
  final Api api;
  const PantallaReservas({super.key, required this.api});

  @override
  State<PantallaReservas> createState() => _PantallaReservasState();
}

bool _diaBloqueado(DateTime d) => (d.month == 12 && (d.day == 24 || d.day == 31)) || (d.month == 1 && d.day == 1);

class _PantallaReservasState extends State<PantallaReservas> {
  List<dynamic>? _salones;
  List<dynamic>? _mias;
  String? _error;

  String? _idSalon;
  Set<DateTime> _ocupados = {};
  DateTime? _fecha;
  TimeOfDay _hora = const TimeOfDay(hour: 18, minute: 0);
  bool _enviando = false;
  ({String texto, bool error})? _resultado;

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  Future<T?> _llamar<T>(Future<T> Function() f) async {
    try {
      return await f();
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
      return null;
    }
  }

  Future<void> _cargar() async {
    try {
      final r = await _llamar(() => Future.wait([
            widget.api.get('/api/salones/todos'),
            widget.api.get('/api/reservas/mias'),
          ]));
      if (r == null || !mounted) return;
      final salones = (r[0] as List).where((s) => s['activo'] == true).toList()
        ..sort((a, b) => '${a['numero']}'.compareTo('${b['numero']}'));
      setState(() {
        _salones = salones;
        _mias = r[1] as List<dynamic>;
        _error = null;
      });
      if (_idSalon == null && salones.isNotEmpty) _elegirSalon('${salones.first['id']}');
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
    }
  }

  Future<void> _elegirSalon(String id) async {
    setState(() {
      _idSalon = id;
      _fecha = null;
      _ocupados = {};
    });
    try {
      final r = await _llamar(() => widget.api.get('/api/reservas/salon/$id/ocupados'));
      if (r == null || !mounted || _idSalon != id) return;
      setState(() => _ocupados = {for (final f in r as List) leerIso(f)!});
    } on ApiError catch (e) {
      if (mounted) setState(() => _resultado = (texto: e.mensaje, error: true));
    }
  }

  bool _disponible(DateTime d) => !_diaBloqueado(d) && !_ocupados.contains(soloFecha(d));

  Future<void> _elegirFecha() async {
    final hoy = soloFecha(DateTime.now());
    final fin = DateTime(hoy.year, 12, 31);
    // El calendario exige que la fecha inicial sea seleccionable.
    var inicial = _fecha ?? hoy;
    while (!_disponible(inicial) && inicial.isBefore(fin)) {
      inicial = inicial.add(const Duration(days: 1));
    }
    if (!_disponible(inicial)) {
      setState(() => _resultado = (texto: 'No quedan días libres este año en este salón.', error: true));
      return;
    }
    final elegida = await showDatePicker(
      context: context,
      initialDate: inicial,
      firstDate: hoy,
      lastDate: fin,
      selectableDayPredicate: _disponible,
      helpText: 'Día de la reserva',
    );
    if (elegida != null) setState(() => _fecha = elegida);
  }

  Future<void> _elegirHora() async {
    final h = await showTimePicker(context: context, initialTime: _hora, helpText: 'Hora de inicio');
    if (h != null) setState(() => _hora = h);
  }

  String _iso(DateTime fecha, TimeOfDay hora) =>
      '${fechaIso(fecha)}T${hora.hour.toString().padLeft(2, '0')}:${hora.minute.toString().padLeft(2, '0')}:00';

  Future<void> _reservar() async {
    if (_idSalon == null || _fecha == null) {
      setState(() => _resultado = (texto: 'Elige el salón y el día.', error: true));
      return;
    }
    setState(() {
      _enviando = true;
      _resultado = null;
    });
    try {
      final m = await _llamar(() => widget.api.post('/api/reservas/crear-propia', {
            'idSalon': _idSalon,
            'fechaYHoraReserva': _iso(_fecha!, _hora),
          }));
      if (m == null || !mounted) return;
      setState(() {
        _resultado = (texto: '$m: ${fechaCorta(_fecha!)} a las ${_hora.format(context)}.', error: false);
        _fecha = null;
      });
      await _cargar();
      if (_idSalon != null) _elegirSalon(_idSalon!);
    } on ApiError catch (e) {
      if (mounted) setState(() => _resultado = (texto: e.mensaje, error: true));
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  Future<void> _cancelar(Map<String, dynamic> r) async {
    final fecha = leerFechaHora(r['fechaYHora']);
    final si = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('Cancelar reserva'),
        content: Text('¿Cancelar la reserva del salón ${r['numeroSalon']}'
            '${fecha != null ? ' del ${fechaHoraCorta(fecha)}' : ''}? El día queda libre para otros vecinos.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Volver')),
          FilledButton(
              style: FilledButton.styleFrom(minimumSize: const Size(96, 44)),
              onPressed: () => Navigator.pop(c, true),
              child: const Text('Cancelar reserva')),
        ],
      ),
    );
    if (si != true || !mounted) return;
    try {
      final m = await _llamar(() => widget.api.delete('/api/reservas/eliminar/${r['id']}'));
      if (m == null || !mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$m')));
      await _cargar();
      if (_idSalon != null) _elegirSalon(_idSalon!);
    } on ApiError catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.mensaje)));
    }
  }

  Future<void> _solicitarOtroAnio() async {
    if (_idSalon == null) return;
    final enviada = await showDialog<String>(
      context: context,
      builder: (_) => _DialogoSolicitud(api: widget.api, idSalon: _idSalon!, numeroSalon: _numeroSalon()),
    );
    if (enviada != null && mounted) setState(() => _resultado = (texto: enviada, error: false));
  }

  String _numeroSalon() =>
      '${_salones?.firstWhere((s) => '${s['id']}' == _idSalon, orElse: () => {'numero': ''})['numero']}';

  // ── Interfaz ───────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final mias = _mias ?? const [];
    final proximas = mias.where((r) => r['pasada'] != true).toList();
    final pasadas = mias.where((r) => r['pasada'] == true).toList();
    return Scaffold(
      appBar: AppBar(title: const Text('Reservas')),
      body: RefreshIndicator(
        color: Aurum.oro,
        onRefresh: _cargar,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
          children: [
            if (_error != null)
              AvisoAurum(_error!)
            else if (_salones == null)
              const Padding(
                  padding: EdgeInsets.all(40), child: Center(child: CircularProgressIndicator(color: Aurum.oro)))
            else ...[
              _nuevaReserva(),
              const SizedBox(height: 14),
              SeccionAurum(
                rotulo: 'Salón social',
                titulo: 'Mis reservas',
                contador: Contador(proximas.length),
                hijo: mias.isEmpty
                    ? const EstadoVacio(icono: Icons.event_available_outlined, texto: 'Aún no tienes reservas.')
                    : Column(children: [
                        for (final r in proximas) _filaReserva(r as Map<String, dynamic>, cancelable: true),
                        for (final r in pasadas.take(5)) _filaReserva(r as Map<String, dynamic>, cancelable: false),
                      ]),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _nuevaReserva() {
    final salones = _salones!;
    return SeccionAurum(
      rotulo: 'Salón social',
      titulo: 'Nueva reserva',
      hijo: salones.isEmpty
          ? const EstadoVacio(icono: Icons.meeting_room_outlined, texto: 'No hay salones disponibles por ahora.')
          : Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
              if (_resultado != null) ...[
                AvisoAurum(_resultado!.texto, error: _resultado!.error),
                const SizedBox(height: 12),
              ],
              Text('Salón', style: GoogleFonts.montserrat(fontSize: 12.5, fontWeight: FontWeight.w700)),
              const SizedBox(height: 8),
              Wrap(spacing: 8, runSpacing: 8, children: [
                for (final s in salones)
                  ChoiceChip(
                    label: Text('${s['numero']}'),
                    selected: _idSalon == '${s['id']}',
                    onSelected: (_) => _elegirSalon('${s['id']}'),
                  ),
              ]),
              const SizedBox(height: 14),
              Row(children: [
                Expanded(child: _campo('Día', _fecha == null ? 'Elegir' : fechaCorta(_fecha!), Icons.event_outlined, _elegirFecha)),
                const SizedBox(width: 12),
                Expanded(child: _campo('Hora', _hora.format(context), Icons.schedule_outlined, _elegirHora)),
              ]),
              const SizedBox(height: 6),
              Text(
                _ocupados.isEmpty
                    ? 'Una reserva por salón por día. No se reserva el 24 y 31 de dic. ni el 1 de ene.'
                    : 'Los días ya reservados aparecen deshabilitados en el calendario.',
                style: GoogleFonts.montserrat(fontSize: 12, color: Aurum.grisTenue),
              ),
              const SizedBox(height: 16),
              FilledButton.icon(
                onPressed: _enviando ? null : _reservar,
                icon: _enviando
                    ? const SizedBox(
                        width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Aurum.blanco))
                    : const Icon(Icons.check_rounded),
                label: const Text('Reservar'),
              ),
              TextButton(
                onPressed: _solicitarOtroAnio,
                child: Text('¿Para el ${DateTime.now().year + 1}? Enviar solicitud'),
              ),
            ]),
    );
  }

  Widget _campo(String rotulo, String valor, IconData icono, VoidCallback alTocar) => InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: alTocar,
        child: InputDecorator(
          decoration: InputDecoration(labelText: rotulo, suffixIcon: Icon(icono, size: 20)),
          child: Text(valor, overflow: TextOverflow.ellipsis),
        ),
      );

  Widget _filaReserva(Map<String, dynamic> r, {required bool cancelable}) {
    final fecha = leerFechaHora(r['fechaYHora']);
    final tema = Theme.of(context);
    return Opacity(
      opacity: cancelable ? 1 : .6,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(children: [
          ChipApto('${r['numeroSalon']}'),
          const SizedBox(width: 12),
          Expanded(
            child: Text(fecha == null ? '—' : fechaHoraCorta(fecha),
                overflow: TextOverflow.ellipsis,
                style: GoogleFonts.montserrat(fontWeight: FontWeight.w600, color: tema.colorScheme.onSurface)),
          ),
          if (cancelable)
            TextButton(onPressed: () => _cancelar(r), child: const Text('Cancelar'))
          else
            const EtiquetaEstado('Pasada', apagada: true),
        ]),
      ),
    );
  }
}

/// Solicitud de reserva para otro año: la aprueba la administración.
class _DialogoSolicitud extends StatefulWidget {
  final Api api;
  final String idSalon;
  final String numeroSalon;
  const _DialogoSolicitud({required this.api, required this.idSalon, required this.numeroSalon});

  @override
  State<_DialogoSolicitud> createState() => _DialogoSolicitudState();
}

class _DialogoSolicitudState extends State<_DialogoSolicitud> {
  final _motivo = TextEditingController();
  DateTime? _fecha;
  String? _error;
  bool _enviando = false;

  @override
  void dispose() {
    _motivo.dispose();
    super.dispose();
  }

  Future<void> _elegir() async {
    final anio = DateTime.now().year + 1;
    var inicial = DateTime(anio, 1, 2);
    final f = await showDatePicker(
      context: context,
      initialDate: _fecha ?? inicial,
      firstDate: DateTime(anio, 1, 1),
      lastDate: DateTime(anio, 12, 31),
      selectableDayPredicate: (d) => !_diaBloqueado(d),
    );
    if (f != null) setState(() => _fecha = f);
  }

  Future<void> _enviar() async {
    if (_fecha == null || _motivo.text.trim().isEmpty) {
      setState(() => _error = 'Elige el día y cuéntale a la administración el motivo.');
      return;
    }
    setState(() => _enviando = true);
    try {
      final m = await widget.api.post('/api/solicitudes-reserva', {
        'idSalon': widget.idSalon,
        'fechaSolicitada': '${fechaIso(_fecha!)}T18:00:00',
        'justificacion': _motivo.text.trim(),
      });
      if (mounted) Navigator.pop(context, '$m');
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
        title: Text('Solicitud ${DateTime.now().year + 1}'),
        content: SingleChildScrollView(
          child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            Text('Salón ${widget.numeroSalon}. La administración la revisa y te confirma.'),
            const SizedBox(height: 12),
            if (_error != null) ...[AvisoAurum(_error!), const SizedBox(height: 12)],
            OutlinedButton.icon(
              onPressed: _elegir,
              icon: const Icon(Icons.event_outlined),
              label: Text(_fecha == null ? 'Elegir día' : fechaCorta(_fecha!)),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _motivo,
              maxLines: 3,
              maxLength: 300,
              decoration: const InputDecoration(labelText: 'Motivo', hintText: 'Ej: celebración de 15 años'),
            ),
          ]),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
          FilledButton(
              style: FilledButton.styleFrom(minimumSize: const Size(96, 44)),
              onPressed: _enviando ? null : _enviar,
              child: const Text('Enviar')),
        ],
      );
}
