/// Extrae placas colombianas del texto que lee la cámara (OCR).
///
/// Formatos (los mismos que valida el API en VehiculoModel):
///   carro: ABC123   ·   moto: ABC12 o ABC12D
/// El OCR confunde letras y números parecidos; como la posición dice qué
/// debe ir (3 letras y luego números), se corrige por posición:
/// "A8C123" → "ABC123", "ABC1Z3" → "ABC123".
library;

const _aLetra = {'0': 'O', '1': 'I', '2': 'Z', '4': 'A', '5': 'S', '6': 'G', '8': 'B'};
const _aNumero = {'O': '0', 'Q': '0', 'D': '0', 'I': '1', 'L': '1', 'Z': '2', 'S': '5', 'G': '6', 'B': '8', 'T': '7'};

final _carro = RegExp(r'^[A-Z]{3}\d{3}$');
final _moto = RegExp(r'^[A-Z]{3}\d{2}[A-Z]?$');

/// Solo letras y números, en mayúscula ("abc-123" → "ABC123").
String normalizarPlaca(String texto) => texto.toUpperCase().replaceAll(RegExp('[^A-Z0-9]'), '');

bool esPlacaValida(String placa) => _carro.hasMatch(placa) || _moto.hasMatch(placa);

/// Corrige por posición un trozo de 5-6 caracteres; null si no da una placa.
String? _corregir(String trozo) {
  if (trozo.length < 5 || trozo.length > 6) return null;
  // Solo se corrige si en la zona de números ya hay al menos un dígito real:
  // sin esto, palabras como "BOGOTA" se volvían placas ("BOG07A").
  if (!RegExp(r'\d').hasMatch(trozo.substring(3, 5))) return null;
  final b = StringBuffer();
  for (var i = 0; i < trozo.length; i++) {
    final c = trozo[i];
    if (i < 3) {
      b.write(_aLetra[c] ?? c); // letras
    } else if (i < 5) {
      b.write(_aNumero[c] ?? c); // números
    } else {
      // 6.º carácter: número (carro) o letra (moto). Se deja como viene.
      b.write(c);
    }
  }
  final p = b.toString();
  if (_carro.hasMatch(p) || _moto.hasMatch(p)) return p;
  // Carro con el último número leído como letra ("ABC12O" → "ABC120").
  final comoCarro = p.substring(0, 5) + (_aNumero[p.length == 6 ? p[5] : ''] ?? '');
  return _carro.hasMatch(comoCarro) ? comoCarro : null;
}

/// Placas posibles en el texto del OCR, de la más probable a la menos.
/// Prefiere líneas que por sí solas son una placa (en la placa física el
/// número va en su propia línea, y abajo el nombre de la ciudad).
List<String> placasEnTexto(String textoOcr) {
  final candidatas = <String>[];
  void agregar(String? p) {
    if (p != null && !candidatas.contains(p)) candidatas.add(p);
  }

  final lineas = textoOcr.split('\n').map(normalizarPlaca).where((l) => l.isNotEmpty).toList();
  // 1) La línea completa es una placa (lo más confiable).
  for (final l in lineas) {
    agregar(_corregir(l));
  }
  // 2) Una placa dentro de una línea más larga ("PLACAABC123"). Aquí solo
  // coincidencias exactas: corregir dentro de un texto largo inventa placas.
  for (final l in lineas.where((l) => l.length > 6)) {
    for (final largo in [6, 5]) {
      for (var i = 0; i + largo <= l.length; i++) {
        final trozo = l.substring(i, i + largo);
        if (esPlacaValida(trozo)) agregar(trozo);
      }
    }
  }
  return candidatas;
}

/// Confirma una placa solo cuando la cámara la lee igual varias veces
/// seguidas (una lectura suelta puede venir de un reflejo o un cartel).
class EstabilizadorPlaca {
  final int lecturasNecesarias;
  String? _ultima;
  int _veces = 0;

  EstabilizadorPlaca({this.lecturasNecesarias = 2});

  /// Devuelve la placa cuando quedó confirmada; null mientras tanto.
  String? agregar(List<String> candidatas) {
    if (candidatas.isEmpty) return null; // un cuadro sin texto no reinicia la cuenta
    final placa = candidatas.first;
    _veces = placa == _ultima ? _veces + 1 : 1;
    _ultima = placa;
    return _veces >= lecturasNecesarias ? placa : null;
  }

  void reiniciar() {
    _ultima = null;
    _veces = 0;
  }
}
