// ══════════════════════════════════════════════════════════════════════════════
//  misVisitas.js — el propietario autoriza visitas (Fase 6)
//  /api/propiedad/mia            → sus apartamentos (para el selector)
//  /api/visitas-autorizadas/**   → mías, crear, cancelar
// ══════════════════════════════════════════════════════════════════════════════

let filtroVisitas = 'proximas';
let autorizaciones = [];

function esc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}

function aviso(texto, esError) {
    const caja = document.getElementById('visitaAviso');
    caja.textContent = texto || '';
    caja.className = 'gsz-aviso' + (texto ? (esError ? ' error' : ' ok') : '');
}

async function pedir(url, opciones) {
    const r = await fetch(url, opciones);
    const t = await r.text();
    if (!r.ok) throw new Error(t || 'Error ' + r.status);
    try { return JSON.parse(t); } catch (e) { return t; }
}

const hoyISO = () => new Date().toLocaleDateString('en-CA'); // AAAA-MM-DD en hora local

function fechaCorta(iso) {
    const [a, m, d] = iso.split('-').map(Number);
    return new Date(a, m - 1, d).toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric', month: 'short' });
}

function rango(a) {
    return a.fechaDesde === a.fechaHasta ? fechaCorta(a.fechaDesde)
        : `${fechaCorta(a.fechaDesde)} → ${fechaCorta(a.fechaHasta)}`;
}

const ETIQUETA = {
    VIGENTE: ['gsz-estado-ok', 'Hoy'],
    PROGRAMADA: ['gsz-estado-alerta', 'Programada'],
    VENCIDA: ['', 'Vencida'],
    CANCELADA: ['', 'Cancelada']
};

async function cargarApartamentos() {
    const select = document.getElementById('visitaApto');
    try {
        const aptos = await pedir('/api/propiedad/mia');
        if (!aptos.length) {
            select.innerHTML = '<option value="">Sin apartamentos vinculados</option>';
            document.getElementById('btnAutorizar').disabled = true;
            aviso('Aún no tienes apartamentos vinculados. La administración los asigna desde Gestión de Datos.', true);
            return;
        }
        select.innerHTML = aptos.map(a => `<option value="${esc(a.idApartamento)}">Apartamento ${esc(a.numeroApartamento)}</option>`).join('');
    } catch (e) {
        aviso('No se pudieron cargar tus apartamentos: ' + e.message, true);
    }
}

async function cargarAutorizaciones() {
    try {
        autorizaciones = await pedir('/api/visitas-autorizadas/mias');
        pintar();
    } catch (e) {
        document.getElementById('listaAutorizaciones').innerHTML = `<li class="gsz-vacio">No se pudo cargar: ${esc(e.message)}</li>`;
    }
}

function filtrarVisitas(boton) {
    filtroVisitas = boton.dataset.filtro;
    boton.parentElement.querySelectorAll('button').forEach(b => b.classList.toggle('activo', b === boton));
    pintar();
}

function pintar() {
    const proximas = a => a.estado === 'VIGENTE' || a.estado === 'PROGRAMADA';
    const lista = autorizaciones
        .filter(a => filtroVisitas === 'proximas' ? proximas(a) : !proximas(a))
        .sort((x, y) => filtroVisitas === 'proximas'
            ? x.fechaDesde.localeCompare(y.fechaDesde) : y.fechaDesde.localeCompare(x.fechaDesde));
    document.getElementById('visitasContador').textContent = lista.length;
    const ul = document.getElementById('listaAutorizaciones');
    if (!lista.length) {
        ul.innerHTML = `<li class="gsz-vacio">
            <svg viewBox="0 0 24 24"><circle cx="9" cy="8" r="3"/><path d="M3.5 19c.7-3.2 2.8-5 5.5-5"/><path d="M15 17.5l2 2 4-4.5"/></svg>
            ${filtroVisitas === 'proximas' ? 'No tienes visitas autorizadas próximas.' : 'Aún no hay visitas pasadas.'}</li>`;
        return;
    }
    ul.innerHTML = lista.map(a => {
        const [clase, texto] = ETIQUETA[a.estado] || ['', a.estado];
        const iniciales = (a.nombreVisitante[0] + (a.apellidoVisitante[0] || '')).toUpperCase();
        const extra = [
            a.numeroDocumento ? `<span class="gsz-visita-dato gsz-mono">${esc(a.numeroDocumento)}</span>` : '<span class="gsz-visita-dato">Sin documento</span>',
            a.placa ? `<span class="gsz-placa">${esc(a.placa)}</span>` : '',
            a.ingresos ? `<span class="gsz-visita-dato">${a.ingresos} ingreso${a.ingresos === 1 ? '' : 's'}</span>` : ''
        ].join('');
        const cancelable = a.estado === 'VIGENTE' || a.estado === 'PROGRAMADA';
        return `<li class="gsz-visita gsz-visita-aut${cancelable ? '' : ' salio'}">
            <span class="gsz-visita-avatar">${esc(iniciales)}</span>
            <div class="gsz-visita-info">
                <b>${esc(a.nombreVisitante)} ${esc(a.apellidoVisitante)}</b>
                <div class="gsz-visita-meta"><span class="gsz-apto">${esc(a.numeroApartamento)}</span>${extra}</div>
                ${a.nota ? `<small class="gsz-visita-nota">“${esc(a.nota)}”</small>` : ''}
            </div>
            <div class="gsz-visita-hora">
                <span class="gsz-estado ${clase}">${texto}</span>
                <small>${rango(a)}</small>
            </div>
            ${cancelable ? `<button type="button" class="gsz-btn gsz-btn-borde gsz-btn-salida" onclick="cancelar('${a.id}', this)">Cancelar</button>` : ''}
        </li>`;
    }).join('');
}

async function autorizarVisita(event) {
    event.preventDefault();
    const v = id => document.getElementById(id).value.trim();
    const boton = document.getElementById('btnAutorizar');
    boton.disabled = true;
    try {
        const creada = await pedir('/api/visitas-autorizadas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                idApartamento: v('visitaApto'),
                nombreVisitante: v('visitaNombre'),
                apellidoVisitante: v('visitaApellido'),
                numeroDocumento: v('visitaDocumento'),
                placa: v('visitaPlaca'),
                fechaDesde: v('visitaDesde'),
                fechaHasta: v('visitaHasta'),
                nota: v('visitaNota')
            })
        });
        aviso(`Listo: ${creada.nombreVisitante} ${creada.apellidoVisitante} queda autorizado(a) ${rango(creada).toLowerCase()}. Portería ya lo ve.`, false);
        ['visitaNombre', 'visitaApellido', 'visitaDocumento', 'visitaPlaca', 'visitaNota'].forEach(id => document.getElementById(id).value = '');
        filtrarVisitas(document.querySelector('[data-filtro="proximas"]'));
        await cargarAutorizaciones();
    } catch (e) {
        aviso(e.message, true);
    } finally {
        boton.disabled = false;
    }
    return false;
}

async function cancelar(id, boton) {
    if (!confirm('¿Cancelar esta autorización? Portería ya no dejará entrar a esta persona con ella.')) return;
    boton.disabled = true;
    try {
        aviso(await pedir(`/api/visitas-autorizadas/${id}/cancelar`, { method: 'PUT' }), false);
        await cargarAutorizaciones();
    } catch (e) {
        boton.disabled = false;
        aviso(e.message, true);
    }
}

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) window.location.href = '/logout';
    return false;
}

window.addEventListener('DOMContentLoaded', () => {
    const desde = document.getElementById('visitaDesde'), hasta = document.getElementById('visitaHasta');
    desde.value = hasta.value = desde.min = hasta.min = hoyISO();
    desde.addEventListener('change', () => {
        hasta.min = desde.value;
        if (!hasta.value || hasta.value < desde.value) hasta.value = desde.value;
    });
    document.getElementById('visitaPlaca').addEventListener('input', e => {
        e.target.value = e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    });
    cargarApartamentos();
    cargarAutorizaciones();
});
