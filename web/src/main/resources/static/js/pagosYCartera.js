// ══════════════════════════════════════════════════════════════════════════════
//  pagosYCartera.js — Pagos y cartera (Bloque J)
//   Mi cuenta ............ GET /api/pagos/mi-cuenta, POST /api/pagos/cargos/{id}/pagar
//   Vuelta de Wompi ...... ?id=<transacción> → POST /api/pagos/confirmar
//   Administración ....... /api/cartera/** (resumen, detalle, pagos, valores, cuotas)
// ══════════════════════════════════════════════════════════════════════════════

const ES_ADMIN = document.body.dataset.admin === 'true';
let pasarelaHabilitada = false;
let cartera = null;
let filtroCartera = 'todos';
let aptoAbierto = null;

const pesos = v => Number(v || 0).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const fecha = iso => {
    if (!iso) return '—';
    const [a, m, d] = String(iso).slice(0, 10).split('-').map(Number);
    return new Date(a, m - 1, d).toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
};

function esc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}

function aviso(texto, esError, idCaja = 'carteraAviso') {
    const caja = document.getElementById(idCaja);
    if (!caja) return;
    caja.textContent = texto || '';
    caja.className = 'gsz-aviso' + (texto ? (esError ? ' error' : ' ok') : '');
    if (texto && idCaja === 'carteraAviso') caja.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
}

async function pedir(url, opciones = {}) {
    const r = await fetch(url, opciones);
    const t = await r.text();
    if (!r.ok) throw new Error(t || 'Error ' + r.status);
    try { return JSON.parse(t); } catch (e) { return t; }
}

const enviar = (url, metodo, cuerpo) => pedir(url, {
    method: metodo, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(cuerpo || {})
});

const ESTADOS = {
    PAGADO: ['gsz-estado-ok', 'Pagado'],
    PENDIENTE: ['gsz-estado-alerta', 'Pendiente'],
    VENCIDO: ['gsz-estado-vencido', 'Vencido']
};
const MEDIOS = { WOMPI: 'En línea', TRANSFERENCIA: 'Transferencia', CONSIGNACION: 'Consignación', EFECTIVO: 'Efectivo' };

function cambiarTab(boton) {
    boton.parentElement.querySelectorAll('button').forEach(b => b.classList.toggle('activo', b === boton));
    document.querySelectorAll('.gsz-tab').forEach(t => t.hidden = t.id !== 'tab-' + boton.dataset.tab);
    if (boton.dataset.tab === 'conjunto') cargarCartera();
    if (boton.dataset.tab === 'valores') cargarValores();
}

// ── Mi cuenta ───────────────────────────────────────────────────────────────

async function cargarMiCuenta() {
    const caja = document.getElementById('miCuenta');
    try {
        const [cuentas, pasarela] = await Promise.all([pedir('/api/pagos/mi-cuenta'), pedir('/api/pagos/pasarela')]);
        pasarelaHabilitada = !!pasarela.habilitada;
        if (!cuentas.length) {
            caja.innerHTML = `<div class="gsz-panel"><div class="gsz-vacio">
                <svg viewBox="0 0 24 24"><rect x="3" y="6" width="18" height="13" rx="2.5"/><path d="M3 10h18"/></svg>
                No tienes apartamentos vinculados, así que no hay estado de cuenta.${ES_ADMIN ? ' Revisa la pestaña “Cartera del conjunto”.' : ''}</div></div>`;
            if (ES_ADMIN) document.querySelector('[data-tab="conjunto"]').click();
            return;
        }
        caja.innerHTML = cuentas.map(cuentaHtml).join('');
    } catch (e) {
        caja.innerHTML = `<div class="gsz-panel"><div class="gsz-vacio">No se pudo cargar tu estado de cuenta: ${esc(e.message)}</div></div>`;
    }
}

function cuentaHtml(c) {
    const pendientes = c.cargos.filter(x => x.estado !== 'PAGADO');
    const pagados = c.cargos.filter(x => x.estado === 'PAGADO');
    return `<div class="gsz-cuenta">
        <section class="gsz-cuenta-resumen ${c.enMora ? 'mora' : ''}">
            <div>
                <p class="gsz-kicker">Apartamento</p>
                <p class="gsz-cuenta-apto">${esc(c.numeroApartamento)}</p>
            </div>
            <div>
                <p class="gsz-kicker">Saldo pendiente</p>
                <p class="gsz-cuenta-saldo gsz-dinero">${pesos(c.saldo)}</p>
                ${c.saldoVencido > 0 ? `<p class="gsz-cuenta-vencido">${pesos(c.saldoVencido)} vencido</p>` : ''}
            </div>
            <span class="gsz-estado ${c.enMora ? 'gsz-estado-vencido' : 'gsz-estado-ok'}">${c.enMora ? 'En mora' : 'Al día'}</span>
        </section>
        <section class="gsz-panel">
            <div class="gsz-panel-head"><div><p class="gsz-kicker">Por pagar</p><h2 class="gsz-card-titulo">Cobros pendientes</h2></div></div>
            ${pendientes.length ? `<ul class="gsz-cobros">${pendientes.map(x => cobroHtml(x, true)).join('')}</ul>`
                : '<div class="gsz-vacio">No tienes cobros pendientes. ¡Estás al día!</div>'}
            ${pendientes.length && !pasarelaHabilitada ? `<p class="gsz-cartera-nota">El pago en línea se habilitará pronto. Mientras tanto, paga por transferencia y envía el comprobante a la administración: ellos lo registran aquí.</p>` : ''}
        </section>
        ${pagados.length ? `<section class="gsz-panel">
            <div class="gsz-panel-head"><div><p class="gsz-kicker">Historial</p><h2 class="gsz-card-titulo">Pagados</h2></div></div>
            <ul class="gsz-cobros">${pagados.map(x => cobroHtml(x, false)).join('')}</ul>
        </section>` : ''}
    </div>`;
}

function cobroHtml(x, conAccion, admin = false) {
    const [clase, texto] = ESTADOS[x.estado] || ['', x.estado];
    const abonos = x.pagos.filter(p => p.estado === 'APROBADO');
    return `<li class="gsz-cobro">
        <div class="gsz-cobro-info">
            <b>${esc(x.descripcion)}</b>
            <small>Vence ${fecha(x.fechaVencimiento)}${x.pagado > 0 && x.saldo > 0 ? ` · abonado ${pesos(x.pagado)}` : ''}</small>
            ${abonos.length ? `<small class="gsz-cobro-pagos">${abonos.map(p => `${MEDIOS[p.medio] || p.medio} ${pesos(p.valor)} · ${fecha(p.fecha)}${p.soporte ? ' · ' + esc(p.soporte) : ''}`).join('<br>')}</small>` : ''}
        </div>
        <div class="gsz-cobro-valor">
            <span class="gsz-dinero">${pesos(x.estado === 'PAGADO' ? x.valor : x.saldo)}</span>
            <span class="gsz-estado ${clase}">${texto}</span>
        </div>
        ${conAccion && !admin && pasarelaHabilitada ? `<button type="button" class="gsz-btn gsz-btn-noche gsz-cobro-boton" onclick="pagarEnLinea('${x.id}', this)">Pagar</button>` : ''}
        ${conAccion && admin ? `<button type="button" class="gsz-btn gsz-btn-borde gsz-cobro-boton" onclick="abrirPagoManual('${x.id}', ${x.saldo}, '${esc(x.descripcion).replace(/'/g, '&#39;')}')">Registrar pago</button>` : ''}
    </li>`;
}

async function pagarEnLinea(idCargo, boton) {
    boton.disabled = true;
    try {
        const r = await enviar(`/api/pagos/cargos/${idCargo}/pagar`, 'POST',
            { urlRegreso: window.location.origin + '/pagosYCartera' });
        window.location.href = r.url; // checkout de Wompi
    } catch (e) {
        boton.disabled = false;
        aviso(e.message, true);
    }
}

/** Wompi vuelve con ?id=<transacción>: se confirma consultando a la pasarela. */
async function confirmarRegreso() {
    const id = new URLSearchParams(window.location.search).get('id');
    if (!id) return;
    history.replaceState(null, '', window.location.pathname);
    try {
        const r = await enviar('/api/pagos/confirmar', 'POST', { idTransaccion: id });
        const textos = {
            APROBADO: ['¡Pago aprobado! Ya quedó registrado en tu estado de cuenta.', false],
            RECHAZADO: ['El pago fue rechazado o anulado por la entidad. No se descontó nada; puedes intentarlo de nuevo.', true],
            PENDIENTE: ['Tu pago está en proceso. Se reflejará apenas la entidad lo confirme.', false]
        };
        const [t, error] = textos[r.estado] || ['Estado del pago: ' + r.estado, false];
        aviso(t, error);
    } catch (e) {
        aviso('No se pudo confirmar el pago: ' + e.message, true);
    }
}

// ── Cartera del conjunto (administración) ────────────────────────────────────

async function cargarCartera() {
    try {
        cartera = await pedir('/api/cartera/resumen');
        document.getElementById('kpiPorCobrar').textContent = pesos(cartera.porCobrar);
        document.getElementById('kpiVencido').textContent = pesos(cartera.vencido);
        document.getElementById('kpiMora').textContent = cartera.apartamentosEnMora;
        document.getElementById('kpiAlDia').textContent = cartera.apartamentosAlDia;
        pintarCartera();
        if (aptoAbierto) verApartamento(aptoAbierto);
    } catch (e) {
        aviso('No se pudo cargar la cartera: ' + e.message, true);
    }
}

function filtrarCartera(boton) {
    filtroCartera = boton.dataset.filtro;
    boton.parentElement.querySelectorAll('button').forEach(b => b.classList.toggle('activo', b === boton));
    pintarCartera();
}

function pintarCartera() {
    const filas = cartera.apartamentos.filter(a => filtroCartera === 'todos' || a.enMora);
    document.getElementById('tablaCartera').innerHTML = filas.length ? filas.map(a => `
        <tr class="${a.idApartamento === aptoAbierto ? 'activa' : ''}" onclick="verApartamento('${a.idApartamento}')">
            <td><span class="gsz-apto">${esc(a.numeroApartamento)}</span></td>
            <td class="gsz-tabla-tenue">${a.propietarios.length ? a.propietarios.map(esc).join(', ') : '—'}</td>
            <td class="num gsz-dinero">${pesos(a.saldo)}</td>
            <td class="num gsz-dinero">${a.saldoVencido > 0 ? pesos(a.saldoVencido) : '—'}</td>
            <td><span class="gsz-estado ${a.enMora ? 'gsz-estado-vencido' : 'gsz-estado-ok'}">${a.enMora ? 'En mora' : 'Al día'}</span></td>
            <td class="num"><span class="gsz-tabla-ver">Ver ›</span></td>
        </tr>`).join('')
        : '<tr><td colspan="6"><div class="gsz-vacio">Ningún apartamento en mora.</div></td></tr>';
}

async function verApartamento(id) {
    aptoAbierto = id;
    pintarCartera();
    const panel = document.getElementById('detalleApto');
    try {
        const c = await pedir(`/api/cartera/apartamentos/${id}`);
        const pendientes = c.cargos.filter(x => x.estado !== 'PAGADO');
        const pagados = c.cargos.filter(x => x.estado === 'PAGADO');
        panel.innerHTML = `
            <div class="gsz-panel-head">
                <div><p class="gsz-kicker">Apartamento ${esc(c.numeroApartamento)}</p>
                <h2 class="gsz-card-titulo">${pesos(c.saldo)} <small class="gsz-detalle-sub">de saldo</small></h2></div>
                <span class="gsz-estado ${c.enMora ? 'gsz-estado-vencido' : 'gsz-estado-ok'}">${c.enMora ? 'En mora' : 'Al día'}</span>
            </div>
            <p class="gsz-card-sub">${c.propietarios.length ? c.propietarios.map(esc).join(', ') : 'Sin propietarios registrados'}</p>
            <div id="avisoDetalle" class="gsz-aviso" role="status"></div>
            <div id="formPagoManual"></div>
            ${pendientes.length ? `<ul class="gsz-cobros">${pendientes.map(x => cobroHtml(x, true, true)).join('')}</ul>` : '<div class="gsz-vacio">Sin cobros pendientes.</div>'}
            ${pagados.length ? `<p class="gsz-kicker gsz-detalle-historial">Pagados</p><ul class="gsz-cobros">${pagados.slice(0, 8).map(x => cobroHtml(x, false, true)).join('')}</ul>` : ''}`;
    } catch (e) {
        panel.innerHTML = `<div class="gsz-vacio">No se pudo cargar el apartamento: ${esc(e.message)}</div>`;
    }
}

function abrirPagoManual(idCargo, saldo, descripcion) {
    const caja = document.getElementById('formPagoManual');
    caja.innerHTML = `<form class="gsz-pago-manual gsz-form-stack" onsubmit="return registrarPagoManual(event, '${idCargo}')">
        <p class="gsz-kicker">Registrar pago · ${descripcion}</p>
        <div class="gsz-campos-2">
            <label class="gsz-campo"><span>Valor</span><input type="text" id="pmValor" class="gsz-dinero" inputmode="numeric" value="${Math.round(saldo)}" required /></label>
            <label class="gsz-campo"><span>Medio</span>
                <select id="pmMedio">
                    <option value="TRANSFERENCIA">Transferencia</option>
                    <option value="CONSIGNACION">Consignación</option>
                    <option value="EFECTIVO">Efectivo</option>
                </select></label>
        </div>
        <label class="gsz-campo"><span>N.º de comprobante <em>(no aplica para efectivo)</em></span><input type="text" id="pmSoporte" maxlength="120" placeholder="Ej: BCO-77812" /></label>
        <div class="gsz-pago-manual-acciones">
            <button type="button" class="gsz-btn gsz-btn-borde" onclick="document.getElementById('formPagoManual').innerHTML=''">Cancelar</button>
            <button type="submit" class="gsz-btn gsz-btn-noche">Registrar pago</button>
        </div>
    </form>`;
    caja.querySelector('#pmValor').focus();
}

async function registrarPagoManual(event, idCargo) {
    event.preventDefault();
    const v = id => document.getElementById(id).value.trim();
    try {
        const m = await enviar('/api/cartera/pagos', 'POST', { idCargo, valor: v('pmValor'), medio: v('pmMedio'), soporte: v('pmSoporte') });
        aviso(m, false);
        await cargarCartera();
    } catch (e) {
        aviso(e.message, true, 'avisoDetalle');
    }
    return false;
}

// ── Valores y cuotas (administración) ────────────────────────────────────────

async function cargarValores() {
    try {
        const c = await pedir('/api/cartera/configuracion');
        document.getElementById('valCuota').value = c.cuotaAdministracion > 0 ? pesos(c.cuotaAdministracion) : '';
        document.getElementById('valReserva').value = c.valorReserva > 0 ? pesos(c.valorReserva) : '';
        document.getElementById('valDia').value = c.diaVencimiento;
        const mes = document.getElementById('mesCuotas');
        if (!mes.value) mes.value = new Date().toISOString().slice(0, 7);
        document.getElementById('notaCuotas').textContent = c.cuotaAdministracion > 0
            ? `Se cobrará ${pesos(c.cuotaAdministracion)} a cada apartamento, con vencimiento el día ${c.diaVencimiento} del mes.`
            : 'Primero define el valor de la cuota.';
    } catch (e) {
        aviso('No se pudieron cargar los valores: ' + e.message, true);
    }
}

async function guardarValores(event) {
    event.preventDefault();
    try {
        await enviar('/api/cartera/configuracion', 'PUT', {
            cuotaAdministracion: document.getElementById('valCuota').value,
            valorReserva: document.getElementById('valReserva').value || '0',
            diaVencimiento: document.getElementById('valDia').value
        });
        aviso('Valores guardados.', false);
        cargarValores();
    } catch (e) {
        aviso(e.message, true);
    }
    return false;
}

async function generarCuotas(event) {
    event.preventDefault();
    const mes = document.getElementById('mesCuotas').value;
    if (!confirm(`¿Generar la cuota de administración de ${mes} para todos los apartamentos activos?`)) return false;
    try {
        aviso(await enviar('/api/cartera/cuotas', 'POST', { periodo: mes }), false);
    } catch (e) {
        aviso(e.message, true);
    }
    return false;
}

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) window.location.href = '/logout';
    return false;
}

window.addEventListener('DOMContentLoaded', async () => {
    await confirmarRegreso();
    cargarMiCuenta();
});
