import 'package:aurum_condominios/datos/placas.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('placasEnTexto', () {
    test('placa de carro como la lee la cámara (número arriba, ciudad abajo)', () {
      expect(placasEnTexto('ABC 123\nBOGOTA D.C.'), ['ABC123']);
    });

    test('placa de moto con y sin letra final', () {
      expect(placasEnTexto('XYZ 12D\nCARTAGENA').first, 'XYZ12D');
      expect(placasEnTexto('MOT 45').first, 'MOT45');
    });

    test('corrige por posición las confusiones típicas del OCR', () {
      expect(placasEnTexto('A8C-1Z3').first, 'ABC123'); // 8→B en letras, Z→2 en números
      expect(placasEnTexto('0KM 5O1').first, 'OKM501'); // 0→O en letras, O→0 en números
      expect(placasEnTexto('HJK 4SF').first, 'HJK45F'); // S→5
    });

    test('encuentra la placa dentro de una línea más larga', () {
      expect(placasEnTexto('PLACA:ABC123 CARRO'), contains('ABC123'));
    });

    test('ignora texto que no es una placa', () {
      expect(placasEnTexto('CARTAGENA\nBIENVENIDOS\n2026'), isEmpty);
      expect(placasEnTexto(''), isEmpty);
    });
  });

  test('normalizar y validar', () {
    expect(normalizarPlaca(' abc-123 '), 'ABC123');
    expect(esPlacaValida('ABC123'), isTrue);
    expect(esPlacaValida('ABC12D'), isTrue);
    expect(esPlacaValida('AB1234'), isFalse);
  });

  test('el estabilizador confirma solo con lecturas repetidas', () {
    final e = EstabilizadorPlaca();
    expect(e.agregar(['ABC123']), isNull);
    expect(e.agregar([]), isNull); // cuadro sin texto: no reinicia
    expect(e.agregar(['ABC128']), isNull); // lectura distinta: vuelve a empezar
    expect(e.agregar(['ABC128']), 'ABC128');
    e.reiniciar();
    expect(e.agregar(['ABC128']), isNull);
  });
}
