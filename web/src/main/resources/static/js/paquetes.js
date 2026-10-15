// ══════════════════════════════════════════════════════════════════════════════
//  paquetes.js — registro de paquetes de recepción (Funcionario/Administrador)
//  Portería escribe el número del apartamento; el API lo resuelve
//  (POST /api/paquetes con numeroApartamento) y devuelve el número legible en
//  los listados — antes la tabla mostraba el ID interno del apartamento.
// ══════════════════════════════════════════════════════════════════════════════

let filtroPaquetes = 'pendientes';

function escaparHtml(texto) {
    const div = document.createElement('div');
    div.textContent = texto == null ? '' : String(texto);
    return div.innerHTML;
}

function avisoPaquete(texto, esError) {
    const caja = document.getElementById('paqueteAviso');
    caja.textContent = texto || '';
    caja.className = 'gsz-aviso' + (texto ? (esError ? ' error' : ' ok') : '');
}

function filtrarPaquetes(boton) {
    document.querySelectorAll('.gsz-segmentado button').forEach(b => b.classList.toggle('activo', b === boton));
    filtroPaquetes = boton.dataset.filtro;
    cargarPaquetes();
}

async function cargarPaquetes() {
    const url = filtroPaquetes === 'pendientes' ? '/api/paquetes/pendientes' : '/api/paquetes';
    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(await response.text());
        mostrarPaquetes(await response.json());
    } catch (error) {
        console.error('Error al cargar paquetes:', error);
    }
}

function formatoFecha(valor) {
    return new Date(valor).toLocaleString('es-CO', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
    });
}

function mostrarPaquetes(paquetes) {
    const tbody = document.getElementById('paquetesTableBody');
    const pendientes = paquetes.filter(p => !p.entregado).length;
    document.getElementById('paquetesContador').textContent =
        filtroPaquetes === 'pendientes' ? pendientes : paquetes.length;

    if (paquetes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5"><div class="gsz-vacio">
            <svg viewBox="0 0 24 24"><path d="M3.5 8 12 4l8.5 4-8.5 4-8.5-4z"/><path d="M3.5 8v8l8.5 4 8.5-4V8"/><path d="M12 12v8"/></svg>
            ${filtroPaquetes === 'pendientes' ? 'No hay paquetes pendientes por entregar.' : 'Aún no se han registrado paquetes.'}
        </div></td></tr>`;
        return;
    }

    tbody.innerHTML = paquetes.map(p => `
        <tr>
            <td class="px-4 py-3"><span class="gsz-apto">${escaparHtml(p.numeroApartamento)}</span></td>
            <td class="px-4 py-3">
                <span class="block text-sm font-semibold">${escaparHtml(p.nombreReceptor)}</span>
                <span class="block text-xs gsz-mono">${escaparHtml(p.cedulaReceptor)}</span>
            </td>
            <td class="px-4 py-3 text-xs">${formatoFecha(p.fechaHoraLlegada)}${p.entregado && p.fechaHoraEntrega
                ? `<span class="block">Entregado ${formatoFecha(p.fechaHoraEntrega)}</span>` : ''}</td>
            <td class="px-4 py-3 text-center">${p.entregado
                ? '<span class="gsz-estado gsz-estado-ok">Entregado</span>'
                : '<span class="gsz-estado gsz-estado-alerta">Pendiente</span>'}</td>
            <td class="px-4 py-3 text-center">${p.entregado ? '' :
                `<button type="button" class="text-xs font-semibold" onclick="marcarEntregado('${p.id}', this)">
                    <i class="fas fa-check mr-1"></i>Entregar</button>`}</td>
        </tr>`).join('');
}

async function submitPaquete(event) {
    event.preventDefault();
    const form = document.getElementById('paqueteForm');
    const boton = form.querySelector('button[type=submit]');
    boton.disabled = true;
    try {
        const response = await fetch('/api/paquetes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                numeroApartamento: document.getElementById('numeroApartamentoPaquete').value.trim(),
                nombreReceptor: document.getElementById('nombreReceptor').value.trim(),
                cedulaReceptor: document.getElementById('cedulaReceptor').value.trim()
            })
        });
        const texto = await response.text();
        if (!response.ok) throw new Error(texto);
        avisoPaquete(texto, false);
        form.reset();
        document.getElementById('numeroApartamentoPaquete').focus();
        await cargarPaquetes();
    } catch (error) {
        avisoPaquete(error.message || 'No se pudo registrar el paquete', true);
    } finally {
        boton.disabled = false;
    }
    return false;
}

async function marcarEntregado(id, boton) {
    if (!confirm('¿Confirmar la entrega de este paquete?')) return;
    boton.disabled = true;
    try {
        const response = await fetch(`/api/paquetes/${id}/entregar`, { method: 'POST' });
        const texto = await response.text();
        if (!response.ok) throw new Error(texto);
        avisoPaquete(texto, false);
        await cargarPaquetes();
    } catch (error) {
        boton.disabled = false;
        avisoPaquete(error.message, true);
    }
}

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) {
        sessionStorage.clear();
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        document.body.appendChild(form);
        form.submit();
    }
    return false;
}

window.addEventListener('DOMContentLoaded', cargarPaquetes);
