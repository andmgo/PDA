import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/fechas.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';

/// Tarjeta de un paquete, compartida por "Mis paquetes" y portería.
class TarjetaPaquete extends StatelessWidget {
  final Map<String, dynamic> p;
  final Widget? accion;
  const TarjetaPaquete(this.p, {super.key, this.accion});

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final oscuro = tema.brightness == Brightness.dark;
    final entregado = p['entregado'] == true;
    final llegada = leerFechaHora(p['fechaHoraLlegada']);
    final entrega = leerFechaHora(p['fechaHoraEntrega']);
    final tenue = GoogleFonts.montserrat(fontSize: 12, color: Aurum.grisTenue);
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Opacity(
        opacity: entregado ? .75 : 1,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
              CircleAvatar(
                radius: 20,
                backgroundColor: entregado ? Aurum.grisTenue : (oscuro ? Aurum.lider : Aurum.noche),
                child: Icon(Icons.inventory_2_outlined, size: 19, color: entregado ? Aurum.blanco : Aurum.oroClaro),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text('${p['descripcion'] ?? 'Paquete'}',
                      overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.montserrat(
                          fontWeight: FontWeight.w700, fontSize: 15, color: tema.colorScheme.onSurface)),
                  const SizedBox(height: 6),
                  Wrap(spacing: 6, runSpacing: 6, crossAxisAlignment: WrapCrossAlignment.center, children: [
                    ChipApto('${p['numeroApartamento']}'),
                    Text('Para ${p['nombreReceptor']}', style: tenue),
                  ]),
                  const SizedBox(height: 6),
                  if (llegada != null)
                    Text('Llegó ${fechaHoraCorta(llegada)}'
                        '${p['recibidoPor'] != null ? ' · recibió ${p['recibidoPor']}' : ''}', style: tenue),
                  if (entregado && entrega != null)
                    Text('Entregado ${fechaHoraCorta(entrega)}'
                        '${p['entregadoA'] != null ? ' a ${p['entregadoA']}' : ''}', style: tenue),
                ]),
              ),
            ]),
            const SizedBox(height: 10),
            Row(children: [
              EtiquetaEstado(entregado ? 'Entregado' : 'En portería', apagada: entregado),
              if (!entregado && llegada != null) ...[
                const SizedBox(width: 8),
                Flexible(child: Text(haceCuanto(llegada), overflow: TextOverflow.ellipsis, style: tenue)),
              ],
            ]),
            if (accion != null) ...[const SizedBox(height: 10), SizedBox(width: double.infinity, child: accion)],
          ]),
        ),
      ),
    );
  }
}

/// "Mis paquetes" del propietario (Fase 7): GET /api/paquetes/mios.
class PantallaMisPaquetes extends StatefulWidget {
  final Api api;
  const PantallaMisPaquetes({super.key, required this.api});

  @override
  State<PantallaMisPaquetes> createState() => _PantallaMisPaquetesState();
}

class _PantallaMisPaquetesState extends State<PantallaMisPaquetes> {
  List<dynamic>? _paquetes;
  String? _error;
  bool _porRecoger = true;

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  Future<void> _cargar() async {
    try {
      final r = await widget.api.get('/api/paquetes/mios') as List<dynamic>;
      if (mounted) {
        setState(() {
          _paquetes = r;
          _error = null;
        });
      }
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    }
  }

  @override
  Widget build(BuildContext context) {
    final todos = _paquetes;
    final pendientes = todos?.where((p) => p['entregado'] != true).toList() ?? const [];
    final lista = todos == null ? const [] : (_porRecoger ? pendientes : todos.where((p) => p['entregado'] == true).toList());
    final masAntiguo = pendientes.isEmpty ? null : leerFechaHora(pendientes.first['fechaHoraLlegada']);

    return Scaffold(
      appBar: AppBar(title: const Text('Mis paquetes')),
      body: RefreshIndicator(
        color: Aurum.oro,
        onRefresh: _cargar,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
          children: [
            if (_error != null)
              AvisoAurum(_error!)
            else if (todos == null)
              const Padding(
                  padding: EdgeInsets.all(40), child: Center(child: CircularProgressIndicator(color: Aurum.oro)))
            else ...[
              SeccionAurum(
                rotulo: 'Portería',
                titulo: 'Por recoger',
                contador: Contador(pendientes.length),
                hijo: Text(
                  pendientes.isEmpty
                      ? 'No tienes paquetes esperando en portería.'
                      : 'Recógelos en portería. El más antiguo llegó ${haceCuanto(masAntiguo!)}.',
                  style: GoogleFonts.montserrat(fontSize: 13.5),
                ),
              ),
              const SizedBox(height: 14),
              SizedBox(
                width: double.infinity,
                child: SegmentedButton<bool>(
                  showSelectedIcon: false,
                  segments: const [
                    ButtonSegment(value: true, label: Text('Por recoger')),
                    ButtonSegment(value: false, label: Text('Entregados')),
                  ],
                  selected: {_porRecoger},
                  onSelectionChanged: (s) => setState(() => _porRecoger = s.first),
                ),
              ),
              const SizedBox(height: 14),
              if (lista.isEmpty)
                EstadoVacio(
                    icono: Icons.inventory_2_outlined,
                    texto: _porRecoger ? 'Nada por recoger.' : 'Aún no hay paquetes entregados.')
              else
                for (final p in lista) TarjetaPaquete(p as Map<String, dynamic>),
            ],
          ],
        ),
      ),
    );
  }
}

/// Recepción de paquetes en portería (permiso PAQUETES): registrar,
/// pendientes con su entrega, y entregados.
class PantallaPaquetes extends StatefulWidget {
  final Api api;
  final int pestanaInicial;
  const PantallaPaquetes({super.key, required this.api, this.pestanaInicial = 0});

  @override
  State<PantallaPaquetes> createState() => _PantallaPaquetesState();
}

class _PantallaPaquetesState extends State<PantallaPaquetes> with SingleTickerProviderStateMixin {
  late final TabController _pestanas =
      TabController(length: 3, vsync: this, initialIndex: widget.pestanaInicial);
  final _form = GlobalKey<FormState>();
  final _apto = TextEditingController();
  final _nombre = TextEditingController();
  final _cedula = TextEditingController();
  final _descripcion = TextEditingController();
  bool _enviando = false;
  ({String texto, bool error})? _resultado;
  List<dynamic>? _todos;

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  @override
  void dispose() {
    _pestanas.dispose();
    for (final c in [_apto, _nombre, _cedula, _descripcion]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _cargar() async {
    try {
      final r = await widget.api.get('/api/paquetes') as List<dynamic>;
      if (mounted) setState(() => _todos = r);
    } on ApiError catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.mensaje)));
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    }
  }

  Future<void> _registrar() async {
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    try {
      final m = await widget.api.post('/api/paquetes', {
        'numeroApartamento': _apto.text.trim(),
        'nombreReceptor': _nombre.text.trim(),
        'cedulaReceptor': _cedula.text.trim(),
        'descripcion': _descripcion.text.trim(),
      });
      if (!mounted) return;
      final apto = _apto.text.trim();
      for (final c in [_apto, _nombre, _cedula, _descripcion]) {
        c.clear();
      }
      setState(() => _resultado = (texto: '$m → apartamento $apto', error: false));
      FocusScope.of(context).unfocus();
      _cargar();
    } on ApiError catch (e) {
      if (mounted) setState(() => _resultado = (texto: e.mensaje, error: true));
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  Future<void> _entregar(Map<String, dynamic> p) async {
    // null = canceló; "" = lo retira el destinatario
    final entregadoA = await showDialog<String>(context: context, builder: (_) => _DialogoEntrega(p));
    if (entregadoA == null || !mounted) return;
    try {
      final m = await widget.api.post('/api/paquetes/${p['id']}/entregar', {'entregadoA': entregadoA});
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$m')));
      _cargar();
    } on ApiError catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.mensaje)));
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    }
  }

  @override
  Widget build(BuildContext context) {
    final pendientes = _todos?.where((p) => p['entregado'] != true).toList();
    final entregados = _todos?.where((p) => p['entregado'] == true).take(50).toList();
    return Scaffold(
      appBar: AppBar(
        title: const Text('Paquetes'),
        bottom: TabBar(
          controller: _pestanas,
          indicatorColor: Aurum.oro,
          labelPadding: const EdgeInsets.symmetric(horizontal: 4),
          labelStyle: GoogleFonts.montserrat(fontWeight: FontWeight.w700, fontSize: 13.5),
          tabs: [
            const Tab(text: 'Registrar'),
            Tab(
              child: Badge(
                isLabelVisible: (pendientes?.length ?? 0) > 0,
                label: Text('${pendientes?.length ?? 0}'),
                backgroundColor: Aurum.oro,
                textColor: Aurum.noche,
                offset: const Offset(14, -6),
                child: const Text('Pendientes'),
              ),
            ),
            const Tab(text: 'Entregados'),
          ],
        ),
      ),
      body: TabBarView(controller: _pestanas, children: [
        _formulario(),
        _lista(pendientes, vacio: 'No hay paquetes pendientes por entregar.', conEntrega: true),
        _lista(entregados, vacio: 'Aún no se han entregado paquetes.', conEntrega: false),
      ]),
    );
  }

  Widget _formulario() => Form(
        key: _form,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
          children: [
            if (_resultado != null) ...[
              AvisoAurum(_resultado!.texto, error: _resultado!.error),
              const SizedBox(height: 14),
            ],
            TextFormField(
              controller: _apto,
              textCapitalization: TextCapitalization.characters,
              decoration: const InputDecoration(labelText: 'Apartamento'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Obligatorio' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _nombre,
              textCapitalization: TextCapitalization.words,
              decoration: const InputDecoration(labelText: 'Nombre de quien recibe'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Obligatorio' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _cedula,
              keyboardType: TextInputType.number,
              maxLength: 15,
              decoration: const InputDecoration(labelText: 'Cédula de quien recibe', counterText: ''),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Obligatorio' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _descripcion,
              maxLength: 150,
              decoration: const InputDecoration(labelText: 'Descripción (opcional)', counterText: ''),
            ),
            const SizedBox(height: 20),
            FilledButton.icon(
              onPressed: _enviando ? null : _registrar,
              icon: _enviando
                  ? const SizedBox(
                      width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Aurum.blanco))
                  : const Icon(Icons.add_rounded),
              label: const Text('Registrar paquete'),
            ),
          ],
        ),
      );

  Widget _lista(List<dynamic>? lista, {required String vacio, required bool conEntrega}) => RefreshIndicator(
        color: Aurum.oro,
        onRefresh: _cargar,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
          children: [
            if (lista == null)
              const Padding(
                  padding: EdgeInsets.all(40), child: Center(child: CircularProgressIndicator(color: Aurum.oro)))
            else if (lista.isEmpty)
              EstadoVacio(icono: Icons.inventory_2_outlined, texto: vacio)
            else
              for (final p in lista)
                TarjetaPaquete(
                  p as Map<String, dynamic>,
                  accion: conEntrega
                      ? FilledButton.icon(
                          style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(44)),
                          onPressed: () => _entregar(p),
                          icon: const Icon(Icons.check_rounded, size: 18),
                          label: const Text('Entregar'))
                      : null,
                ),
          ],
        ),
      );
}

/// Pregunta quién retira el paquete. El diálogo es dueño de su controlador
/// (liberarlo desde afuera mientras el diálogo aún se cierra rompe el layout).
class _DialogoEntrega extends StatefulWidget {
  final Map<String, dynamic> p;
  const _DialogoEntrega(this.p);

  @override
  State<_DialogoEntrega> createState() => _DialogoEntregaState();
}

class _DialogoEntregaState extends State<_DialogoEntrega> {
  final _quien = TextEditingController();

  @override
  void dispose() {
    _quien.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final p = widget.p;
    return AlertDialog(
      title: const Text('Entregar paquete'),
      content: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text('${p['descripcion'] ?? 'Paquete'} para ${p['nombreReceptor']} (apto ${p['numeroApartamento']}).'),
        const SizedBox(height: 14),
        TextField(
          controller: _quien,
          textCapitalization: TextCapitalization.words,
          decoration: const InputDecoration(labelText: '¿Quién lo retira?', hintText: 'Vacío = el destinatario'),
        ),
      ]),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
        FilledButton(
            style: FilledButton.styleFrom(minimumSize: const Size(96, 44)),
            onPressed: () => Navigator.pop(context, _quien.text.trim()),
            child: const Text('Entregar')),
      ],
    );
  }
}
