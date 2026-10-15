// Cambio de tabs
function switchTab(index) {
    const buttons = document.querySelectorAll('.tab-button');
    const contents = document.querySelectorAll('.tab-content');
    
    buttons.forEach((btn, i) => {
        if (i === index) {
            btn.classList.add('active', 'text-slate-700');
            btn.classList.remove('text-slate-400');
        } else {
            btn.classList.remove('active', 'text-slate-700');
            btn.classList.add('text-slate-400');
        }
    });
    
    contents.forEach((content, i) => {
        if (i === index) {
            content.classList.add('active');
        } else {
            content.classList.remove('active');
        }
    });
}

// Manejo del avatar
document.getElementById('avatarInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(event) {
            const avatarDisplay = document.getElementById('avatarDisplay');
            avatarDisplay.style.backgroundImage = `url(${event.target.result})`;
            avatarDisplay.style.backgroundSize = 'cover';
            avatarDisplay.style.backgroundPosition = 'center';
            avatarDisplay.textContent = '';
        };
        reader.readAsDataURL(file);
    }
});

// Cambio de contraseña — llama directo a /api/usuarios/cambiar-contrasena
// (proxied por ApiProxyController, que adjunta el JWT). El número de
// documento lo resuelve la Api desde ese JWT, nunca se manda desde acá.
document.getElementById('formCambiarContrasena').addEventListener('submit', async function (e) {
    e.preventDefault();

    const contrasenaActual = document.getElementById('contrasenaActual').value;
    const contrasenaNueva = document.getElementById('contrasenaNueva').value;
    const confirmarContrasenaNueva = document.getElementById('confirmarContrasenaNueva').value;

    if (contrasenaNueva.length < 6) {
        mostrarMensajeContrasena('La contraseña nueva debe tener al menos 6 caracteres.', true);
        return;
    }
    if (contrasenaNueva !== confirmarContrasenaNueva) {
        mostrarMensajeContrasena('Las contraseñas nuevas no coinciden.', true);
        return;
    }

    const resp = await fetch('/api/usuarios/cambiar-contrasena', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ contrasenaActual, contrasenaNueva, confirmarContrasena: confirmarContrasenaNueva })
    });

    if (resp.ok) {
        mostrarMensajeContrasena('Contraseña actualizada correctamente.', false);
        this.reset();
    } else {
        const texto = await resp.text();
        mostrarMensajeContrasena(texto || 'No se pudo cambiar la contraseña.', true);
    }
});

function mostrarMensajeContrasena(texto, esError) {
    const el = document.getElementById('mensajeContrasena');
    el.textContent = texto;
    el.className = 'mb-4 px-4 py-2 rounded-lg text-sm ' +
        (esError ? 'bg-red-50 text-red-600' : 'bg-emerald-50 text-emerald-600');
    setTimeout(() => { el.className = 'hidden mb-4 px-4 py-2 rounded-lg text-sm'; }, 4000);
}

// Actualizar último acceso
function updateLastAccess() {
    const now = new Date();
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    };
    
    const formatter = new Intl.DateTimeFormat('es-CO', options);
    const formattedDate = formatter.format(now);
    const finalDate = formattedDate.charAt(0).toUpperCase() + formattedDate.slice(1);
    
    document.getElementById('lastAccessTime').textContent = `Último acceso: ${finalDate}`;
}

updateLastAccess();
setInterval(updateLastAccess, 60000);
// ── Mi hogar: apartamentos y vehículos vinculados a la cuenta (Fase 2) ──
async function cargarMiHogar() {
    const caja = document.getElementById('miHogar');
    if (!caja) return;
    const esc = t => { const d = document.createElement('div'); d.textContent = t == null ? '' : String(t); return d.innerHTML; };
    try {
        const response = await fetch('/api/propiedad/mia');
        if (!response.ok) throw new Error();
        const apartamentos = await response.json();
        if (!apartamentos.length) {
            caja.innerHTML = '<p class="gsz-hogar-vacio">Aún no tienes apartamentos vinculados. La administración los asigna desde Gestión de Datos.</p>';
            return;
        }
        caja.innerHTML = apartamentos.map(a => {
            const vehiculos = a.vehiculos.filter(v => v.activo);
            return `<div class="gsz-hogar-item">
                <span class="gsz-apto">${esc(a.numeroApartamento)}</span>
                <div class="gsz-hogar-vehiculos">${vehiculos.length
                    ? vehiculos.map(v => `<span class="gsz-placa" title="${v.tipo === 'MOTO' ? 'Moto' : 'Carro'}">${esc(v.placa)}</span>`).join('')
                    : '<small>Sin vehículos registrados</small>'}</div>
            </div>`;
        }).join('');
    } catch (e) {
        caja.innerHTML = '<p class="gsz-hogar-vacio">No se pudo cargar esta información.</p>';
    }
}
document.addEventListener('DOMContentLoaded', cargarMiHogar);
