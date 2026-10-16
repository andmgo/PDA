// ══════════════════════════════════════════════════════════════════════════════
//  inicio.js — panel de inicio
//  Las tarjetas "Resumen de hoy" ya vienen filtradas por permiso desde la
//  plantilla; aquí cada una pide su dato real al API (data-kpi) y lo muestra.
// ══════════════════════════════════════════════════════════════════════════════

function updateDate() {
    const opciones = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    document.getElementById('currentDate').textContent = new Date().toLocaleDateString('es-CO', opciones);
}

/** Cómo se resume cada respuesta del API en un número (o una fecha). */
const RESUMEN = {
    largo: datos => ({ valor: datos.length }),
    pendientes: datos => ({ valor: datos.filter(p => !p.entregado).length }),
    esperados: datos => {
        const porLlegar = datos.filter(a => !a.ingresos).length;
        return { valor: porLlegar, sub: porLlegar === datos.length ? null : `${datos.length - porLlegar} ya ingresaron` };
    },
    vigentes: datos => ({ valor: datos.filter(a => a.estado === 'VIGENTE').length }),
    proxima: datos => {
        const proxima = datos.find(r => !r.pasada);
        if (!proxima) return { valor: 'Ninguna', texto: true, sub: 'reserva el salón social' };
        const fecha = new Date(proxima.fechaYHora);
        return {
            valor: fecha.toLocaleDateString('es-CO', { day: 'numeric', month: 'short' }),
            texto: true,
            sub: `${fecha.toLocaleDateString('es-CO', { weekday: 'long' })} · salón ${proxima.numeroSalon}`
        };
    }
};

async function cargarIndicador(tarjeta) {
    const valor = tarjeta.querySelector('.gsz-kpi-valor');
    try {
        const respuesta = await fetch(tarjeta.dataset.kpi);
        if (!respuesta.ok) throw new Error(respuesta.status);
        const resumen = RESUMEN[tarjeta.dataset.tipo](await respuesta.json());
        valor.textContent = resumen.valor;
        tarjeta.classList.toggle('gsz-kpi-texto', !!resumen.texto);
        tarjeta.classList.toggle('gsz-kpi-cero', resumen.valor === 0);
        if (resumen.sub) tarjeta.querySelector('.gsz-kpi-sub').textContent = resumen.sub;
    } catch (e) {
        valor.textContent = '—';
    }
}

function cargarResumen() {
    const tarjetas = document.querySelectorAll('[data-kpi]');
    if (!tarjetas.length) document.querySelector('.gsz-resumen')?.remove();
    tarjetas.forEach(cargarIndicador);
}

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) {
        sessionStorage.clear();
        window.location.href = '/logout';
    }
    return false;
}

updateDate();
cargarResumen();
setInterval(cargarResumen, 60000);
