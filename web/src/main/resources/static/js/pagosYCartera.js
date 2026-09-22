// ══════════════════════════════════════════════════════════════════════════════
//  pagosYCartera.js
//  Al cargar la página se detecta si viene de un callback OAuth2 exitoso
//  (?oauth=ok) para abrir el modal de agregar método de pago.
//  El botón "Agregar Método" inicia el flujo OAuth2 en vez de abrir el modal
//  directamente, como verificación de identidad adicional.
// ══════════════════════════════════════════════════════════════════════════════

const propietarios = {
    1: {
        nombre: "Carlos Mendoza",
        apartamento: "Apartamento 101",
        area: "85 m²",
        cuota: "$350.000",
        saldo: "$0",
        estado: "AL DÍA",
        estadoColor: "text-emerald-500",
        facturas: [
          { mes: "Octubre 2025", codigo: "#ADM-1025", monto: "$350.000", estado: "Pagado", pagado: true },
          { mes: "Septiembre 2025", codigo: "#ADM-0925", monto: "$350.000", estado: "Pagado", pagado: true },
          { mes: "Agosto 2025", codigo: "#ADM-0825", monto: "$350.000", estado: "Pagado", pagado: true }
        ],
        transacciones: [
          { concepto: "Pago Administración", fecha: "05 Oct 2025, 10:30 AM", monto: "+ $350.000", tipo: "ingreso" },
          { concepto: "Pago Administración", fecha: "05 Sep 2025, 09:15 AM", monto: "+ $350.000", tipo: "ingreso" }
        ],
        facturacion: {
          nombre: "Carlos Mendoza",
          documento: "CC 1.234.567",
          email: "carlos.mendoza@email.com",
          telefono: "+57 300 123 4567"
        }
    },
    2: {
        nombre: "María González",
        apartamento: "Apartamento 205",
        area: "92 m²",
        cuota: "$380.000",
        saldo: "$760.000",
        estado: "MORA",
        estadoColor: "text-red-500",
        facturas: [
          { mes: "Octubre 2025", codigo: "#ADM-1025", monto: "$380.000", estado: "Pendiente", pagado: false },
          { mes: "Septiembre 2025", codigo: "#ADM-0925", monto: "$380.000", estado: "Pendiente", pagado: false },
          { mes: "Agosto 2025", codigo: "#ADM-0825", monto: "$380.000", estado: "Pagado", pagado: true }
        ],
        transacciones: [
          { concepto: "Pago Administración", fecha: "08 Ago 2025, 02:45 PM", monto: "+ $380.000", tipo: "ingreso" },
          { concepto: "Pago Administración", fecha: "05 Jul 2025, 11:20 AM", monto: "+ $380.000", tipo: "ingreso" }
        ],
        facturacion: {
          nombre: "María González",
          documento: "CC 9.876.543",
          email: "maria.gonzalez@email.com",
          telefono: "+57 310 987 6543"
        }
    },
    3: {
        nombre: "Juan Pérez",
        apartamento: "Apartamento 308",
        area: "78 m²",
        cuota: "$320.000",
        saldo: "$320.000",
        estado: "PENDIENTE",
        estadoColor: "text-orange-500",
        facturas: [
          { mes: "Octubre 2025", codigo: "#ADM-1025", monto: "$320.000", estado: "Pendiente", pagado: false },
          { mes: "Septiembre 2025", codigo: "#ADM-0925", monto: "$320.000", estado: "Pagado", pagado: true },
          { mes: "Agosto 2025", codigo: "#ADM-0825", monto: "$320.000", estado: "Pagado", pagado: true }
        ],
        transacciones: [
          { concepto: "Pago Administración", fecha: "03 Sep 2025, 04:15 PM", monto: "+ $320.000", tipo: "ingreso" },
          { concepto: "Pago Administración", fecha: "02 Ago 2025, 08:30 AM", monto: "+ $320.000", tipo: "ingreso" }
        ],
        facturacion: {
          nombre: "Juan Pérez",
          documento: "CC 5.555.555",
          email: "juan.perez@email.com",
          telefono: "+57 320 555 5555"
        }
    }
};

// ── Render de propietario ─────────────────────────────────────────────────────

function actualizarPropietario() {
    const select = document.getElementById('propietarioSelect');
    const propietarioId = select.value;
    const datos = propietarios[propietarioId];

    document.getElementById('nombrePropietario').textContent  = datos.nombre;
    document.getElementById('infoPropietario').textContent    = datos.apartamento;
    document.getElementById('areaPropietario').textContent    = datos.area;
    document.getElementById('cuotaMensual').textContent       = datos.cuota;
    document.getElementById('saldoActual').textContent        = datos.saldo;

    const estadoEl = document.getElementById('estadoCartera');
    estadoEl.textContent  = datos.estado;
    estadoEl.className    = datos.estadoColor;
    estadoEl.style.fontSize   = '1.5rem';
    estadoEl.style.fontWeight = '700';

    // Facturas
    const listaFacturas = document.getElementById('listaFacturas');
    listaFacturas.innerHTML = datos.facturas.map(function(f) {
        return '<div style="padding: 1rem 0; border-bottom: 1px solid #f0f2f5;">' +
          '<div class="flex justify-between items-center mb-2">' +
            '<div>' +
              '<p class="font-semibold text-sm text-slate-700">' + f.mes + '</p>' +
              '<p class="text-xs text-slate-500">' + f.codigo + '</p>' +
            '</div>' +
            '<div style="text-align: right;">' +
              '<p class="font-semibold gsz-mono">' + f.monto + '</p>' +
              '<span class="badge ' + (f.pagado ? 'badge-success' : 'badge-warning') + '">' + f.estado + '</span>' +
            '</div>' +
          '</div>' +
          (f.pagado ? '' : '<button onclick="simularPago(\'' + f.mes + '\', \'' + datos.cuota + '\')" class="btn-primary w-full mt-2">Pagar Ahora</button>') +
        '</div>';
    }).join('');

    // Transacciones
    const listaTransacciones = document.getElementById('listaTransacciones');
    listaTransacciones.innerHTML = datos.transacciones.map(function(t) {
        return '<div class="flex items-center" style="gap: 0.75rem; padding: 1rem 0; border-bottom: 1px solid #f0f2f5;">' +
          '<div class="transaction-icon ' + (t.tipo === 'ingreso' ? 'icon-green' : 'icon-red') + '">' +
            '<i class="fas fa-arrow-' + (t.tipo === 'ingreso' ? 'up' : 'down') + '"></i>' +
          '</div>' +
          '<div style="flex: 1;">' +
            '<p class="font-semibold text-sm text-slate-700">' + t.concepto + '</p>' +
            '<p class="text-xs text-slate-500">' + t.fecha + '</p>' +
          '</div>' +
          '<p class="font-semibold gsz-mono ' + (t.tipo === 'ingreso' ? 'text-emerald-500' : 'text-red-500') + '">' + t.monto + '</p>' +
        '</div>';
    }).join('');

    // Info facturación
    const infoFacturacion = document.getElementById('infoFacturacion');
    infoFacturacion.innerHTML =
        '<div class="info-billing">' +
        '<h3 class="mb-4 font-semibold text-slate-700">' + datos.facturacion.nombre + '</h3>' +
        '<div style="line-height: 1.8;">' +
          '<p class="text-sm"><span class="text-slate-500">Documento:</span> <span class="font-semibold text-slate-700" style="margin-left: 0.5rem;">' + datos.facturacion.documento + '</span></p>' +
          '<p class="text-sm"><span class="text-slate-500">Email:</span> <span class="font-semibold text-slate-700" style="margin-left: 0.5rem;">' + datos.facturacion.email + '</span></p>' +
          '<p class="text-sm"><span class="text-slate-500">Teléfono:</span> <span class="font-semibold text-slate-700" style="margin-left: 0.5rem;">' + datos.facturacion.telefono + '</span></p>' +
        '</div>' +
        '<div style="margin-top: 1rem;">' +
          '<button class="btn-outline w-full"><i class="fas fa-edit"></i> Editar</button>' +
        '</div>' +
        '</div>';
}

function simularPago(mes, monto) {
    alert('¡Pago simulado!\n\nConcepto: Administración ' + mes + '\nMonto: ' + monto +
          '\n\nEsta es una simulación educativa. En un sistema real, aquí se procesaría el pago real.');
}

// ── OAuth2: Agregar Método de Pago ────────────────────────────────────────────

/**
 * Llamado por el botón "Agregar Método".
 * En lugar de abrir el modal directamente, inicia el flujo OAuth2 con Google
 * como paso de verificación de identidad.
 * Cuando Google devuelva el control (?oauth=ok), abrirModal() se ejecuta.
 */
function iniciarAgregarMetodoPago() {
    // Redirige al endpoint que inicia el flujo OAuth2
    // OAuth2ActionController (/oauth2/iniciar) guarda la acción en sesión
    // y redirige a /oauth2/authorization/google
    window.location.href = '/oauth2/iniciar?accion=PAGO';
}

/**
 * Abre el modal de agregar tarjeta.
 * Se llama directamente al retornar de OAuth2 con ?oauth=ok,
 * o puede llamarse desde abrirModal() si ya pasó la verificación.
 */
function abrirModalAgregarTarjeta() {
    const modal = document.getElementById('modalAgregarMetodo');
    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function cerrarModalAgregarTarjeta() {
    const modal = document.getElementById('modalAgregarMetodo');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

function guardarNuevoMetodo() {
    const numero = document.getElementById('inputNumeroTarjeta')?.value?.trim();
    const tipo   = document.getElementById('inputTipoTarjeta')?.value;

    if (!numero || numero.length < 16) {
        alert('Por favor ingrese un número de tarjeta válido (16 dígitos).');
        return;
    }

    // Aquí iría la llamada real a /api/pagos/metodos para guardar en MongoDB
    alert('Método de pago agregado correctamente.\nTarjeta ' + tipo + ' terminada en ' + numero.slice(-4));
    cerrarModalAgregarTarjeta();
}

// ── Detección del retorno OAuth2 al cargar la página ─────────────────────────

window.addEventListener('DOMContentLoaded', function () {
    document.getElementById('propietarioSelect').addEventListener('change', actualizarPropietario);
    actualizarPropietario();

    const params = new URLSearchParams(window.location.search);
    const oauth  = params.get('oauth');
    const motivo = params.get('motivo');

    if (oauth === 'ok') {
        // Google verificó la identidad → abrir modal de agregar tarjeta
        abrirModalAgregarTarjeta();
        // Limpiar el query string de la URL sin recargar
        history.replaceState(null, '', '/pagosYCartera');
    } else if (oauth === 'error') {
        const mensajes = {
            'no_correo':          'No se pudo obtener el correo de Google.',
            'correo_no_coincide': 'La cuenta de Google no coincide con tu usuario del sistema.',
        };
        alert('Error al verificar identidad con Google: ' +
              (mensajes[motivo] || 'Inténtalo de nuevo.'));
        history.replaceState(null, '', '/pagosYCartera');
    }
});

// ── Cerrar sesión ─────────────────────────────────────────────────────────────

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) {
        sessionStorage.clear();
        // FIX: Spring Security 6 requiere POST para logout.
        // Un GET a /logout es rechazado (CSRF) y deja al usuario en la página de error.
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        document.body.appendChild(form);
        form.submit();
    }
    return false;
}