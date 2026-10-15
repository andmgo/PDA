// ══════════════════════════════════════════════════════════════════════════════
//  misPaquetes.js — paquetes de los apartamentos del propietario (Fase 7)
//  GET /api/paquetes/mios (pendientes primero)
// ══════════════════════════════════════════════════════════════════════════════

let filtroMisPaquetes = 'pendientes';
let misPaquetes = [];

function esc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}

function fecha(valor) {
    return new Date(valor).toLocaleString('es-CO', { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
}

/** "hace 3 h", "hace 2 días" — cuánto lleva esperando en portería. */
function haceCuanto(valor) {
    const min = Math.max(0, Math.round((Date.now() - new Date(valor)) / 60000));
    if (min < 60) return `hace ${min} min`;
    const h = Math.round(min / 60);
    if (h < 24) return `hace ${h} h`;
    const d = Math.round(h / 24);
    return `hace ${d} día${d === 1 ? '' : 's'}`;
}

async function cargarMisPaquetes() {
    try {
        const r = await fetch('/api/paquetes/mios');
        const t = await r.text();
        if (!r.ok) throw new Error(t || 'Error ' + r.status);
        misPaquetes = JSON.parse(t);
        resumen();
        pintar();
    } catch (e) {
        const aviso = document.getElementById('paqAviso');
        aviso.textContent = 'No se pudieron cargar tus paquetes: ' + e.message;
        aviso.className = 'gsz-aviso error';
    }
}

function resumen() {
    const pendientes = misPaquetes.filter(p => !p.entregado);
    document.getElementById('paqPorRecoger').textContent = pendientes.length;
    document.getElementById('paqResumenTexto').textContent = pendientes.length
        ? 'Recógelos en portería. El más antiguo llegó ' + haceCuanto(pendientes[0].fechaHoraLlegada) + '.'
        : 'No tienes paquetes esperando en portería.';
}

function filtrarMisPaquetes(boton) {
    filtroMisPaquetes = boton.dataset.filtro;
    boton.parentElement.querySelectorAll('button').forEach(b => b.classList.toggle('activo', b === boton));
    pintar();
}

function pintar() {
    const lista = misPaquetes.filter(p => filtroMisPaquetes === 'pendientes' ? !p.entregado : p.entregado);
    document.getElementById('paqContador').textContent = lista.length;
    const ul = document.getElementById('listaMisPaquetes');
    if (!lista.length) {
        ul.innerHTML = `<li class="gsz-vacio">
            <svg viewBox="0 0 24 24"><path d="M3.5 8 12 4l8.5 4-8.5 4-8.5-4z"/><path d="M3.5 8v8l8.5 4 8.5-4V8"/><path d="M12 12v8"/></svg>
            ${filtroMisPaquetes === 'pendientes' ? 'Nada por recoger.' : 'Aún no hay paquetes entregados.'}</li>`;
        return;
    }
    ul.innerHTML = lista.map(p => `<li class="gsz-visita gsz-visita-aut${p.entregado ? ' salio' : ''}">
            <span class="gsz-visita-avatar gsz-paq-icono">
                <svg viewBox="0 0 24 24"><path d="M3.5 8 12 4l8.5 4-8.5 4-8.5-4z"/><path d="M3.5 8v8l8.5 4 8.5-4V8"/><path d="M12 12v8"/></svg>
            </span>
            <div class="gsz-visita-info">
                <b>${esc(p.descripcion || 'Paquete')}</b>
                <div class="gsz-visita-meta">
                    <span class="gsz-apto">${esc(p.numeroApartamento)}</span>
                    <span class="gsz-visita-dato">Para ${esc(p.nombreReceptor)}</span>
                </div>
                <small class="gsz-visita-nota">Llegó ${fecha(p.fechaHoraLlegada)}${p.recibidoPor ? ' · recibió ' + esc(p.recibidoPor) : ''}</small>
                ${p.entregado ? `<small class="gsz-visita-nota">Entregado ${fecha(p.fechaHoraEntrega)}${p.entregadoA ? ' a ' + esc(p.entregadoA) : ''}</small>` : ''}
            </div>
            <div class="gsz-visita-hora">
                <span class="gsz-estado ${p.entregado ? '' : 'gsz-estado-alerta'}">${p.entregado ? 'Entregado' : 'En portería'}</span>
                ${p.entregado ? '' : `<small>${haceCuanto(p.fechaHoraLlegada)}</small>`}
            </div>
        </li>`).join('');
}

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) window.location.href = '/logout';
    return false;
}

window.addEventListener('DOMContentLoaded', () => {
    cargarMisPaquetes();
    setInterval(cargarMisPaquetes, 60000);
});
