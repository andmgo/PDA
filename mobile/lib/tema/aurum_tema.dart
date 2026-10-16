import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Paleta del Manual de Marca de Aurum Condominios (misma que la web,
/// ver web/static/css/gescazone-theme.css). Solo azules de marca, oro y
/// neutros; el rojo apagado se reserva para acciones destructivas.
class Aurum {
  Aurum._();

  static const noche = Color(0xFF1E2A4A); // 60 % — fondos y estructura
  static const lider = Color(0xFF2E3F6F); // 30 % — solo botones/UI en modo claro
  static const oro = Color(0xFFD4A843); // 10 % — acentos
  static const oroClaro = Color(0xFFF0C96A); // acentos sobre azul noche
  static const oroTexto = Color(0xFF9A7420); // oro legible sobre fondo claro
  static const oroSuave = Color(0xFFFBF3DF);
  static const blanco = Color(0xFFF8F7F4); // blanco cálido
  static const gris = Color(0xFF4A4A4A); // texto
  static const grisTenue = Color(0xFF8B8F9C);
  static const borde = Color(0xFFE8E4DA);
  static const rojoEstado = Color(0xFFB23A3A); // solo destructivo / error crítico

  // Modo oscuro. Antes las tarjetas eran azul noche y los rellenos azul
  // líder (#2E3F6F): junto a la web se veía "un azul más claro". Ahora la
  // estructura (barras, encabezado, chips) es azul noche y las tarjetas un
  // tono más profundo; el contraste lo dan el oro translúcido y un filo
  // dorado tenue, nunca otro azul más claro.
  static const fondoOscuro = Color(0xFF111A30);
  static const tarjetaOscura = Color(0xFF172139);
  static const campoOscuro = Color(0xFF1B2643);
  static const superficieOscura = noche;
  static const superficieOscura2 = campoOscuro;
  /// Relleno "hundido" de íconos, contadores y selecciones en modo oscuro:
  /// el azul más profundo con filo dorado. (El oro translúcido sobre azul
  /// daba un café grisáceo opaco.)
  static const hundido = fondoOscuro;
  static const filoOscuro = Color(0x2EF0C96A); // oro claro al 18 %: bordes finos
  static const filoDorado = Color(0x73F0C96A); // oro claro al 45 %: selección

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
    primary: oscuro ? Aurum.oro : Aurum.noche,
    onPrimary: oscuro ? Aurum.noche : Aurum.blanco,
    secondary: Aurum.oro,
    onSecondary: Aurum.noche,
    error: Aurum.rojoEstado,
    onError: Colors.white,
    surface: oscuro ? Aurum.tarjetaOscura : Colors.white,
    onSurface: oscuro ? Aurum.blanco : Aurum.noche,
    surfaceContainerHighest: oscuro ? Aurum.campoOscuro : const Color(0xFFF3F1EC),
    surfaceTint: Colors.transparent, // sin el velo de color de Material 3
    outline: oscuro ? Aurum.filoOscuro : Aurum.borde,
    outlineVariant: oscuro ? Aurum.filoOscuro : Aurum.borde,
  );

  final texto = GoogleFonts.montserratTextTheme(
    oscuro ? ThemeData.dark().textTheme : ThemeData.light().textTheme,
  ).apply(
    bodyColor: oscuro ? const Color(0xFFC3C7D3) : Aurum.gris,
    displayColor: oscuro ? Aurum.blanco : Aurum.noche,
  );

  final bordeCampo = OutlineInputBorder(
    borderRadius: BorderRadius.circular(12),
    borderSide: BorderSide(color: esquema.outline, width: 1.2),
  );
  final tituloBarra = GoogleFonts.montserrat(fontSize: 18, fontWeight: FontWeight.w700, letterSpacing: -.2, color: Aurum.blanco);
  final etiquetaPequena = GoogleFonts.montserrat(fontSize: 11.5, fontWeight: FontWeight.w600, letterSpacing: .1);

  return ThemeData(
    useMaterial3: true,
    colorScheme: esquema,
    scaffoldBackgroundColor: oscuro ? Aurum.fondoOscuro : Aurum.blanco,
    textTheme: texto,
    // Estructura en azul noche plano, como la barra lateral y el encabezado de la web.
    appBarTheme: AppBarTheme(
      backgroundColor: Aurum.noche,
      foregroundColor: Aurum.blanco,
      elevation: 0,
      scrolledUnderElevation: 0,
      surfaceTintColor: Colors.transparent,
      titleTextStyle: tituloBarra,
      iconTheme: const IconThemeData(color: Aurum.blanco),
      actionsIconTheme: const IconThemeData(color: Aurum.oroClaro),
    ),
    tabBarTheme: TabBarThemeData(
      labelColor: Aurum.oroClaro,
      unselectedLabelColor: Aurum.blanco.withValues(alpha: .7),
      indicatorColor: Aurum.oro,
      dividerColor: Colors.transparent,
      labelStyle: GoogleFonts.montserrat(fontWeight: FontWeight.w700, fontSize: 13.5),
      unselectedLabelStyle: GoogleFonts.montserrat(fontWeight: FontWeight.w600, fontSize: 13.5),
    ),
    navigationBarTheme: NavigationBarThemeData(
      backgroundColor: Aurum.noche,
      surfaceTintColor: Colors.transparent,
      elevation: 0,
      height: 68,
      indicatorColor: Aurum.hundido,
      indicatorShape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12), side: const BorderSide(color: Aurum.filoDorado)),
      iconTheme: WidgetStateProperty.resolveWith((estado) => IconThemeData(
          size: 23,
          color: estado.contains(WidgetState.selected) ? Aurum.oroClaro : Aurum.blanco.withValues(alpha: .62))),
      labelTextStyle: WidgetStateProperty.resolveWith((estado) => etiquetaPequena.copyWith(
          color: estado.contains(WidgetState.selected) ? Aurum.oroClaro : Aurum.blanco.withValues(alpha: .62))),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: oscuro ? Aurum.campoOscuro : Colors.white,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      border: bordeCampo,
      enabledBorder: bordeCampo,
      focusedBorder: bordeCampo.copyWith(borderSide: const BorderSide(color: Aurum.oro, width: 1.8)),
      labelStyle: TextStyle(color: oscuro ? const Color(0xFFC3C7D3) : Aurum.gris),
      floatingLabelStyle: TextStyle(color: oscuro ? Aurum.oroClaro : Aurum.oroTexto, fontWeight: FontWeight.w600),
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
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: oscuro ? Aurum.oroClaro : Aurum.noche,
        side: BorderSide(color: oscuro ? Aurum.oro.withValues(alpha: .45) : Aurum.borde, width: 1.2),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        textStyle: GoogleFonts.montserrat(fontSize: 14.5, fontWeight: FontWeight.w700),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        foregroundColor: oscuro ? Aurum.oroClaro : Aurum.oroTexto,
        textStyle: GoogleFonts.montserrat(fontSize: 14, fontWeight: FontWeight.w700),
      ),
    ),
    // Selector segmentado: el elegido en azul noche con texto oro (claro) o
    // con filo dorado (oscuro); antes era un bloque de oro.
    segmentedButtonTheme: SegmentedButtonThemeData(
      style: ButtonStyle(
        backgroundColor: WidgetStateProperty.resolveWith((e) => e.contains(WidgetState.selected)
            ? Aurum.noche
            : Colors.transparent),
        foregroundColor: WidgetStateProperty.resolveWith((e) => e.contains(WidgetState.selected)
            ? Aurum.oroClaro
            : (oscuro ? Aurum.blanco.withValues(alpha: .75) : Aurum.noche)),
        side: WidgetStateProperty.resolveWith((e) => BorderSide(
            color: e.contains(WidgetState.selected) && oscuro
                ? Aurum.filoDorado
                : (oscuro ? Aurum.filoOscuro : Aurum.borde))),
        textStyle: WidgetStatePropertyAll(GoogleFonts.montserrat(fontWeight: FontWeight.w700, fontSize: 13.5)),
        minimumSize: const WidgetStatePropertyAll(Size(0, 44)),
      ),
    ),
    chipTheme: ChipThemeData(
      backgroundColor: oscuro ? Aurum.campoOscuro : Colors.white,
      selectedColor: Aurum.noche,
      side: BorderSide(color: oscuro ? Aurum.filoOscuro : Aurum.borde),
      labelStyle: GoogleFonts.montserrat(fontWeight: FontWeight.w600, color: oscuro ? Aurum.blanco : Aurum.noche),
      secondaryLabelStyle: GoogleFonts.montserrat(fontWeight: FontWeight.w700, color: Aurum.oroClaro),
      checkmarkColor: Aurum.oroClaro,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
    ),
    cardTheme: CardThemeData(
      elevation: 0,
      margin: EdgeInsets.zero,
      color: oscuro ? Aurum.tarjetaOscura : Colors.white,
      surfaceTintColor: Colors.transparent,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
        side: BorderSide(color: esquema.outline),
      ),
    ),
    dialogTheme: DialogThemeData(
      backgroundColor: oscuro ? Aurum.tarjetaOscura : Colors.white,
      surfaceTintColor: Colors.transparent,
      shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20), side: BorderSide(color: esquema.outline)),
      titleTextStyle: GoogleFonts.montserrat(
          fontSize: 19, fontWeight: FontWeight.w800, color: oscuro ? Aurum.blanco : Aurum.noche),
    ),
    datePickerTheme: DatePickerThemeData(
      backgroundColor: oscuro ? Aurum.tarjetaOscura : Colors.white,
      surfaceTintColor: Colors.transparent,
      headerBackgroundColor: Aurum.noche,
      headerForegroundColor: Aurum.blanco,
      todayBorder: const BorderSide(color: Aurum.oro),
    ),
    timePickerTheme: TimePickerThemeData(
      backgroundColor: oscuro ? Aurum.tarjetaOscura : Colors.white,
      dialHandColor: Aurum.oro,
    ),
    floatingActionButtonTheme: const FloatingActionButtonThemeData(
      backgroundColor: Aurum.noche,
      foregroundColor: Aurum.oroClaro,
      elevation: 2,
    ),
    dividerTheme: DividerThemeData(color: esquema.outline, thickness: 1),
    snackBarTheme: SnackBarThemeData(
      behavior: SnackBarBehavior.floating,
      backgroundColor: Aurum.noche,
      contentTextStyle: GoogleFonts.montserrat(color: Aurum.blanco, fontSize: 13.5),
      shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12), side: BorderSide(color: Aurum.oroClaro.withValues(alpha: .25))),
    ),
  );
}
