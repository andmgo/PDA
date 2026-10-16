# Pruebas unitarias de la API — Aurum Condominios

Proyecto Maven aparte (`gescazone-pruebas`) con pruebas unitarias de la API
(`../api`). Prueba las **reglas de negocio** sin base de datos ni servidor:
cada prueba corre en milisegundos y no depende de datos guardados.

- **JUnit 5**: `@Nested` agrupa por regla, `@DisplayName` las describe en
  español, `@ParameterizedTest` cubre varios casos con una sola prueba.
- **Mockito**: reemplaza repositorios y la pasarela en las pruebas de
  servicios (`@Mock`, `@InjectMocks`, `@Spy` para fijar "hoy").
- **AssertJ**: aserciones legibles (`assertThat(...).isEqualByComparingTo(...)`).
- **JaCoCo**: mide qué parte del código de la API ejercitan estas pruebas.

## Cómo ejecutarlas

Desde la carpeta `GESCAZONE/` (la raíz, donde está el `pom.xml` que agrupa
`api` y `pruebas`):

```bash
# Solo este proyecto (compila la API antes) + reporte de cobertura
./mvnw -pl pruebas -am verify -Dtest='gescazone.demo.**.*Pruebas,gescazone.demo.domain.model.*Test' -Dsurefire.failIfNoSpecifiedTests=false

# Todo: pruebas de la API (necesitan PostgreSQL local) + este proyecto
./mvnw verify
```

En Windows usa `mvnw.cmd`. Hace falta Java 17 (`JAVA_HOME`).

**Reporte de cobertura:** `pruebas/target/site/jacoco-aggregate/index.html`.

## Organización

Los paquetes replican los de la API (así las pruebas pueden usar métodos
de visibilidad de paquete, como `CarteraService.hoy()`).

| Archivo | Qué prueba |
|---|---|
| `domain/model/CargoModelTest` | Cobros: creación, saldo nunca negativo, anulación, vencimiento, mes en español |
| `domain/model/PagoModelTest` | Pagos en línea (pendiente → aprobado/rechazado, referencia única, centavos) y manuales (comprobante obligatorio salvo efectivo) |
| `domain/model/ConfiguracionCarteraModelTest` | Valores de la cartera y fecha de vencimiento de la cuota |
| `domain/model/VisitaAutorizadaModelTest` | Visitas autorizadas: fechas, máximo 30 días, 90 de anticipación, vigencia, cancelación |
| `domain/model/PaqueteModelTest` | Entrega de paquetes: a quién se entrega, no dos veces |
| `application/service/CarteraServicePruebas` | Resumen del conjunto (totales, mora, orden), validaciones de entrada, confirmación con la pasarela y eventos falsos |
| `application/service/ReservaReglasPruebas` | Fechas de reserva: pasado, 24/31 dic y 1 ene bloqueados, otro año por solicitud |
| `infrastructure/pagos/WompiPasarelaPruebas` | Enlace de pago, firma de integridad, estados de Wompi, ids inválidos, eventos ignorados |

Las clases de servicio terminan en `Pruebas` para no confundirse con las
pruebas que viven dentro de la API (`api/src/test`), que siguen allí.

## Cómo agregar una prueba

1. Crea la clase en el paquete que corresponde al código que pruebas.
2. Si la regla es de un modelo, pruébalo directo (sin mocks).
3. Si es de un servicio, simula sus repositorios con `@Mock` y crea el
   servicio con `@InjectMocks`; si usa la fecha de hoy, envuélvelo con
   `@Spy` y fija `hoy()`.
4. Nombra la clase `…Test` (modelos) o `…Pruebas` (servicios); surefire
   incluye ambos patrones.
