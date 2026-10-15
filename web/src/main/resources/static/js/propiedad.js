// ══════════════════════════════════════════════════════════════════════════════
//  propiedad.js — Gestión de Datos › Apartamentos › "Propietarios y vehículos"
//  Vincula propietarios (usuarios con cuenta) y registra los vehículos de cada
//  apartamento. Es la base de las visitas pre-autorizadas, "mis paquetes" y el
//  lector de placas de portería. Habla con /api/propiedad/** (ver
//  PropiedadRestController en gescazone-api).
// ══════════════════════════════════════════════════════════════════════════════

let propiedadApartamentoId = null;

function escaparHtml(texto) {
    const div = document.createElement('div');
    div.textContent = texto == null ? '' : String(texto);
    return div.innerHTML;
}

function mensajePropiedad(texto, esError) {
    const caja = document.getElementById('propiedadMensaje');
    caja.textContent = texto || '';
    caja.className = 'gsz-propiedad-msg' + (texto ? (esError ? ' error' : ' ok') : '');
}

async function openPropiedadModal(idApartamento, numero) {
    propiedadApartamentoId = idApartamento;
    document.getElementById('propiedadTitulo').textContent = numero;
    mensajePropiedad('');
    document.getElementById('propiedadModal').classList.add('show');
    await cargarPropiedad();
}

function closePropiedadModal() {
    document.getElementById('propiedadModal').classList.remove('show');
    propiedadApartamentoId = null;
}

async function cargarPropiedad() {
    try {
        const response = await fetch(`/api/propiedad/apartamentos/${propiedadApartamentoId}`);
        if (!response.ok) throw new Error(await response.text());
        const detalle = await response.json();
        pintarPropietarios(detalle.propietarios);
        pintarVehiculos(detalle.vehiculos);
    } catch (error) {
        mensajePropiedad('No se pudo cargar la información: ' + error.message, true);
    }
}

function pintarPropietarios(propietarios) {
    const lista = document.getElementById('propiedadPropietarios');
    if (!propietarios.length) {
        lista.innerHTML = '<li class="vacio">Sin propietarios vinculados.</li>';
        return;
    }
    lista.innerHTML = propietarios.map(p => `
        <li>
            <div>
                <b>${escaparHtml(p.nombre)} ${escaparHtml(p.apellido)}</b>
                <small><span class="gsz-mono">${escaparHtml(p.numeroDocumento)}</span> · ${escaparHtml(p.correo)}</small>
            </div>
            <button type="button" class="gsz-propiedad-quitar" onclick="quitarPropietario('${p.id}', this)">Desvincular</button>
        </li>`).join('');
}

function pintarVehiculos(vehiculos) {
    const lista = document.getElementById('propiedadVehiculos');
    if (!vehiculos.length) {
        lista.innerHTML = '<li class="vacio">Sin vehículos registrados.</li>';
        return;
    }
    lista.innerHTML = vehiculos.map(v => {
        const detalle = [v.tipo === 'MOTO' ? 'Moto' : 'Carro', v.marca, v.color,
                         v.numeroParqueadero ? 'Parq. ' + v.numeroParqueadero : null]
            .filter(Boolean).map(escaparHtml).join(' · ');
        return `
        <li class="${v.activo ? '' : 'inactivo'}">
            <div>
                <b class="gsz-placa">${escaparHtml(v.placa)}</b>
                <small>${detalle}${v.activo ? '' : ' · Desactivado'}</small>
            </div>
            ${v.activo ? `<button type="button" class="gsz-propiedad-quitar" onclick="desactivarVehiculo('${v.id}', this)">Desactivar</button>` : ''}
        </li>`;
    }).join('');
}

async function enviarPropiedad(url, metodo, cuerpo) {
    const response = await fetch(url, {
        method: metodo,
        headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
        body: cuerpo ? JSON.stringify(cuerpo) : undefined
    });
    const texto = await response.text();
    if (!response.ok) throw new Error(texto || 'Error ' + response.status);
    return texto;
}

async function submitPropietario(event) {
    event.preventDefault();
    const input = document.getElementById('propietarioIdentificador');
    try {
        const mensaje = await enviarPropiedad(
            `/api/propiedad/apartamentos/${propiedadApartamentoId}/propietarios`, 'POST',
            { identificador: input.value.trim() });
        input.value = '';
        mensajePropiedad(mensaje, false);
        await cargarPropiedad();
    } catch (error) {
        mensajePropiedad(error.message, true);
    }
    return false;
}

async function quitarPropietario(idUsuario, boton) {
    if (!confirm('¿Desvincular a este propietario del apartamento?')) return;
    boton.disabled = true;
    try {
        mensajePropiedad(await enviarPropiedad(
            `/api/propiedad/apartamentos/${propiedadApartamentoId}/propietarios/${idUsuario}`, 'DELETE'), false);
        await cargarPropiedad();
    } catch (error) {
        boton.disabled = false;
        mensajePropiedad(error.message, true);
    }
}

async function submitVehiculo(event) {
    event.preventDefault();
    const campos = ['vehiculoPlaca', 'vehiculoMarca', 'vehiculoColor', 'vehiculoParqueadero'];
    const valor = id => document.getElementById(id).value.trim();
    try {
        const mensaje = await enviarPropiedad(
            `/api/propiedad/apartamentos/${propiedadApartamentoId}/vehiculos`, 'POST', {
                placa: valor('vehiculoPlaca'),
                tipo: document.getElementById('vehiculoTipo').value,
                marca: valor('vehiculoMarca'),
                color: valor('vehiculoColor'),
                numeroParqueadero: valor('vehiculoParqueadero')
            });
        campos.forEach(id => document.getElementById(id).value = '');
        mensajePropiedad(mensaje, false);
        await cargarPropiedad();
    } catch (error) {
        mensajePropiedad(error.message, true);
    }
    return false;
}

async function desactivarVehiculo(idVehiculo, boton) {
    if (!confirm('¿Desactivar este vehículo? Dejará de reconocerse en portería.')) return;
    boton.disabled = true;
    try {
        mensajePropiedad(await enviarPropiedad(`/api/propiedad/vehiculos/${idVehiculo}/desactivar`, 'PUT'), false);
        await cargarPropiedad();
    } catch (error) {
        boton.disabled = false;
        mensajePropiedad(error.message, true);
    }
}
