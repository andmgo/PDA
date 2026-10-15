/// Fechas como las muestra Aurum (en español, sin depender del idioma del
/// teléfono) y como las espera el API (AAAA-MM-DD).
library;

const _dias = ['lun', 'mar', 'mié', 'jue', 'vie', 'sáb', 'dom'];
const _meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sept', 'oct', 'nov', 'dic'];

DateTime soloFecha(DateTime d) => DateTime(d.year, d.month, d.day);

String fechaIso(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

DateTime? leerIso(Object? texto) {
  if (texto == null) return null;
  final t = '$texto';
  return t.length >= 10 ? DateTime.tryParse(t.substring(0, 10)) : null;
}

/// "jue 15 oct"
String fechaCorta(DateTime d) => '${_dias[d.weekday - 1]} ${d.day} ${_meses[d.month - 1]}';

/// "jue 15 oct" o "jue 15 oct → vie 16 oct"
String rangoFechas(DateTime desde, DateTime hasta) =>
    soloFecha(desde) == soloFecha(hasta) ? fechaCorta(desde) : '${fechaCorta(desde)} → ${fechaCorta(hasta)}';

/// "2026-10-15 11:40:47" → "11:40"
String horaDe(Object? fechaHora) {
  final t = '${fechaHora ?? ''}';
  return t.length >= 16 ? t.substring(11, 16) : '';
}

/// "2026-10-15T16:20:44.123" (LocalDateTime del API) → DateTime local.
DateTime? leerFechaHora(Object? texto) => texto == null ? null : DateTime.tryParse('$texto');

/// "jue 15 oct · 16:20"
String fechaHoraCorta(DateTime d) =>
    '${fechaCorta(d)} · ${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';

/// "hace 5 min", "hace 3 h", "hace 2 días" (cuánto lleva esperando).
String haceCuanto(DateTime d, [DateTime? ahora]) {
  final min = (ahora ?? DateTime.now()).difference(d).inMinutes.clamp(0, 1 << 30);
  if (min < 60) return 'hace $min min';
  final h = (min / 60).round();
  if (h < 24) return 'hace $h h';
  final dias = (h / 24).round();
  return 'hace $dias día${dias == 1 ? '' : 's'}';
}
