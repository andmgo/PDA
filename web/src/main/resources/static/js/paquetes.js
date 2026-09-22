// ══════════════════════════════════════════════════════════════════════════════
//  paquetes.js — registro de paquetes de recepción (Funcionario/Administrador)
// ══════════════════════════════════════════════════════════════════════════════

async function cargarPaquetes() {
    const soloPendientes = document.getElementById('soloPendientes').checked;
    const url = soloPendientes ? '/api/paquetes/pendientes' : '/api/paquetes';
    try {
        const response = await fetch(url);
        if (!response.ok) {
            console.error('Error al cargar paquetes:', await response.text());
            return;
        }
        const paquetes = await response.json();
        mostrarPaquetes(paquetes);
    } catch (error) {
        console.error('Error al cargar paquetes:', error);
    }
}

function mostrarPaquetes(paquetes) {
    const tbody = document.getElementById('paquetesTableBody');
    tbody.innerHTML = '';
    if (paquetes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="p-8 text-center align-middle bg-transparent border-b">
            <div class="flex flex-col items-center justify-center text-gray-500">
                <i class="fas fa-box-open text-4xl mb-3 opacity-50"></i>
                <p class="text-sm font-semibold mb-1">No hay paquetes registrados</p>
            </div>
        </td></tr>`;
        return;
    }
    paquetes.forEach(p => {
        const llegada = new Date(p.fechaHoraLlegada).toLocaleString('es-CO', {
            year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
        });
        const estadoBadge = p.entregado
            ? '<span class="bg-gradient-to-tl from-emerald-500 to-teal-400 px-2.5 text-xs rounded-1.8 py-1.4 inline-block font-bold uppercase text-white">Entregado</span>'
            : '<span class="bg-gradient-to-tl from-yellow-500 to-orange-400 px-2.5 text-xs rounded-1.8 py-1.4 inline-block font-bold uppercase text-white">Pendiente</span>';
        const accion = p.entregado
            ? ''
            : `<button onclick="marcarEntregado('${p.id}')" class="text-xs font-semibold text-emerald-600 hover:text-emerald-800">
                   <i class="fas fa-check mr-1"></i>Marcar entregado
               </button>`;

        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-sm font-semibold">${p.idApartamento}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${p.nombreReceptor}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${p.cedulaReceptor}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${llegada}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">${estadoBadge}</td>
            <td class="p-2 text-center align-middle bg-transparent border-b">${accion}</td>`;
        tbody.appendChild(row);
    });
}

async function submitPaquete(event) {
    event.preventDefault();

    const numeroApartamento = document.getElementById('numeroApartamentoPaquete').value.trim();
    const nombreReceptor    = document.getElementById('nombreReceptor').value.trim();
    const cedulaReceptor    = document.getElementById('cedulaReceptor').value.trim();
    const errorSpan         = document.getElementById('apartamentoPaqueteError');
    errorSpan.textContent = '';

    try {
        const aptResponse = await fetch(`/api/apartamentos/${numeroApartamento}`);
        if (!aptResponse.ok) {
            errorSpan.textContent = 'No existe un apartamento con ese número';
            return false;
        }
        const apartamento = await aptResponse.json();

        const response = await fetch('/api/paquetes', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({
                idApartamento:  apartamento.id,
                nombreReceptor: nombreReceptor,
                cedulaReceptor: cedulaReceptor
            })
        });

        const result = await response.text();
        if (response.ok) {
            alert('✅ ' + result);
            document.getElementById('paqueteForm').reset();
            await cargarPaquetes();
        } else {
            alert('Error al registrar el paquete: ' + result);
        }
    } catch (error) {
        console.error('Error al registrar el paquete:', error);
        alert('Error al registrar el paquete: ' + error.message);
    }
    return false;
}

async function marcarEntregado(id) {
    if (!confirm('¿Confirmar la entrega de este paquete?')) return;
    try {
        const response = await fetch(`/api/paquetes/${id}/entregar`, { method: 'POST' });
        const result = await response.text();
        alert(result);
        if (response.ok) await cargarPaquetes();
    } catch (error) {
        console.error('Error al marcar el paquete como entregado:', error);
        alert('Error al marcar el paquete como entregado: ' + error.message);
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
