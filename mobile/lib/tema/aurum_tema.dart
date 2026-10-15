import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Paleta del Manual de Marca de Aurum Condominios (misma que la web,
/// ver web/static/css/gescazone-theme.css). Solo azules de marca, oro y
/// neutros; el rojo apagado se reserva para acciones destructivas.
class Aurum {
  Aurum._();

  static const noche = Color(0xFF1E2A4A); // 60 % — fondos y estructura
  static const lider = Color(0xFF2E3F6F); // 30 % — tarjetas y UI
  static const oro = Color(0xFFD4A843); // 10 % — acentos
  static const oroClaro = Color(0xFFF0C96A); // acentos sobre azul noche
  static const oroTexto = Color(0xFF9A7420); // oro legible sobre fondo claro
  static const oroSuave = Color(0xFFFBF3DF);
  static const blanco = Color(0xFFF8F7F4); // blanco cálido
  static const gris = Color(0xFF4A4A4A); // texto
  static const grisTenue = Color(0xFF8B8F9C);
  static const borde = Color(0xFFE8E4DA);
  static const rojoEstado = Color(0xFFB23A3A); // solo destructivo / error crítico

  // Modo oscuro
  static const fondoOscuro = Color(0xFF111A30);
  static const superficieOscura = Color(0xFF1E2A4A);
  static const superficieOscura2 = Color(0xFF24325A);

  static const degradadoOro = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFFF0C96A), Color(0xFFD4A843), Color(0xFFB88A2C)],
    stops: [0, .55, 1],
  );
}

ThemeData temaAurum(Brightness brillo) {
  final oscuro = brillo == Brightness.dark;
  final esquema = ColorScheme(
    brightness: brillo,
    primary: oscuro ? Aurum.oro : Aurum.lider,
    onPrimary: oscuro ? Aurum.noche : Aurum.blanco,
    secondary: Aurum.oro,
    onSecondary: Aurum.noche,
    error: Aurum.rojoEstado,
    onError: Colors.white,
    surface: oscuro ? Aurum.superficieOscura : Colors.white,
    onSurface: oscuro ? Aurum.blanco : Aurum.noche,
    surfaceContainerHighest: oscuro ? Aurum.superficieOscura2 : const Color(0xFFFBFAF7),
    outline: oscuro ? Aurum.lider : Aurum.borde,
  );

  final texto = GoogleFonts.montserratTextTheme(
    oscuro ? ThemeData.dark().textTheme : ThemeData.light().textTheme,
  ).apply(
    bodyColor: oscuro ? const Color(0xFFC3C7D3) : Aurum.gris,
    displayColor: oscuro ? Aurum.blanco : Aurum.noche,
  );

  final bordeCampo = OutlineInputBorder(
    borderRadius: BorderRadius.circular(12),
    borderSide: BorderSide(color: esquema.outline, width: 1.5),
  );

  return ThemeData(
    useMaterial3: true,
    colorScheme: esquema,
    scaffoldBackgroundColor: oscuro ? Aurum.fondoOscuro : Aurum.blanco,
    textTheme: texto,
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: oscuro ? Aurum.superficieOscura2 : Colors.white,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      border: bordeCampo,
      enabledBorder: bordeCampo,
      focusedBorder: bordeCampo.copyWith(borderSide: const BorderSide(color: Aurum.oro, width: 2)),
      labelStyle: TextStyle(color: oscuro ? const Color(0xFFC3C7D3) : Aurum.gris),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: oscuro ? Aurum.oro : Aurum.noche,
        foregroundColor: oscuro ? Aurum.noche : Aurum.blanco,
        minimumSize: const Size.fromHeight(54),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        textStyle: GoogleFonts.montserrat(fontSize: 15.5, fontWeight: FontWeight.w700),
      ),
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      margin: EdgeInsets.zero,
      color: oscuro ? Aurum.superficieOscura : Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: BorderSide(color: esquema.outline),
      ),
    ),
    snackBarTheme: const SnackBarThemeData(behavior: SnackBarBehavior.floating),
  );
}
