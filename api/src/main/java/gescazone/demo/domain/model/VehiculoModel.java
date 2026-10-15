package gescazone.demo.domain.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/** Vehículo de un residente, ligado a un apartamento (y opcionalmente a su parqueadero). */
public class VehiculoModel {

    public enum Tipo { CARRO, MOTO }

    // Formatos de placa colombiana: carro "ABC123"; moto "ABC12D" (o "ABC12" en placas antiguas).
    private static final Pattern PLACA_CARRO = Pattern.compile("^[A-Z]{3}\\d{3}$");
    private static final Pattern PLACA_MOTO = Pattern.compile("^[A-Z]{3}\\d{2}[A-Z]?$");

    private String id;
    private String placa;
    private Tipo tipo;
    private String marca;
    private String color;
    private String idApartamento;
    private String idParqueadero;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    public VehiculoModel() {}

    public VehiculoModel(String placa, Tipo tipo, String idApartamento) {
        this.placa = normalizarPlaca(placa);
        this.tipo = tipo;
        this.idApartamento = idApartamento;
        this.fechaRegistro = LocalDateTime.now();
        validarFormatoPlaca(this.placa, tipo);
    }

    /** "abc-123", " ABC 123 " → "ABC123". Es la forma en que se guarda y se busca. */
    public static String normalizarPlaca(String placa) {
        if (placa == null) return null;
        return placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    public static void validarFormatoPlaca(String placaNormalizada, Tipo tipo) {
        if (placaNormalizada == null || placaNormalizada.isBlank())
            throw new IllegalArgumentException("La placa es obligatoria");
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de vehículo es obligatorio (CARRO o MOTO)");
        boolean valida = tipo == Tipo.CARRO
                ? PLACA_CARRO.matcher(placaNormalizada).matches()
                : PLACA_MOTO.matcher(placaNormalizada).matches();
        if (!valida)
            throw new IllegalArgumentException(tipo == Tipo.CARRO
                    ? "Placa de carro inválida: debe ser 3 letras y 3 números (ej. ABC123)"
                    : "Placa de moto inválida: debe ser 3 letras, 2 números y opcionalmente una letra (ej. ABC12D)");
    }

    public void desactivar() {
        if (!this.activo)
            throw new IllegalStateException("Este vehículo ya está desactivado");
        this.activo = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }

    public String getIdParqueadero() { return idParqueadero; }
    public void setIdParqueadero(String idParqueadero) { this.idParqueadero = idParqueadero; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
