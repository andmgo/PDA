import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../tema/aurum_tema.dart';

/// Logotipo: isotipo + la palabra "AURUM" como IMAGEN (nunca texto, para
/// conservar la letra del logo) + "CONDOMINIOS" en Montserrat espaciada.
class LogoAurum extends StatelessWidget {
  final double alto;
  final bool vertical;
  const LogoAurum({super.key, this.alto = 44, this.vertical = false});

  @override
  Widget build(BuildContext context) {
    final palabra = Column(
      crossAxisAlignment: vertical ? CrossAxisAlignment.center : CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Image.asset('assets/marca/aurum-palabra.png', height: alto * .36, semanticLabel: 'Aurum'),
        SizedBox(height: alto * .12),
        Text('CONDOMINIOS',
            style: GoogleFonts.montserrat(
                fontSize: alto * .2, fontWeight: FontWeight.w600, letterSpacing: alto * .07, color: Aurum.blanco)),
      ],
    );
    final isotipo = Image.asset('assets/marca/aurum-isotipo.png', height: alto, semanticLabel: 'Aurum Condominios');
    return vertical
        ? Column(mainAxisSize: MainAxisSize.min, children: [isotipo, SizedBox(height: alto * .3), palabra])
        : Row(mainAxisSize: MainAxisSize.min, children: [isotipo, SizedBox(width: alto * .28), palabra]);
  }
}

/// Encabezado azul noche (plano, como la web): rótulo dorado, título y
/// descripción opcional, con el isotipo como marca de agua.
class EncabezadoAurum extends StatelessWidget {
  final String rotulo;
  final Widget titulo;
  final String? descripcion;
  final Widget? accion;
  final Widget? pie;

  const EncabezadoAurum({
    super.key,
    required this.rotulo,
    required this.titulo,
    this.descripcion,
    this.accion,
    this.pie,
  });

  @override
  Widget build(BuildContext context) {
    final oscuro = Theme.of(context).brightness == Brightness.dark;
    return Container(
      width: double.infinity,
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        color: Aurum.noche,
        borderRadius: BorderRadius.circular(24),
        border: oscuro ? Border.all(color: Aurum.oroClaro.withValues(alpha: .15)) : null,
      ),
      child: Stack(
        children: [
          Positioned(
            right: -10,
            bottom: -22,
            child: Opacity(opacity: .07, child: Image.asset('assets/marca/aurum-isotipo.png', height: 120)),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(22, 22, 16, 24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(child: Rotulo(rotulo, color: Aurum.oroClaro)),
                    ?accion,
                  ],
                ),
                const SizedBox(height: 8),
                DefaultTextStyle(
                  style: GoogleFonts.montserrat(
                      fontSize: 28, fontWeight: FontWeight.w800, letterSpacing: -.5, color: Aurum.blanco, height: 1.15),
                  child: titulo,
                ),
                if (descripcion != null) ...[
                  const SizedBox(height: 6),
                  Text(descripcion!,
                      style: GoogleFonts.montserrat(fontSize: 14, color: Aurum.blanco.withValues(alpha: .74), height: 1.45)),
                ],
                if (pie != null) ...[const SizedBox(height: 14), pie!],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Rótulo pequeño en mayúsculas espaciadas ("PORTERÍA", "MI HOGAR"…).
class Rotulo extends StatelessWidget {
  final String texto;
  final Color? color;
  const Rotulo(this.texto, {super.key, this.color});

  @override
  Widget build(BuildContext context) {
    final oscuro = Theme.of(context).brightness == Brightness.dark;
    return Text(texto.toUpperCase(),
        style: GoogleFonts.montserrat(
            fontSize: 11,
            fontWeight: FontWeight.w700,
            letterSpacing: 2.4,
            color: color ?? (oscuro ? Aurum.oroClaro : Aurum.oroTexto)));
  }
}

/// Texto oro con degradado (para resaltar, ej. el nombre en el saludo).
class TextoOro extends StatelessWidget {
  final String texto;
  const TextoOro(this.texto, {super.key});

  @override
  Widget build(BuildContext context) => ShaderMask(
        shaderCallback: (r) => Aurum.degradadoOro.createShader(r),
        blendMode: BlendMode.srcIn,
        child: Text(texto),
      );
}

/// Número de apartamento: etiqueta azul noche con texto oro (como la web).
class ChipApto extends StatelessWidget {
  final String numero;
  const ChipApto(this.numero, {super.key});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(color: Aurum.noche, borderRadius: BorderRadius.circular(8)),
        child: Text(numero,
            style: GoogleFonts.ibmPlexMono(fontSize: 13, fontWeight: FontWeight.w600, color: Aurum.oroClaro)),
      );
}

/// Placa de vehículo: amarilla con borde azul noche, como una placa real.
class ChipPlaca extends StatelessWidget {
  final String placa;
  const ChipPlaca(this.placa, {super.key});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
        decoration: BoxDecoration(
          color: Aurum.oroClaro,
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: Aurum.noche, width: 1.5),
        ),
        child: Text(placa,
            style: GoogleFonts.ibmPlexMono(
                fontSize: 13, fontWeight: FontWeight.w600, letterSpacing: 1.2, color: Aurum.noche)),
      );
}

/// Acceso a un módulo. Si [proximamente] es true se ve, pero indica en qué
/// fase llega (la app se arma por fases, igual que la web).
class TarjetaAccion extends StatelessWidget {
  final IconData icono;
  final String titulo;
  final String subtitulo;
  final String? proximamente;
  final VoidCallback? alTocar;

  const TarjetaAccion({
    super.key,
    required this.icono,
    required this.titulo,
    required this.subtitulo,
    this.proximamente,
    this.alTocar,
  });

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    final oscuro = tema.brightness == Brightness.dark;
    final deshabilitada = proximamente != null;
    return Card(
      child: InkWell(
        borderRadius: BorderRadius.circular(20),
        onTap: deshabilitada
            ? () => ScaffoldMessenger.of(context)
                .showSnackBar(SnackBar(content: Text('$titulo llega a la app en la $proximamente.')))
            : alTocar,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 42,
                    height: 42,
                    decoration: BoxDecoration(
                      color: oscuro ? Aurum.lider : Aurum.oroSuave,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(icono, size: 22, color: oscuro ? Aurum.oroClaro : Aurum.oroTexto),
                  ),
                  const Spacer(),
                  if (deshabilitada)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: tema.colorScheme.surfaceContainerHighest,
                        borderRadius: BorderRadius.circular(99),
                        border: Border.all(color: tema.colorScheme.outline),
                      ),
                      child: Text('Próximamente',
                          style: GoogleFonts.montserrat(fontSize: 10, fontWeight: FontWeight.w700, color: Aurum.grisTenue)),
                    ),
                ],
              ),
              const SizedBox(height: 14),
              Text(titulo,
                  style: GoogleFonts.montserrat(
                      fontSize: 15, fontWeight: FontWeight.w700, color: tema.colorScheme.onSurface)),
              const SizedBox(height: 2),
              Text(subtitulo, style: GoogleFonts.montserrat(fontSize: 12.5, color: Aurum.grisTenue)),
            ],
          ),
        ),
      ),
    );
  }
}

/// Tarjeta de sección con rótulo y título.
class SeccionAurum extends StatelessWidget {
  final String rotulo;
  final String titulo;
  final Widget hijo;
  final Widget? contador;

  const SeccionAurum({super.key, required this.rotulo, required this.titulo, required this.hijo, this.contador});

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Rotulo(rotulo),
            const SizedBox(height: 4),
            Row(children: [
              Text(titulo,
                  style: GoogleFonts.montserrat(
                      fontSize: 18, fontWeight: FontWeight.w800, color: tema.colorScheme.onSurface)),
              if (contador != null) ...[const SizedBox(width: 8), contador!],
            ]),
            const SizedBox(height: 14),
            hijo,
          ],
        ),
      ),
    );
  }
}

class Contador extends StatelessWidget {
  final int valor;
  const Contador(this.valor, {super.key});

  @override
  Widget build(BuildContext context) {
    final oscuro = Theme.of(context).brightness == Brightness.dark;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 2),
      decoration: BoxDecoration(
          color: oscuro ? Aurum.lider : Aurum.oroSuave, borderRadius: BorderRadius.circular(99)),
      child: Text('$valor',
          style: GoogleFonts.montserrat(
              fontSize: 12.5, fontWeight: FontWeight.w700, color: oscuro ? Aurum.oroClaro : Aurum.oroTexto)),
    );
  }
}

class EstadoVacio extends StatelessWidget {
  final IconData icono;
  final String texto;
  const EstadoVacio({super.key, required this.icono, required this.texto});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 18),
        child: Center(
          child: Column(children: [
            Icon(icono, size: 30, color: Aurum.oro),
            const SizedBox(height: 8),
            Text(texto,
                textAlign: TextAlign.center,
                style: GoogleFonts.montserrat(fontSize: 13.5, color: Aurum.grisTenue)),
          ]),
        ),
      );
}
