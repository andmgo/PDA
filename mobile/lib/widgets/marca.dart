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
        border: Border.all(color: oscuro ? Aurum.filoOscuro : Colors.transparent),
      ),
      child: Stack(
        children: [
          // Marca de agua: centrada en la altura y completa (antes iba con
          // bottom negativo y la mitad inferior del isotipo quedaba recortada).
          Positioned(
            right: 14,
            top: 0,
            bottom: 0,
            child: Center(
              child: Opacity(opacity: .07, child: Image.asset('assets/marca/aurum-isotipo.png', height: 84)),
            ),
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
/// En modo oscuro lleva un filo dorado tenue para separarse de la tarjeta.
class ChipApto extends StatelessWidget {
  final String numero;
  const ChipApto(this.numero, {super.key});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: Aurum.noche,
          borderRadius: BorderRadius.circular(8),
          border: Theme.of(context).brightness == Brightness.dark ? Border.all(color: Aurum.filoOscuro) : null,
        ),
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
                      color: oscuro ? Aurum.hundido : Aurum.oroSuave,
                      borderRadius: BorderRadius.circular(12),
                      border: oscuro ? Border.all(color: Aurum.filoOscuro) : null,
                    ),
                    child: Icon(icono, size: 22, color: oscuro ? Aurum.oroClaro : Aurum.oroTexto),
                  ),
                  const SizedBox(width: 8),
                  // Flexible + scaleDown: en celulares angostos la tarjeta mide
                  // ~150 px y ícono + etiqueta no cabían ("right overflowed").
                  if (deshabilitada)
                    Flexible(
                      child: Align(
                        alignment: Alignment.topRight,
                        child: FittedBox(
                          fit: BoxFit.scaleDown,
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              color: tema.colorScheme.surfaceContainerHighest,
                              borderRadius: BorderRadius.circular(99),
                              border: Border.all(color: tema.colorScheme.outline),
                            ),
                            child: Text('Próximamente',
                                style: GoogleFonts.montserrat(
                                    fontSize: 10, fontWeight: FontWeight.w700, color: Aurum.grisTenue)),
                          ),
                        ),
                      ),
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
              Flexible(
                child: Text(titulo,
                    style: GoogleFonts.montserrat(
                        fontSize: 18, fontWeight: FontWeight.w800, color: tema.colorScheme.onSurface)),
              ),
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
          color: oscuro ? Aurum.hundido : Aurum.oroSuave,
          borderRadius: BorderRadius.circular(99),
          border: oscuro ? Border.all(color: Aurum.filoOscuro) : null),
      child: Text('$valor',
          style: GoogleFonts.montserrat(
              fontSize: 12.5, fontWeight: FontWeight.w700, color: oscuro ? Aurum.oroClaro : Aurum.oroTexto)),
    );
  }
}

/// Aviso dentro de un formulario: fondo oro suave con borde izquierdo (oro
/// para errores y avisos, azul líder para confirmaciones). Sin rojo ni verde.
class AvisoAurum extends StatelessWidget {
  final String texto;
  final bool error;
  final Widget? contenido;
  const AvisoAurum(this.texto, {super.key, this.error = true, this.contenido});

  @override
  Widget build(BuildContext context) {
    final oscuro = Theme.of(context).brightness == Brightness.dark;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
      decoration: BoxDecoration(
        color: oscuro ? Aurum.superficieOscura2 : (error ? Aurum.oroSuave : const Color(0xFFEEF0F6)),
        borderRadius: BorderRadius.circular(10),
        border: Border(left: BorderSide(color: error ? Aurum.oro : (oscuro ? Aurum.oroClaro : Aurum.lider), width: 3)),
      ),
      child: contenido ??
          Text(texto,
              style: GoogleFonts.montserrat(fontSize: 13, height: 1.4, color: oscuro ? Aurum.blanco : Aurum.noche)),
    );
  }
}

/// Etiqueta de estado ("Hoy", "Programada", "Ya ingresó"…).
class EtiquetaEstado extends StatelessWidget {
  final String texto;
  final bool destacada;
  final bool apagada;
  const EtiquetaEstado(this.texto, {super.key, this.destacada = false, this.apagada = false});

  @override
  Widget build(BuildContext context) {
    final oscuro = Theme.of(context).brightness == Brightness.dark;
    final fondo = destacada
        ? Aurum.noche
        : apagada
            ? Theme.of(context).colorScheme.surfaceContainerHighest
            : (oscuro ? Aurum.hundido : Aurum.oroSuave);
    final color = destacada ? Aurum.oroClaro : apagada ? Aurum.grisTenue : (oscuro ? Aurum.oroClaro : Aurum.oroTexto);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 3),
      decoration: BoxDecoration(
        color: fondo,
        borderRadius: BorderRadius.circular(99),
        border: oscuro && !apagada ? Border.all(color: destacada ? Aurum.filoDorado : Aurum.filoOscuro) : null,
      ),
      child: Text(texto.toUpperCase(),
          style: GoogleFonts.montserrat(fontSize: 10.5, fontWeight: FontWeight.w800, letterSpacing: .6, color: color)),
    );
  }
}

/// Círculo azul noche con las iniciales en oro (como en la web).
class AvatarIniciales extends StatelessWidget {
  final String nombre;
  final bool apagado;
  const AvatarIniciales(this.nombre, {super.key, this.apagado = false});

  @override
  Widget build(BuildContext context) {
    final iniciales = nombre.split(' ').where((p) => p.isNotEmpty).take(2).map((p) => p[0]).join().toUpperCase();
    final oscuro = Theme.of(context).brightness == Brightness.dark;
    return Container(
      width: 40,
      height: 40,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: apagado ? Aurum.grisTenue : Aurum.noche,
        border: oscuro && !apagado ? Border.all(color: Aurum.oroClaro.withValues(alpha: .35)) : null,
      ),
      child: Text(iniciales,
          style: GoogleFonts.montserrat(
              fontWeight: FontWeight.w800, fontSize: 13, color: apagado ? Aurum.blanco : Aurum.oroClaro)),
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
