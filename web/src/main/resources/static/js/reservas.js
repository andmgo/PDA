// ══════════════════════════════════════════════════════════════════════════════
//  reservas.js
// ══════════════════════════════════════════════════════════════════════════════

let currentDeleteId = null;
let currentDate = new Date();
let reservasData = [];
let salonValidado = null;

// ── TABS ──────────────────────────────────────────────────────────────────────
document.querySelectorAll('.tab-button').forEach(button => {
    button.addEventListener('click', () => {
        const targetTab = button.dataset.tab;

        document.querySelectorAll('.tab-button').forEach(btn => {
            btn.classList.remove('active', 'text-slate-700');
            btn.classList.add('text-slate-400');
        });
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        button.classList.add('active', 'text-slate-700');
        button.classList.remove('text-slate-400');
        document.getElementById(targetTab).classList.add('active');

        if (targetTab === 'calendar') {
            renderCalendar();
        }
    });
});

// ── VALIDACIÓN EN TIEMPO REAL - SALÓN ─────────────────────────────────────────
let timeoutSalon;
document.getElementById('numeroSalon').addEventListener('input', function () {
    const numero      = this.value.trim();
    const errorSpan   = document.getElementById('salonError');
    const successSpan = document.getElementById('salonSuccess');

    salonValidado = null;
    successSpan.classList.add('hidden');
    errorSpan.textContent = '';

    if (numero === '') return;

    clearTimeout(timeoutSalon);
    timeoutSalon = setTimeout(async () => {
        try {
            const response = await fetch(`/api/salones/${numero}`);
            if (response.ok) {
                const salon = await response.json();
                salonValidado = salon;
                document.getElementById('medidasSalonEncontrado').textContent =
                    `${salon.medidas} m² - ${salon.estado?.nombre || 'Sin estado'}`;
                successSpan.classList.remove('hidden');
                errorSpan.textContent = '';
            } else {
                salonValidado = null;
                errorSpan.textContent = 'Salón no encontrado en el sistema';
                successSpan.classList.add('hidden');
            }
        } catch (error) {
            console.error('Error al validar salón:', error);
            salonValidado = null;
            errorSpan.textContent = 'Error al validar el salón';
        }
    }, 500);
});

// ── CARGAR RESERVAS ───────────────────────────────────────────────────────────
async function cargarReservas() {
    console.log('🔄 Intentando cargar reservas...');
    try {
        const response = await fetch('/api/reservas/todas');
        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ Error del servidor:', errorText);
            alert('Error al cargar reservas: ' + response.status + ' - ' + errorText);
            return;
        }
        reservasData = await response.json();
        console.log('✅ Reservas cargadas:', reservasData.length, 'reservas');
        mostrarReservas(reservasData);
        renderCalendar();
    } catch (error) {
        console.error('❌ Error en la solicitud:', error);
        alert('Error al cargar las reservas: ' + error.message);
    }
}

function mostrarReservas(reservas) {
    const tbody = document.getElementById('reservasTableBody');
    if (!tbody) {
        console.log('ℹ️ Tabla de historial no disponible (solo para Administradores)');
        return;
    }
    tbody.innerHTML = '';
    if (reservas.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td colspan="5" class="p-8 text-center align-middle bg-transparent border-b">
                <div class="flex flex-col items-center justify-center text-gray-500">
                    <i class="fas fa-calendar-times text-4xl mb-3 opacity-50"></i>
                    <p class="text-sm font-semibold mb-1">No hay reservas registradas</p>
                </div>
            </td>`;
        tbody.appendChild(row);
        return;
    }
    reservas.forEach(reserva => {
        const fecha = new Date(reserva.fechaYHoraReserva);
        const fechaFormateada = fecha.toLocaleString('es-CO', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-sm font-semibold">${reserva.idReserva}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${reserva.usuario.numeroDocumento}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${reserva.salon.numero}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${fechaFormateada}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="openDeleteModal('${reserva.idReserva}', 'Reserva #${reserva.idReserva}')" class="text-xs font-semibold text-red-500 hover:text-red-700">
                    <i class="fas fa-trash mr-1"></i>Eliminar
                </button>
            </td>`;
        tbody.appendChild(row);
    });
}

// ── SUBMIT RESERVA ────────────────────────────────────────────────────────────

/**
 * Se llama cuando el usuario pulsa "Confirmar Reserva". La reserva queda a
 * nombre de quien tiene la sesión iniciada — la Api resuelve el usuario
 * desde el JWT (ver ReservaSalonSocialRestController.crearPropia()), ya no
 * hace falta escribir un número de documento ni pasar por la verificación
 * de identidad con Google (el usuario del proyecto pidió quitar ese paso:
 * ya iniciaste sesión, no hace falta volver a probar quién sos para algo
 * tan simple como reservar un salón).
 */
async function submitReserva(event) {
    event.preventDefault();

    if (!salonValidado) {
        alert('Por favor, ingrese un número de salón válido');
        document.getElementById('numeroSalon').focus();
        return false;
    }

    const idSalon = salonValidado.idSalonSocial || salonValidado.id;

    if (!idSalon) {
        console.error('❌ Salón validado pero sin ID:', salonValidado);
        alert('Error: No se pudo obtener el ID del salón. Revise la consola.');
        return false;
    }

    const data = {
        idSalon:          idSalon,
        fechaYHoraReserva: document.getElementById('fechaHoraReserva').value
    };

    try {
        const response = await fetch('/api/reservas/crear-propia', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(data)
        });

        const result = await response.text();

        if (response.ok) {
            alert('✅ ' + result);
            resetForm();
            await cargarReservas();
            document.querySelector('[data-tab="calendar"]').click();
        } else {
            console.error('❌ Error del servidor:', result);
            alert('Error al crear la reserva: ' + result);
        }
    } catch (error) {
        console.error('❌ Error en la solicitud:', error);
        alert('Error al crear la reserva: ' + error.message);
    }

    return false;
}

// ── SOLICITUD DE RESERVA EXCEPCIONAL (OTRO AÑO) ───────────────────────────────
function openSolicitudModal() {
    if (!salonValidado) {
        alert('Primero ingrese y valide el número de salón.');
        return;
    }
    document.getElementById('solicitudModal').classList.add('show');
}

function closeSolicitudModal() {
    document.getElementById('solicitudModal').classList.remove('show');
    document.getElementById('solicitudForm').reset();
}

async function submitSolicitud(event) {
    event.preventDefault();

    const idSalon = salonValidado.idSalonSocial || salonValidado.id;

    const data = {
        idSalon:         idSalon,
        fechaSolicitada: document.getElementById('fechaSolicitud').value,
        justificacion:   document.getElementById('justificacionSolicitud').value
    };

    try {
        const response = await fetch('/api/solicitudes-reserva', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(data)
        });
        const result = await response.text();

        if (response.ok) {
            alert('✅ ' + result);
            closeSolicitudModal();
        } else {
            alert('Error al enviar la solicitud: ' + result);
        }
    } catch (error) {
        console.error('Error al enviar la solicitud:', error);
        alert('Error al enviar la solicitud: ' + error.message);
    }
    return false;
}

function resetForm() {
    document.getElementById('reservationForm').reset();
    salonValidado = null;
    document.getElementById('salonSuccess').classList.add('hidden');
    document.getElementById('salonError').textContent = '';
}

// ── CALENDARIO ────────────────────────────────────────────────────────────────
function renderCalendar() {
    const year  = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                        'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

    document.getElementById('currentMonth').textContent = `${monthNames[month]} ${year}`;

    const firstDay       = new Date(year, month, 1).getDay();
    const daysInMonth    = new Date(year, month + 1, 0).getDate();
    const daysInPrevMonth = new Date(year, month, 0).getDate();

    const calendarDays = document.getElementById('calendarDays');
    calendarDays.innerHTML = '';

    for (let i = firstDay - 1; i >= 0; i--) {
        const dayEl = document.createElement('div');
        dayEl.className = 'calendar-day other-month';
        dayEl.textContent = daysInPrevMonth - i;
        calendarDays.appendChild(dayEl);
    }

    const today = new Date();
    for (let day = 1; day <= daysInMonth; day++) {
        const dayEl  = document.createElement('div');
        dayEl.className = 'calendar-day';

        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        if (year === today.getFullYear() && month === today.getMonth() && day === today.getDate()) {
            dayEl.classList.add('today');
        }

        const dayReservations = reservasData.filter(r => {
            const reservaDate = new Date(r.fechaYHoraReserva);
            return reservaDate.getFullYear() === year &&
                   reservaDate.getMonth()    === month &&
                   reservaDate.getDate()     === day;
        });

        if (dayReservations.length > 0) {
            dayEl.classList.add('has-reservation');
            const dots = document.createElement('div');
            dots.className = 'reservation-dots';
            dayReservations.forEach(() => {
                const dot = document.createElement('div');
                dot.className = 'reservation-dot';
                dots.appendChild(dot);
            });
            dayEl.appendChild(document.createTextNode(day));
            dayEl.appendChild(dots);
        } else {
            dayEl.textContent = day;
        }

        dayEl.addEventListener('click', () => showDayReservations(dateStr, dayReservations));
        calendarDays.appendChild(dayEl);
    }

    const remainingDays = 42 - (firstDay + daysInMonth);
    for (let day = 1; day <= remainingDays; day++) {
        const dayEl = document.createElement('div');
        dayEl.className = 'calendar-day other-month';
        dayEl.textContent = day;
        calendarDays.appendChild(dayEl);
    }
}

function showDayReservations(dateStr, dayReservations) {
    const modal     = document.getElementById('dayModal');
    const modalBody = document.getElementById('dayModalBody');

    const date          = new Date(dateStr + 'T12:00:00');
    const formattedDate = date.toLocaleDateString('es-ES', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });

    let content = `<h4 class="mb-4 text-slate-700 font-semibold">Disponibilidad para ${formattedDate}</h4>`;

    if (dayReservations.length === 0) {
        content += `
            <div class="p-4 bg-green-50 rounded-lg mb-4">
                <p class="text-green-700"><i class="fas fa-check-circle mr-2"></i>Todos los salones están disponibles en esta fecha</p>
            </div>`;
    } else {
        content += `<div class="space-y-3">`;
        dayReservations.forEach(reserva => {
            const fecha = new Date(reserva.fechaYHoraReserva);
            const hora  = fecha.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
            content += `
                <div class="p-3 bg-red-50 border border-red-200 rounded-lg">
                    <div class="flex justify-between items-center mb-2">
                        <strong class="text-slate-700">${reserva.salon.numero}</strong>
                        <span class="text-xs bg-red-500 text-white px-2 py-1 rounded">Ocupado</span>
                    </div>
                    <div class="text-sm text-slate-600">
                        <p><i class="far fa-clock mr-2"></i>Hora: ${hora}</p>
                    </div>
                </div>`;
        });
        content += `</div>`;
    }

    content += `
        <button onclick="goToNewReservation()" class="mt-4 w-full px-6 py-2.5 text-sm font-bold text-white bg-gradient-to-tl from-blue-500 to-violet-500 rounded-lg hover:shadow-xs hover:-translate-y-px transition-all">
            <i class="fas fa-calendar-plus mr-2"></i>Reservar en esta fecha
        </button>`;

    modalBody.innerHTML = content;
    modal.classList.add('show');
}

function closeDayModal() { document.getElementById('dayModal').classList.remove('show'); }
function goToNewReservation() { closeDayModal(); document.querySelector('[data-tab="new"]').click(); }

document.getElementById('prevMonth').addEventListener('click', () => {
    currentDate.setMonth(currentDate.getMonth() - 1);
    renderCalendar();
});
document.getElementById('nextMonth').addEventListener('click', () => {
    currentDate.setMonth(currentDate.getMonth() + 1);
    renderCalendar();
});

// ── DELETE MODAL ──────────────────────────────────────────────────────────────
function openDeleteModal(id, name) {
    currentDeleteId = id;
    document.getElementById('deleteMessage').innerHTML =
        `¿Está seguro que desea eliminar <span class="font-bold">${name}</span>? Esta acción no se puede deshacer.`;
    document.getElementById('deleteModal').classList.add('show');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.remove('show');
    currentDeleteId = null;
}

async function confirmDelete() {
    if (!currentDeleteId) return;
    try {
        const response = await fetch(`/api/reservas/eliminar/${currentDeleteId}`, { method: 'DELETE' });
        const result   = await response.text();
        await cargarReservas();
        closeDeleteModal();
        alert(result);
    } catch (error) {
        console.error('Error al eliminar:', error);
        closeDeleteModal();
        alert('Error al eliminar la reserva');
    }
}

window.onclick = function (event) {
    if (event.target === document.getElementById('deleteModal'))    closeDeleteModal();
    if (event.target === document.getElementById('dayModal'))       closeDayModal();
    if (event.target === document.getElementById('solicitudModal')) closeSolicitudModal();
};

// ── INICIALIZACIÓN ────────────────────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', async function () {
    // Cargar reservas y renderizar calendario
    await cargarReservas();
    renderCalendar();

    // Fecha mínima: 48 horas desde ahora
    const ahora = new Date();
    ahora.setHours(ahora.getHours() + 48);
    document.getElementById('fechaHoraReserva').setAttribute('min', ahora.toISOString().slice(0, 16));
});

// ── Cerrar sesión ─────────────────────────────────────────────────────────────
function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) {
        sessionStorage.clear();
        // FIX: Spring Security 6 requiere POST para logout.
        // Se crea y envía un form dinámico en vez de un GET redirect.
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        document.body.appendChild(form);
        form.submit();
    }
    return false;
}