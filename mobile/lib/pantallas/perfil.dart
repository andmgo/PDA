import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/sesion.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';

/// Mi perfil (Fase 9): datos de la cuenta, apartamentos y vehículos,
/// cambio de contraseña (POST /api/usuarios/cambiar-contrasena, el
/// documento sale del JWT) y cerrar sesión.
class PantallaPerfil extends StatefulWidget {
  final Api api;
  final Sesion sesion;
  final VoidCallback alCerrarSesion;
  const PantallaPerfil({super.key, required this.api, required this.sesion, required this.alCerrarSesion});

  @override
  State<PantallaPerfil> createState() => _PantallaPerfilState();
}

class _PantallaPerfilState extends State<PantallaPerfil> {
  List<dynamic>? _hogar;

  final _form = GlobalKey<FormState>();
  final _actual = TextEditingController();
  final _nueva = TextEditingController();
  final _confirmar = TextEditingController();
  bool _ver = false;
  bool _enviando = false;
  ({String texto, bool error})? _resultado;

  Sesion get _s => widget.sesion;

  @override
  void initState() {
    super.initState();
    _cargar();
  }

  @override
  void dispose() {
    for (final c in [_actual, _nueva, _confirmar]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _cargar() async {
    try {
      final r = await widget.api.get('/api/propiedad/mia');
      if (mounted) setState(() => _hogar = r as List<dynamic>);
    } on ApiError {
      if (mounted) setState(() => _hogar = const []);
    } on SesionTerminada {
      return;
    }
  }

  Future<void> _cambiar() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _enviando = true;
      _resultado = null;
    });
    try {
      final m = await widget.api.post('/api/usuarios/cambiar-contrasena', {
        'contrasenaActual': _actual.text,
        'contrasenaNueva': _nueva.text,
        'confirmarContrasena': _confirmar.text,
      });
      if (!mounted) return;
      for (final c in [_actual, _nueva, _confirmar]) {
        c.clear();
      }
      FocusScope.of(context).unfocus();
      setState(() => _resultado = (texto: '$m', error: false));
    } on ApiError catch (e) {
      if (mounted) setState(() => _resultado = (texto: e.mensaje, error: true));
    } on SesionTerminada {
      return;
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  Future<void> _salir() async {
    final si = await showDialog<bool>(
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
    if (si == true) widget.alCerrarSesion();
  }

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final nombre = '${_s.nombre} ${_s.apellido}'.trim();
    final iniciales = nombre.split(' ').where((p) => p.isNotEmpty).take(2).map((p) => p[0]).join().toUpperCase();
    final dato = GoogleFonts.montserrat(fontSize: 13.5, color: tema.colorScheme.onSurface);
    return Scaffold(
      appBar: AppBar(title: const Text('Mi perfil')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Row(children: [
                CircleAvatar(
                  radius: 28,
                  backgroundColor: tema.brightness == Brightness.dark ? Aurum.lider : Aurum.noche,
                  child: Text(iniciales,
                      style: GoogleFonts.montserrat(fontWeight: FontWeight.w800, fontSize: 18, color: Aurum.oroClaro)),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text(nombre,
                        style: GoogleFonts.montserrat(
                            fontSize: 17, fontWeight: FontWeight.w800, color: tema.colorScheme.onSurface)),
                    const SizedBox(height: 4),
                    EtiquetaEstado(_s.rolLegible, destacada: true),
                  ]),
                ),
              ]),
            ),
          ),
          const SizedBox(height: 14),
          SeccionAurum(
            rotulo: 'Cuenta',
            titulo: 'Mis datos',
            hijo: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              _fila(Icons.badge_outlined, 'Documento', _s.numeroDocumento, dato),
              _fila(Icons.mail_outline_rounded, 'Correo', _s.correo.isEmpty ? '—' : _s.correo, dato),
              const SizedBox(height: 6),
              Text('Para cambiar estos datos, escribe a la administración.',
                  style: GoogleFonts.montserrat(fontSize: 12, color: Aurum.grisTenue)),
            ]),
          ),
          if (_hogar != null && _hogar!.isNotEmpty) ...[
            const SizedBox(height: 14),
            SeccionAurum(
              rotulo: 'Mi hogar',
              titulo: _hogar!.length == 1 ? 'Mi apartamento' : 'Mis apartamentos',
              hijo: Column(children: [
                for (final a in _hogar!)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
                      ChipApto('${a['numeroApartamento']}'),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Wrap(spacing: 6, runSpacing: 6, children: [
                          for (final v in (a['vehiculos'] as List? ?? const []).where((v) => v['activo'] == true))
                            ChipPlaca('${v['placa']}'),
                        ]),
                      ),
                    ]),
                  ),
              ]),
            ),
          ],
          const SizedBox(height: 14),
          SeccionAurum(
            rotulo: 'Seguridad',
            titulo: 'Cambiar contraseña',
            hijo: Form(
              key: _form,
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                if (_resultado != null) ...[
                  AvisoAurum(_resultado!.texto, error: _resultado!.error),
                  const SizedBox(height: 12),
                ],
                TextFormField(
                  controller: _actual,
                  obscureText: !_ver,
                  decoration: InputDecoration(
                    labelText: 'Contraseña actual',
                    suffixIcon: IconButton(
                      tooltip: _ver ? 'Ocultar' : 'Mostrar',
                      onPressed: () => setState(() => _ver = !_ver),
                      icon: Icon(_ver ? Icons.visibility_off_outlined : Icons.visibility_outlined),
                    ),
                  ),
                  validator: (v) => (v == null || v.isEmpty) ? 'Obligatoria' : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _nueva,
                  obscureText: !_ver,
                  decoration: const InputDecoration(labelText: 'Contraseña nueva', helperText: 'Mínimo 6 caracteres'),
                  validator: (v) => (v == null || v.length < 6) ? 'Mínimo 6 caracteres' : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _confirmar,
                  obscureText: !_ver,
                  decoration: const InputDecoration(labelText: 'Repite la contraseña nueva'),
                  validator: (v) => v != _nueva.text ? 'No coincide con la nueva' : null,
                ),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: _enviando ? null : _cambiar,
                  child: _enviando
                      ? const SizedBox(
                          width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Aurum.blanco))
                      : const Text('Cambiar contraseña'),
                ),
              ]),
            ),
          ),
          const SizedBox(height: 20),
          OutlinedButton.icon(
            style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(50)),
            onPressed: _salir,
            icon: const Icon(Icons.logout_rounded),
            label: const Text('Cerrar sesión'),
          ),
        ],
      ),
    );
  }

  Widget _fila(IconData icono, String rotulo, String valor, TextStyle estilo) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(children: [
          Icon(icono, size: 20, color: Aurum.oro),
          const SizedBox(width: 12),
          SizedBox(
              width: 90,
              child: Text(rotulo, style: GoogleFonts.montserrat(fontSize: 12.5, color: Aurum.grisTenue))),
          Expanded(child: Text(valor, overflow: TextOverflow.ellipsis, style: estilo)),
        ]),
      );
}
