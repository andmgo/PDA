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
