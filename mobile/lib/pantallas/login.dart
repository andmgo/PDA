import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../datos/api.dart';
import '../datos/sesion.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';

/// Inicio de sesión con la misma cuenta de la web (documento o correo).
class PantallaLogin extends StatefulWidget {
  final Api api;
  final ValueChanged<Sesion> alIniciarSesion;
  final String? aviso; // p. ej. "Tu cuenta fue desactivada"

  const PantallaLogin({super.key, required this.api, required this.alIniciarSesion, this.aviso});

  @override
  State<PantallaLogin> createState() => _PantallaLoginState();
}

class _PantallaLoginState extends State<PantallaLogin> {
  final _form = GlobalKey<FormState>();
  final _usuario = TextEditingController();
  final _clave = TextEditingController();
  bool _cargando = false;
  bool _verClave = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _error = widget.aviso;
  }

  @override
  void dispose() {
    _usuario.dispose();
    _clave.dispose();
    super.dispose();
  }

  Future<void> _entrar() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _cargando = true;
      _error = null;
    });
    try {
      final sesion = await widget.api.iniciarSesion(_usuario.text, _clave.text);
      widget.alIniciarSesion(sesion);
    } on ApiError catch (e) {
      setState(() => _error = e.mensaje);
    } finally {
      if (mounted) setState(() => _cargando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    return Scaffold(
      backgroundColor: Aurum.noche,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(28, 36, 28, 30),
              child: Column(children: [
                const LogoAurum(alto: 64, vertical: true),
                const SizedBox(height: 22),
                Text('Tu conjunto, en un solo lugar.',
                    textAlign: TextAlign.center,
                    style: GoogleFonts.montserrat(fontSize: 18, fontWeight: FontWeight.w700, color: Aurum.oroClaro)),
              ]),
            ),
            Expanded(
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: tema.scaffoldBackgroundColor,
                  borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
                ),
                child: SingleChildScrollView(
                  padding: const EdgeInsets.fromLTRB(24, 28, 24, 32),
                  child: Form(
                    key: _form,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Text('Bienvenido de nuevo',
                            style: GoogleFonts.montserrat(
                                fontSize: 24, fontWeight: FontWeight.w800, color: tema.colorScheme.onSurface)),
                        const SizedBox(height: 4),
                        Text('Ingresa con tu número de documento o tu correo.',
                            style: GoogleFonts.montserrat(fontSize: 13.5, color: Aurum.grisTenue)),
                        const SizedBox(height: 22),
                        if (_error != null) ...[
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: Aurum.oroSuave,
                              borderRadius: BorderRadius.circular(12),
                              border: const Border(left: BorderSide(color: Aurum.oro, width: 3)),
                            ),
                            child: Text(_error!,
                                style: GoogleFonts.montserrat(fontSize: 13.5, color: Aurum.noche)),
                          ),
                          const SizedBox(height: 16),
                        ],
                        TextFormField(
                          controller: _usuario,
                          keyboardType: TextInputType.emailAddress,
                          autofillHints: const [AutofillHints.username],
                          textInputAction: TextInputAction.next,
                          decoration: const InputDecoration(
                              labelText: 'Documento o correo', prefixIcon: Icon(Icons.person_outline)),
                          validator: (v) => (v == null || v.trim().length < 4) ? 'Escribe tu documento o correo' : null,
                        ),
                        const SizedBox(height: 14),
                        TextFormField(
                          controller: _clave,
                          obscureText: !_verClave,
                          autofillHints: const [AutofillHints.password],
                          onFieldSubmitted: (_) => _entrar(),
                          decoration: InputDecoration(
                            labelText: 'Contraseña',
                            prefixIcon: const Icon(Icons.lock_outline),
                            suffixIcon: IconButton(
                              tooltip: _verClave ? 'Ocultar' : 'Mostrar',
                              icon: Icon(_verClave ? Icons.visibility_off_outlined : Icons.visibility_outlined),
                              onPressed: () => setState(() => _verClave = !_verClave),
                            ),
                          ),
                          validator: (v) => (v == null || v.isEmpty) ? 'Escribe tu contraseña' : null,
                        ),
                        const SizedBox(height: 22),
                        FilledButton(
                          onPressed: _cargando ? null : _entrar,
                          child: _cargando
                              ? const SizedBox(
                                  width: 22, height: 22, child: CircularProgressIndicator(strokeWidth: 2.4))
                              : const Text('Iniciar sesión'),
                        ),
                        const SizedBox(height: 18),
                        Text('¿No tienes cuenta? Regístrate en la web; la administración la aprueba.',
                            textAlign: TextAlign.center,
                            style: GoogleFonts.montserrat(fontSize: 12.5, color: Aurum.grisTenue)),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
