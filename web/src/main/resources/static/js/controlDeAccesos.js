// ══════════════════════════════════════════════════════════════════════════════
//  controlDeAccesos.js — Portería (Fase 3)
//  Ingreso de visitantes en un solo paso contra /api/accesos/visitas/**
//  (VisitaRestController en gescazone-api): si el documento ya vino antes se
//  autocompletan sus datos; si es la primera vez, el API crea al visitante.
//  El parqueadero de visitante se asigna solo si no se elige uno.
//  La piscina sigue usando /api/accesos/piscina/** como antes.
// ══════════════════════════════════════════════════════════════════════════════

let modoIngreso = 'pie';
let vistaActual = 'dentro';
let busquedaVisitante = null;   // temporizador de la búsqueda por documento

// ── Utilidades ──────────────────────────────────────────────────────────────

function esc(texto) {
    const div = document.createElement('div');
    div.textContent = texto == null ? '' : String(texto);
    return div.innerHTML;
}

function aviso(id, texto, esError) {
    const caja = document.getElementById(id);
    if (!caja) return;
    caja.textContent = texto || '';
    caja.className = 'gsz-aviso' + (texto ? (esError ? ' error' : ' ok') : '');
}

async function pedir(url, opciones) {
    const response = await fetch(url, opciones);
    const texto = await response.text();
    if (!response.ok) throw new Error(texto || 'Error ' + response.status);
    try { return JSON.parse(texto); } catch (e) { return texto; }
}

function enviarJson(url, metodo, cuerpo) {
    return pedir(url, {
        method: metodo,
        headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
        body: cuerpo ? JSON.stringify(cuerpo) : undefined
    });
}

/** "2026-10-15 08:05:00" → { hora: "8:05 a. m.", hace: "hace 2 h" } */
function tiempo(fechaTexto) {
    if (!fechaTexto) return { hora: '—', hace: '' };
    const fecha = new Date(fechaTexto.replace(' ', 'T'));
    const hora = fecha.toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' });
    const minutos = Math.max(0, Math.round((Date.now() - fecha.getTime()) / 60000));
    const hace = minutos < 1 ? 'recién' : minutos < 60 ? `hace ${minutos} min`
        : `hace ${Math.floor(minutos / 60)} h ${minutos % 60 ? (minutos % 60) + ' min' : ''}`.trim();
    return { hora, hace };
}

function iniciales(nombre) {
    return (nombre || '?').split(' ').filter(Boolean).slice(0, 2).map(p => p[0]).join('').toUpperCase();
}

// ── Formulario de ingreso ───────────────────────────────────────────────────

function elegirModo(boton) {
    modoIngreso = boton.dataset.modo;
    boton.parentElement.querySelectorAll('button').forEach(b => b.classList.toggle('activo', b === boton));
    const vehiculo = modoIngreso === 'vehiculo';
    document.getElementById('camposVehiculo').hidden = !vehiculo;
    document.getElementById('placaVisitante').required = vehiculo;
    if (vehiculo) {
        cargarParqueaderosLibres();
        document.getElementById('placaVisitante').focus();
    }
}

async function cargarParqueaderosLibres() {
    const select = document.getElementById('parqueaderoVisitante');
    try {
        const libres = await pedir('/api/accesos/visitas/parqueaderos-libres');
        select.innerHTML = `<option value="">${libres.length
            ? `Asignar automáticamente (${libres.length} libres)` : 'Sin parqueaderos libres'}</option>`
            + libres.map(n => `<option value="${esc(n)}">${esc(n)}</option>`).join('');
    } catch (e) {
        select.innerHTML = '<option value="">Asignar automáticamente</option>';
    }
}

function estadoVisitante(html, clase) {
    const p = document.getElementById('visitanteEstado');
    p.innerHTML = html || '';
    p.className = 'gsz-visitante-estado' + (clase ? ' ' + clase : '');
}

/** Al escribir el documento: si ya vino antes, autocompleta. */
function alEscribirDocumento() {
    clearTimeout(busquedaVisitante);
    const doc = document.getElementById('docVisitante').value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (doc.length < 5) { estadoVisitante(''); return; }
    busquedaVisitante = setTimeout(async () => {
        try {
            const v = await pedir(`/api/accesos/visitas/visitante/${doc}`);
            document.getElementById('nombreVisitante').value = v.nombre || '';
            document.getElementById('apellidoVisitante').value = v.apellido || '';
            if (v.celular) document.getElementById('celularVisitante').value = v.celular;
            if (v.ultimaPlaca && !document.getElementById('placaVisitante').value)
                document.getElementById('placaVisitante').value = v.ultimaPlaca;
            if (v.bloqueado) {
                estadoVisitante(`<b>${esc(v.nombre)} ${esc(v.apellido)}</b> tiene el ingreso bloqueado.`, 'bloqueado');
            } else {
                const veces = v.visitasPrevias === 1 ? '1 visita previa' : `${v.visitasPrevias} visitas previas`;
                estadoVisitante(`Visitante frecuente · <b>${esc(v.nombre)} ${esc(v.apellido)}</b> · ${veces}`
                    + (v.ultimaPlaca ? ` · última placa <span class="gsz-placa">${esc(v.ultimaPlaca)}</span>` : ''), 'conocido');
            }
        } catch (e) {
            estadoVisitante('Primera visita: escribe su nombre y apellido.', 'nuevo');
        }
    }, 350);
}

async function registrarIngreso(event) {
    event.preventDefault();
    const boton = document.getElementById('btnIngreso');
    const valor = id => document.getElementById(id).value.trim();
    boton.disabled = true;
    try {
        const mensaje = await enviarJson('/api/accesos/visitas/ingreso', 'POST', {
            numeroDocumento: valor('docVisitante'),
            nombre: valor('nombreVisitante'),
            apellido: valor('apellidoVisitante'),
            celular: valor('celularVisitante'),
            numeroApartamento: valor('aptoVisitante'),
            conVehiculo: modoIngreso === 'vehiculo',
            placa: valor('placaVisitante'),
            numeroParqueadero: valor('parqueaderoVisitante')
        });
        aviso('ingresoAviso', mensaje, false);
        document.getElementById('ingresoForm').reset();
        estadoVisitante('');
        elegirModo(document.querySelector('[data-modo="pie"]'));
        document.getElementById('docVisitante').focus();
        if (vistaActual !== 'piscina') await cargarVisitas();
    } catch (error) {
        aviso('ingresoAviso', error.message, true);
    } finally {
        boton.disabled = false;
    }
    return false;
}

// ── Vistas: dentro ahora / hoy / piscina ────────────────────────────────────

function cambiarVista(boton) {
    vistaActual = boton.dataset.vista;
    boton.parentElement.querySelectorAll('button').forEach(b => b.classList.toggle('activo', b === boton));
    const piscina = vistaActual === 'piscina';
    document.getElementById('vistaVisitas').hidden = piscina;
    document.getElementById('vistaPiscina').hidden = !piscina;
    const titulos = {
        dentro: ['En este momento', 'Dentro del conjunto'],
        hoy: ['Registro del día', 'Visitas de hoy'],
        piscina: ['Áreas comunes', 'Acceso a la piscina']
    };
    document.getElementById('vistaKicker').textContent = titulos[vistaActual][0];
    document.getElementById('vistaTitulo').firstChild.textContent = titulos[vistaActual][1] + ' ';
    if (piscina) cargarPiscina(); else cargarVisitas();
}

async function cargarVisitas() {
    const lista = document.getElementById('listaVisitas');
    try {
        const visitas = await pedir(`/api/accesos/visitas/${vistaActual === 'hoy' ? 'hoy' : 'dentro'}`);
        document.getElementById('contadorVista').textContent = visitas.length;
        if (!visitas.length) {
            lista.innerHTML = `<li class="gsz-vacio">
                <svg viewBox="0 0 24 24"><path d="M12 3.5 19 6v6c0 5-3 8-7 8.5-4-.5-7-3.5-7-8.5V6z"/><path d="M9.5 12l1.8 1.8L15 10"/></svg>
                ${vistaActual === 'hoy' ? 'Hoy no han ingresado visitantes.' : 'No hay visitantes dentro del conjunto.'}</li>`;
            return;
        }
        lista.innerHTML = visitas.map(v => {
            const entrada = tiempo(v.entrada);
            const salio = !!v.salida;
            const vehiculo = v.tipo === 'VEHICULO'
                ? `<span class="gsz-placa">${esc(v.placa)}</span><span class="gsz-visita-dato">Parq. ${esc(v.numeroParqueadero || '—')}</span>`
                : '<span class="gsz-visita-dato">A pie</span>';
            return `<li class="gsz-visita${salio ? ' salio' : ''}">
                <span class="gsz-visita-avatar">${esc(iniciales(v.nombreCompleto))}</span>
                <div class="gsz-visita-info">
                    <b>${esc(v.nombreCompleto)}</b>
                    <div class="gsz-visita-meta">
                        <span class="gsz-apto">${esc(v.numeroApartamento)}</span>${vehiculo}
                        <span class="gsz-visita-dato gsz-mono">${esc(v.numeroDocumento)}</span>
                    </div>
                </div>
                <div class="gsz-visita-hora">
                    <b>${entrada.hora}</b>
                    <small>${salio ? 'Salió ' + tiempo(v.salida).hora : entrada.hace}</small>
                </div>
                ${salio ? '' : `<button type="button" class="gsz-btn gsz-btn-borde gsz-btn-salida"
                    onclick="registrarSalida('${v.tipo}', '${v.id}', this)">Salida</button>`}
            </li>`;
        }).join('');
    } catch (error) {
        lista.innerHTML = `<li class="gsz-vacio">No se pudo cargar la lista: ${esc(error.message)}</li>`;
    }
}

async function registrarSalida(tipo, id, boton) {
    boton.disabled = true;
    try {
        aviso('ingresoAviso', await enviarJson(`/api/accesos/visitas/${tipo}/${id}/salida`, 'PUT'), false);
        await cargarVisitas();
    } catch (error) {
        boton.disabled = false;
        aviso('ingresoAviso', error.message, true);
    }
}

// ── Piscina ─────────────────────────────────────────────────────────────────

async function cargarPiscina() {
    const tbody = document.getElementById('historialPiscinaBody');
    try {
        const accesos = await pedir('/api/accesos/piscina/todos');
        document.getElementById('contadorVista').textContent = accesos.length;
        if (!accesos.length) {
            tbody.innerHTML = '<tr><td colspan="4"><div class="gsz-vacio">No hay accesos a la piscina registrados.</div></td></tr>';
            return;
        }
        accesos.sort((a, b) => String(b.fechaHora).localeCompare(String(a.fechaHora)));
        tbody.innerHTML = accesos.map(a => `<tr>
            <td class="px-4 py-3"><span class="block text-sm font-semibold">${esc(a.nombreCompleto || '—')}</span>
                <span class="block text-xs gsz-mono">${esc(a.numeroDocumento)}</span></td>
            <td class="px-4 py-3"><span class="gsz-apto">${esc(a.numeroApartamento || '—')}</span></td>
            <td class="px-4 py-3 text-xs">${a.fechaHora ? new Date(a.fechaHora).toLocaleString('es-CO',
                { day: '2-digit', month: 'short', hour: 'numeric', minute: '2-digit' }) : '—'}</td>
            <td class="px-4 py-3 text-center">
                <button type="button" class="text-xs font-semibold" onclick="abrirModificarPiscina('${a.id}', '${esc(a.numeroDocumento)}', '${esc(a.numeroApartamento)}')"><i class="fas fa-edit mr-1"></i>Modificar</button>
                <button type="button" class="text-xs font-semibold text-red-500" onclick="eliminarPiscina('${a.id}', this)"><i class="fas fa-trash mr-1"></i>Eliminar</button>
            </td></tr>`).join('');
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="4"><div class="gsz-vacio">No se pudo cargar: ${esc(error.message)}</div></td></tr>`;
    }
}

async function registrarPiscina(event) {
    event.preventDefault();
    const form = event.target;
    try {
        const mensaje = await enviarJson('/api/accesos/piscina/registrar', 'POST', {
            numeroDocumento: document.getElementById('documentoPiscina').value.toUpperCase().replace(/[^A-Z0-9]/g, ''),
            numeroApartamento: document.getElementById('apartamentoPiscina').value.trim().toUpperCase()
        });
        aviso('piscinaAviso', mensaje, false);
        form.reset();
        await cargarPiscina();
    } catch (error) {
        aviso('piscinaAviso', error.message, true);
    }
    return false;
}

function abrirModificarPiscina(id, documento, apartamento) {
    document.getElementById('idAccesoModificar').value = id;
    document.getElementById('documentoPiscinaModificar').value = documento;
    document.getElementById('apartamentoPiscinaModificar').value = apartamento;
    aviso('modificarPiscinaAviso', '');
    document.getElementById('modificarPiscinaModal').classList.add('show');
}

function cerrarModificarPiscina() {
    document.getElementById('modificarPiscinaModal').classList.remove('show');
}

async function guardarPiscina(event) {
    event.preventDefault();
    try {
        const mensaje = await enviarJson(`/api/accesos/piscina/modificar/${document.getElementById('idAccesoModificar').value}`, 'PUT', {
            numeroDocumento: document.getElementById('documentoPiscinaModificar').value.toUpperCase().replace(/[^A-Z0-9]/g, ''),
            numeroApartamento: document.getElementById('apartamentoPiscinaModificar').value.trim().toUpperCase()
        });
        cerrarModificarPiscina();
        aviso('piscinaAviso', mensaje, false);
        await cargarPiscina();
    } catch (error) {
        aviso('modificarPiscinaAviso', error.message, true);
    }
    return false;
}

async function eliminarPiscina(id, boton) {
    if (!confirm('¿Eliminar este registro de acceso a la piscina?')) return;
    boton.disabled = true;
    try {
        aviso('piscinaAviso', await enviarJson(`/api/accesos/piscina/eliminar/${id}`, 'DELETE'), false);
        await cargarPiscina();
    } catch (error) {
        boton.disabled = false;
        aviso('piscinaAviso', error.message, true);
    }
}

// ── Sesión ──────────────────────────────────────────────────────────────────

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) {
        sessionStorage.clear();
        window.location.href = '/logout';
    }
    return false;
}

// ── Arranque ────────────────────────────────────────────────────────────────

window.addEventListener('DOMContentLoaded', () => {
    document.getElementById('docVisitante').addEventListener('input', alEscribirDocumento);
    document.getElementById('placaVisitante').addEventListener('input', e => {
        e.target.value = e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    });
    cargarVisitas();
    // La lista "dentro ahora" se refresca sola (otro vigilante pudo registrar algo)
    setInterval(() => { if (vistaActual !== 'piscina' && !document.hidden) cargarVisitas(); }, 30000);
});
