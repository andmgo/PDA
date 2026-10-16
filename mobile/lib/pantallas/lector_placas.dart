import 'dart:async';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

import '../datos/api.dart';
import '../datos/fechas.dart';
import '../datos/placas.dart';
import '../tema/aurum_tema.dart';
import '../widgets/marca.dart';
import 'porteria.dart';

/// Lector de placas de portería (Fase 8). La cámara lee el texto en el
/// celular (ML Kit, sin internet y sin enviar la imagen); cuando la misma
/// placa se lee dos veces seguidas se consulta GET
/// /api/accesos/visitas/placa/{placa} y se muestra qué hacer:
/// residente, visitante que sale, visita autorizada o placa desconocida.
/// Siempre se puede escribir la placa a mano (sin cámara o sin permiso).
class PantallaLectorPlacas extends StatefulWidget {
  final Api api;

  /// false en las pruebas: sin cámara, solo la búsqueda manual.
  final bool usarCamara;
  const PantallaLectorPlacas({super.key, required this.api, this.usarCamara = true});

  @override
  State<PantallaLectorPlacas> createState() => _PantallaLectorPlacasState();
}

class _PantallaLectorPlacasState extends State<PantallaLectorPlacas> with WidgetsBindingObserver {
  CameraController? _camara;
  CameraDescription? _descripcion;
  // Solo con cámara (en las pruebas no existe el plugin de ML Kit).
  late final TextRecognizer _ocr = TextRecognizer(script: TextRecognitionScript.latin);
  final _estabilizador = EstabilizadorPlaca();
  final _manual = TextEditingController();

  String? _avisoCamara; // sin permiso, sin cámara…
  bool _procesando = false;
  DateTime _ultimoCuadro = DateTime.fromMillisecondsSinceEpoch(0);
  bool _linterna = false;
  String? _vista; // última placa candidata (se muestra mientras se confirma)

  // Resultado
  bool _consultando = false;
  Map<String, dynamic>? _lectura;
  String? _error;
  String? _mensaje;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    if (widget.usarCamara) _iniciarCamara();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _camara?.dispose();
    if (widget.usarCamara) _ocr.close();
    _manual.dispose();
    super.dispose();
  }

  /// La cámara se suelta al salir de la app y se retoma al volver.
  @override
  void didChangeAppLifecycleState(AppLifecycleState estado) {
    if (!widget.usarCamara) return;
    if (estado == AppLifecycleState.inactive) {
      _camara?.dispose();
      _camara = null;
    } else if (estado == AppLifecycleState.resumed && _camara == null) {
      _iniciarCamara();
    }
  }

  Future<void> _iniciarCamara() async {
    try {
      final camaras = await availableCameras();
      _descripcion = camaras.firstWhere((c) => c.lensDirection == CameraLensDirection.back,
          orElse: () => camaras.first);
      final control = CameraController(_descripcion!, ResolutionPreset.high,
          enableAudio: false, imageFormatGroup: ImageFormatGroup.nv21);
      await control.initialize();
      if (!mounted) return control.dispose();
      setState(() {
        _camara = control;
        _avisoCamara = null;
      });
      if (_lectura == null) await _reanudar();
    } on CameraException catch (e) {
      if (mounted) {
        setState(() => _avisoCamara = e.code.contains('Denied') || e.code.contains('denied')
            ? 'Sin permiso para usar la cámara. Actívalo en Ajustes, o escribe la placa abajo.'
            : 'No se pudo abrir la cámara. Escribe la placa abajo.');
      }
    } on StateError {
      if (mounted) setState(() => _avisoCamara = 'Este equipo no tiene cámara. Escribe la placa abajo.');
    }
  }

  Future<void> _reanudar() async {
    final c = _camara;
    if (c == null || !c.value.isInitialized || c.value.isStreamingImages) return;
    _estabilizador.reiniciar();
    await c.startImageStream(_cuadro);
  }

  Future<void> _pausar() async {
    final c = _camara;
    if (c != null && c.value.isStreamingImages) await c.stopImageStream();
  }

  /// Un cuadro de la cámara: como máximo uno cada 350 ms, y nunca dos a la vez.
  Future<void> _cuadro(CameraImage imagen) async {
    final ahora = DateTime.now();
    if (_procesando || _consultando || ahora.difference(_ultimoCuadro).inMilliseconds < 350) return;
    _procesando = true;
    _ultimoCuadro = ahora;
    try {
      final entrada = _aEntrada(imagen);
      if (entrada == null) return;
      final texto = await _ocr.processImage(entrada);
      final candidatas = placasEnTexto(texto.text);
      if (mounted && candidatas.isNotEmpty && candidatas.first != _vista) {
        setState(() => _vista = candidatas.first);
      }
      final confirmada = _estabilizador.agregar(candidatas);
      if (confirmada != null) await _consultar(confirmada);
    } catch (e) {
      debugPrint('Lector de placas: $e');
    } finally {
      _procesando = false;
    }
  }

  InputImage? _aEntrada(CameraImage imagen) {
    final rotacion = InputImageRotationValue.fromRawValue(_descripcion!.sensorOrientation);
    final formato = InputImageFormatValue.fromRawValue(imagen.format.raw);
    if (rotacion == null || formato == null || imagen.planes.length != 1) return null;
    final plano = imagen.planes.first;
    return InputImage.fromBytes(
      bytes: plano.bytes,
      metadata: InputImageMetadata(
        size: Size(imagen.width.toDouble(), imagen.height.toDouble()),
        rotation: rotacion,
        format: formato,
        bytesPerRow: plano.bytesPerRow,
      ),
    );
  }

  Future<void> _consultar(String placaEscrita) async {
    final placa = normalizarPlaca(placaEscrita);
    if (placa.length < 4) {
      setState(() => _error = 'Escribe la placa completa (ej. ABC123).');
      return;
    }
    await _pausar();
    HapticFeedback.mediumImpact();
    setState(() {
      _consultando = true;
      _error = null;
      _mensaje = null;
      _vista = placa;
    });
    try {
      final r = await widget.api.get('/api/accesos/visitas/placa/$placa');
      if (mounted) setState(() => _lectura = r as Map<String, dynamic>);
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
      await _reanudar();
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    } finally {
      if (mounted) setState(() => _consultando = false);
    }
  }

  Future<void> _otraPlaca() async {
    setState(() {
      _lectura = null;
      _error = null;
      _mensaje = null;
      _vista = null;
      _manual.clear();
    });
    await _reanudar();
  }

  Future<void> _registrarSalida(Map<String, dynamic> visita) async {
    try {
      final m = await widget.api.put('/api/accesos/visitas/${visita['tipo']}/${visita['id']}/salida');
      if (mounted) setState(() => _mensaje = '$m');
    } on ApiError catch (e) {
      if (mounted) setState(() => _error = e.mensaje);
    } on SesionTerminada {
      if (mounted) Navigator.of(context).popUntil((r) => r.isFirst);
    }
  }

  Future<void> _abrirIngreso({Map<String, dynamic>? autorizacion, String? placa}) async {
    await Navigator.of(context).push(MaterialPageRoute(
        builder: (_) => PantallaPorteria(api: widget.api, autorizacionInicial: autorizacion, placaInicial: placa)));
    if (mounted) _otraPlaca();
  }

  Future<void> _alternarLinterna() async {
    final c = _camara;
    if (c == null) return;
    try {
      await c.setFlashMode(_linterna ? FlashMode.off : FlashMode.torch);
      setState(() => _linterna = !_linterna);
    } on CameraException {
      // equipo sin linterna
    }
  }

  // ── Interfaz ───────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Aurum.noche,
      appBar: AppBar(
        title: const Text('Lector de placas'),
        actions: [
          if (_camara != null)
            IconButton(
              tooltip: _linterna ? 'Apagar linterna' : 'Encender linterna',
              onPressed: _alternarLinterna,
              icon: Icon(_linterna ? Icons.flashlight_off_outlined : Icons.flashlight_on_outlined),
            ),
        ],
      ),
      body: Column(children: [
        Expanded(child: _visor()),
        _panel(),
      ]),
    );
  }

  Widget _visor() {
    final c = _camara;
    final listo = c != null && c.value.isInitialized;
    return Stack(fit: StackFit.expand, children: [
      if (listo)
        ClipRect(
          child: FittedBox(
            fit: BoxFit.cover,
            child: SizedBox(
              // La vista previa viene en horizontal; en vertical se invierte.
              width: c.value.previewSize!.height,
              height: c.value.previewSize!.width,
              child: CameraPreview(c),
            ),
          ),
        )
      else
        Center(
          child: Padding(
            padding: const EdgeInsets.all(28),
            child: _avisoCamara != null
                ? Column(mainAxisSize: MainAxisSize.min, children: [
                    const Icon(Icons.no_photography_outlined, color: Aurum.oroClaro, size: 40),
                    const SizedBox(height: 12),
                    Text(_avisoCamara!,
                        textAlign: TextAlign.center,
                        style: GoogleFonts.montserrat(color: Aurum.blanco, fontSize: 14)),
                  ])
                : widget.usarCamara
                    ? const CircularProgressIndicator(color: Aurum.oro)
                    : const SizedBox.shrink(),
          ),
        ),
      if (listo) ...[
        // Recuadro guía con la proporción de una placa
        Center(
          child: FractionallySizedBox(
            widthFactor: .78,
            child: AspectRatio(
              aspectRatio: 2.1,
              child: DecoratedBox(
                decoration: BoxDecoration(
                  border: Border.all(color: _lectura != null ? Aurum.oroClaro : Aurum.oro, width: 3),
                  borderRadius: BorderRadius.circular(14),
                ),
              ),
            ),
          ),
        ),
        Positioned(
          left: 0,
          right: 0,
          top: 16,
          child: Center(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
              decoration: BoxDecoration(color: Aurum.noche.withValues(alpha: .75), borderRadius: BorderRadius.circular(99)),
              child: Text(
                _lectura != null || _consultando
                    ? 'Placa leída'
                    : _vista != null
                        ? 'Leyendo $_vista…'
                        : 'Apunta a la placa',
                style: GoogleFonts.montserrat(color: Aurum.blanco, fontWeight: FontWeight.w600, fontSize: 13),
              ),
            ),
          ),
        ),
      ],
    ]);
  }

  Widget _panel() {
    final tema = Theme.of(context);
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: tema.scaffoldBackgroundColor,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(22)),
      ),
      padding: EdgeInsets.fromLTRB(16, 16, 16, 16 + MediaQuery.of(context).padding.bottom),
      child: AnimatedSize(
        duration: const Duration(milliseconds: 200),
        alignment: Alignment.topCenter,
        child: _consultando
            ? const Padding(
                padding: EdgeInsets.all(24), child: Center(child: CircularProgressIndicator(color: Aurum.oro)))
            : _lectura != null
                ? _resultado(_lectura!)
                : _busquedaManual(),
      ),
    );
  }

  Widget _busquedaManual() => Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
        if (_error != null) ...[AvisoAurum(_error!), const SizedBox(height: 12)],
        Row(children: [
          Expanded(
            child: TextField(
              controller: _manual,
              textCapitalization: TextCapitalization.characters,
              maxLength: 7,
              style: GoogleFonts.ibmPlexMono(fontSize: 18, letterSpacing: 2),
              inputFormatters: [
                FilteringTextInputFormatter.allow(RegExp('[a-zA-Z0-9]')),
                TextInputFormatter.withFunction((_, n) => n.copyWith(text: n.text.toUpperCase())),
              ],
              decoration: const InputDecoration(labelText: 'O escribe la placa', counterText: ''),
              onSubmitted: _consultar,
            ),
          ),
          const SizedBox(width: 10),
          SizedBox(
            height: 54,
            child: FilledButton(
              style: FilledButton.styleFrom(minimumSize: const Size(0, 54)),
              onPressed: () => _consultar(_manual.text),
              child: const Text('Consultar'),
            ),
          ),
        ]),
      ]);

  Widget _resultado(Map<String, dynamic> l) {
    final placa = '${l['placa']}';
    final tipo = '${l['resultado']}';
    final tenue = GoogleFonts.montserrat(fontSize: 13, color: Aurum.grisTenue);
    final titulo = GoogleFonts.montserrat(
        fontSize: 18, fontWeight: FontWeight.w800, color: Theme.of(context).colorScheme.onSurface);

    final List<Widget> detalle;
    Widget? accion;
    switch (tipo) {
      case 'RESIDENTE':
        final r = l['residente'] as Map<String, dynamic>;
        final v = r['vehiculo'] as Map<String, dynamic>?;
        final propietarios = (r['propietarios'] as List? ?? const [])
            .map((p) => '${p['nombre']} ${p['apellido']}')
            .join(', ');
        detalle = [
          Text('Vehículo de residente', style: titulo),
          const SizedBox(height: 8),
          Wrap(spacing: 8, runSpacing: 6, crossAxisAlignment: WrapCrossAlignment.center, children: [
            ChipApto('${r['numeroApartamento']}'),
            if (v != null)
              Text([v['tipo'] == 'MOTO' ? 'Moto' : 'Carro', v['marca'], v['color']].where((x) => x != null).join(' · '),
                  style: tenue),
          ]),
          if (propietarios.isNotEmpty) ...[const SizedBox(height: 6), Text(propietarios, style: tenue)],
        ];
      case 'VISITANTE_DENTRO':
        final v = l['visitaDentro'] as Map<String, dynamic>;
        detalle = [
          Text('${v['nombreCompleto']}', style: titulo),
          const SizedBox(height: 8),
          Wrap(spacing: 8, runSpacing: 6, crossAxisAlignment: WrapCrossAlignment.center, children: [
            ChipApto('${v['numeroApartamento']}'),
            Text('Visitante dentro · entró ${horaDe(v['entrada'])}', style: tenue),
            if (v['numeroParqueadero'] != null) Text('Parq. ${v['numeroParqueadero']}', style: tenue),
          ]),
        ];
        accion = _mensaje != null
            ? null
            : FilledButton.icon(
                onPressed: () => _registrarSalida(v),
                icon: const Icon(Icons.logout_rounded),
                label: const Text('Registrar salida'));
      case 'AUTORIZADA':
        final a = l['autorizacion'] as Map<String, dynamic>;
        detalle = [
          Text('${a['nombreVisitante']} ${a['apellidoVisitante']}', style: titulo),
          const SizedBox(height: 8),
          Wrap(spacing: 8, runSpacing: 6, crossAxisAlignment: WrapCrossAlignment.center, children: [
            ChipApto('${a['numeroApartamento']}'),
            Text('Visita autorizada por ${a['autorizadaPor']}', style: tenue),
          ]),
          if (a['nota'] != null) ...[
            const SizedBox(height: 6),
            Text('“${a['nota']}”', style: tenue.copyWith(fontStyle: FontStyle.italic)),
          ],
        ];
        accion = FilledButton.icon(
            onPressed: () => _abrirIngreso(autorizacion: a),
            icon: const Icon(Icons.login_rounded),
            label: const Text('Registrar ingreso'));
      default: // DESCONOCIDA
        detalle = [
          Text('Placa no registrada', style: titulo),
          const SizedBox(height: 6),
          Text('No es de un residente ni tiene visita autorizada para hoy.', style: tenue),
        ];
        accion = FilledButton.icon(
            onPressed: () => _abrirIngreso(placa: placa),
            icon: const Icon(Icons.login_rounded),
            label: const Text('Registrar ingreso'));
    }

    final etiqueta = switch (tipo) {
      'RESIDENTE' => 'Residente',
      'VISITANTE_DENTRO' => 'Sale',
      'AUTORIZADA' => 'Autorizada',
      _ => 'Desconocida',
    };
    return Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
      Row(children: [
        ChipPlaca(placa),
        const SizedBox(width: 10),
        EtiquetaEstado(etiqueta, destacada: tipo != 'DESCONOCIDA', apagada: tipo == 'DESCONOCIDA'),
      ]),
      const SizedBox(height: 12),
      ...detalle,
      if (_mensaje != null) ...[const SizedBox(height: 12), AvisoAurum(_mensaje!, error: false)],
      if (_error != null) ...[const SizedBox(height: 12), AvisoAurum(_error!)],
      const SizedBox(height: 16),
      if (accion != null) ...[SizedBox(width: double.infinity, child: accion), const SizedBox(height: 10)],
      SizedBox(
        width: double.infinity,
        child: OutlinedButton.icon(
          style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
          onPressed: _otraPlaca,
          icon: const Icon(Icons.document_scanner_outlined),
          label: const Text('Leer otra placa'),
        ),
      ),
    ]);
  }
}
