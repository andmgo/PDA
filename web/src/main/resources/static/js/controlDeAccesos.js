let visitantesConVehiculo = [];
let visitantesSinVehiculo = [];
let accesosPiscina = [];
let currentSalidaId = null;
let currentSalidaTipo = null;
let apartamentosDB = [];

// ========== FUNCIONES DE VALIDACIÓN ==========
function validarSoloNumeros(input, errorSpanId) {
    const valor = input.value;
    const errorSpan = document.getElementById(errorSpanId);
    
    if (!errorSpan) return true;
    
    // Limpiar caracteres no numéricos
    if (!/^\d*$/.test(valor)) {
        input.value = valor.replace(/\D/g, '');
        errorSpan.textContent = 'Solo se permiten números';
        return false;
    }
    
    if (valor === '') {
        errorSpan.textContent = '';
        return true;
    }
    
    if (valor.length < 8) {
        errorSpan.textContent = 'El documento debe tener al menos 8 dígitos';
        return false;
    }
    
    errorSpan.textContent = '';
    return true;
}

function validarApartamentoNumero(input, errorSpanId) {
    const valor = input.value;
    const errorSpan = document.getElementById(errorSpanId);
    
    if (!errorSpan) return true;
    
    // Limpiar caracteres no numéricos
    if (!/^\d*$/.test(valor)) {
        input.value = valor.replace(/\D/g, '');
        errorSpan.textContent = 'Solo se permiten números';
        return false;
    }
    
    errorSpan.textContent = '';
    return true;
}

function validarFormatoParqueadero(input, errorSpanId) {
    const valor = input.value.toUpperCase();
    const errorSpan = document.getElementById(errorSpanId);
    
    if (!errorSpan) return true;
    
    // Convertir a mayúsculas automáticamente
    input.value = valor;
    
    if (valor === '') {
        errorSpan.textContent = '';
        return true;
    }
    
    // Validar formato P-XX o P-XXX
    if (!/^P-\d{2,3}$/.test(valor)) {
        errorSpan.textContent = 'Formato inválido. Use: P-01, P-02, etc.';
        return false;
    }
    
    errorSpan.textContent = '';
    return true;
}

// Cargar apartamentos desde la API
async function cargarApartamentos() {
    try {
        const response = await fetch('/api/apartamentos/todos');
        const apartamentos = await response.json();
        apartamentosDB = apartamentos.map(apt => apt.numero);
        console.log('Apartamentos cargados:', apartamentosDB);
    } catch (error) {
        console.error('Error al cargar apartamentos:', error);
    }
}

// Cargar datos desde el backend
async function cargarVisitantesConVehiculo() {
    try {
        const response = await fetch('/api/accesos/visitante-vehiculo/todos');
        if (response.ok) {
            visitantesConVehiculo = await response.json();
            console.log('Visitantes con vehículo recibidos:', visitantesConVehiculo);
            renderVisitantesConVehiculo();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function cargarVisitantesSinVehiculo() {
    try {
        const response = await fetch('/api/accesos/visitante-sin-vehiculo/todos');
        if (response.ok) {
            visitantesSinVehiculo = await response.json();
            console.log('Visitantes sin vehículo recibidos:', visitantesSinVehiculo);
            renderVisitantesSinVehiculo();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function cargarHistorialPiscina() {
    try {
        const response = await fetch('/api/accesos/piscina/todos');
        if (response.ok) {
            accesosPiscina = await response.json();
            console.log('Datos de piscina recibidos:', accesosPiscina);
            renderHistorialPiscina();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function openTab(evt, tabName) {
    const tabContents = document.getElementsByClassName('tab-content');
    for (let i = 0; i < tabContents.length; i++) {
        tabContents[i].classList.remove('active');
    }
    const tabButtons = document.getElementsByClassName('tab-button');
    for (let i = 0; i < tabButtons.length; i++) {
        tabButtons[i].classList.remove('active');
        tabButtons[i].style.color = '#94a3b8';
    }
    document.getElementById(tabName).classList.add('active');
    evt.currentTarget.classList.add('active');
    evt.currentTarget.style.color = '#334155';
}

function formatFecha(fechaStr) {
    if (!fechaStr) return '-';
    const fecha = new Date(fechaStr);
    const dia = fecha.getDate().toString().padStart(2, '0');
    const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
    const anio = fecha.getFullYear();
    const hora = fecha.getHours().toString().padStart(2, '0');
    const min = fecha.getMinutes().toString().padStart(2, '0');
    return `${dia}/${mes}/${anio} ${hora}:${min}`;
}

async function validarDocumentoVisitante(documento) {
    try {
        const numeroDocumento = parseInt(documento);
        const response = await fetch(`/api/residentes/${numeroDocumento}`);
        
        if (!response.ok) return false;
        
        const residente = await response.json();
        if (!residente) return false;
        
        // ✅ nombreTipoResidente es el campo correcto
        const tipoNombre = residente.nombreTipoResidente || 
                           residente.tipoResidente?.nombre || 
                           residente.tipo_residente || 
                           '';
        
        return tipoNombre.toString().toLowerCase().trim().includes('visitante');
        
    } catch (error) {
        console.error('Error al validar documento visitante:', error);
        return false;
    }
}

async function validarDocumentoResidente(documento) {
    try {
        const numeroDocumento = parseInt(documento);
        const response = await fetch(`/api/residentes/${numeroDocumento}`);
        
        if (response.ok) {
            const residente = await response.json();
            if (residente) {
                const tipoNombre = residente.nombreTipoResidente || 
                                   residente.tipoResidente?.nombre || '';
                // Es residente si existe y NO es visitante
                const esVisitante = tipoNombre.toLowerCase().includes('visitante');
                if (!esVisitante) return true;
            }
        }
        
        // También verificar si es usuario del sistema
        const responseUsr = await fetch(`/api/usuarios/${documento}`);
        if (responseUsr.ok) {
            const usuario = await responseUsr.json();
            if (usuario) return true;
        }
        
        return false;
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}

function validarApartamento(numero) {
    return apartamentosDB.includes(numero.toUpperCase());
}

// Modals
function openVisitanteVehiculoModal() {
    document.getElementById('visitanteVehiculoModal').classList.add('show');
    document.getElementById('visitanteVehiculoForm').reset();
    document.getElementById('errorDocumentoVehiculo').textContent = '';
    document.getElementById('errorApartamentoVehiculo').textContent = '';
    document.getElementById('errorParqueadero').textContent = '';
}

function closeVisitanteVehiculoModal() {
    document.getElementById('visitanteVehiculoModal').classList.remove('show');
    document.getElementById('visitanteVehiculoForm').reset();
}

function openVisitanteSinVehiculoModal() {
    document.getElementById('visitanteSinVehiculoModal').classList.add('show');
    document.getElementById('errorDocumentoSinVehiculo').textContent = '';
    document.getElementById('errorApartamentoSinVehiculo').textContent = '';
}

function closeVisitanteSinVehiculoModal() {
    document.getElementById('visitanteSinVehiculoModal').classList.remove('show');
    document.getElementById('visitanteSinVehiculoForm').reset();
}

function openAccesoPiscinaModal() {
    document.getElementById('accesoPiscinaModal').classList.add('show');
    document.getElementById('errorDocumentoPiscina').textContent = '';
    document.getElementById('errorApartamentoPiscina').textContent = '';
}

function closeAccesoPiscinaModal() {
    document.getElementById('accesoPiscinaModal').classList.remove('show');
    document.getElementById('accesoPiscinaForm').reset();
}

function openSalidaModal(id, tipo, documento) {
    currentSalidaId = id;
    currentSalidaTipo = tipo;
    document.getElementById('documentoSalida').textContent = documento;
    document.getElementById('salidaModal').classList.add('show');
}

function closeSalidaModal() {
    document.getElementById('salidaModal').classList.remove('show');
    currentSalidaId = null;
    currentSalidaTipo = null;
}

// Form Submissions
document.getElementById('visitanteVehiculoForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    console.log('📝 Formulario enviado');
    
    const documento = document.getElementById('documentoVehiculo').value;
    const apartamento = document.getElementById('apartamentoVehiculo').value.toUpperCase();
    const numeroParqueadero = document.getElementById('numeroParqueadero').value.toUpperCase();
    const placa = document.getElementById('placaVehiculo').value.toUpperCase();

     // Limpiar todos los errores
    document.getElementById('errorDocumentoVehiculo').textContent = '';
    document.getElementById('errorApartamentoVehiculo').textContent = '';
    document.getElementById('errorParqueadero').textContent = '';
    
    let hayErrores = false;
    
    // ✅ VALIDAR DOCUMENTO (mínimo 8 dígitos)
    if (!/^\d{8,}$/.test(documento)) {
        document.getElementById('errorDocumentoVehiculo').textContent = 'El documento debe tener al menos 8 dígitos numéricos';
        return;
    }
    
    // ✅ VALIDAR FORMATO PARQUEADERO
    if (!/^P-\d{2,3}$/.test(numeroParqueadero)) {
        document.getElementById('errorParqueadero').textContent = 'Formato inválido. Use: P-01, P-02, etc.';
        hayErrores = true;
    }
    
    // Si hay errores de formato, detener aquí
    if (hayErrores) return;
    
    console.log('Datos del formulario:', {documento, apartamento, numeroParqueadero, placa});
    
    console.log('Datos del formulario:', {documento, apartamento, numeroParqueadero, placa});

    console.log('Validando visitante...');
    const esVisitante = await validarDocumentoVisitante(documento);
    console.log('Resultado validación visitante:', esVisitante);
    
    if (!esVisitante) {
        console.log('❌ No es visitante');
        document.getElementById('errorDocumentoVehiculo').textContent = 'El documento no corresponde a un visitante registrado';
        return;
    }

    console.log('Validando apartamento...');
    if (!validarApartamento(apartamento)) {
        console.log('❌ Apartamento no existe');
        document.getElementById('errorApartamentoVehiculo').textContent = 'El apartamento no existe';
        return;
    }

    console.log('✅ Todas las validaciones pasaron, enviando al backend...');

    try {
        const datosEnvio = {
            numeroDocumento: parseInt(documento),
            numeroApartamento: apartamento,
            numeroParqueadero: numeroParqueadero,
            placa: placa
        };
        
        console.log('Datos a enviar:', datosEnvio);
        
        const response = await fetch('/api/accesos/visitante-vehiculo/registrar-entrada', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datosEnvio)
        });

        console.log('Respuesta del servidor:', response.status, response.statusText);
        const responseText = await response.text();
        console.log('Texto de respuesta:', responseText);
        
        if (!response.ok) {
            console.log('❌ Error del servidor');
            // ✅ MEJORADO: Detectar error de parqueadero
            const textoLower = responseText.toLowerCase();
            
            if (textoLower.includes('parqueadero') && (textoLower.includes('no existe') || textoLower.includes('not found'))) {
                document.getElementById('errorParqueadero').textContent = 'El parqueadero no existe o no está disponible';
            } else if (textoLower.includes('ocupado') || textoLower.includes('no disponible')) {
                document.getElementById('errorParqueadero').textContent = 'El parqueadero ya está ocupado';
            } else {
                // Mostrar error genérico
                alert('Error: ' + responseText);
            }
            return;
        }

        console.log('✅ Registro exitoso');
        await cargarVisitantesConVehiculo();
        closeVisitanteVehiculoModal();
        alert(responseText || 'Visitante registrado exitosamente');
    } catch (error) {
        console.error('❌ Error completo:', error);
        alert('Error al registrar el visitante: ' + error.message);}
});

document.getElementById('visitanteSinVehiculoForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const documento = document.getElementById('documentoSinVehiculo').value;
    const apartamento = document.getElementById('apartamentoSinVehiculo').value.toUpperCase();
    
    // ✅ VALIDAR DOCUMENTO
    if (!/^\d{8,}$/.test(documento)) {
        document.getElementById('errorDocumentoSinVehiculo').textContent = 'El documento debe tener al menos 8 dígitos numéricos';
        return;
    }
    
    document.getElementById('errorDocumentoSinVehiculo').textContent = '';
    document.getElementById('errorApartamentoSinVehiculo').textContent = '';

    const esVisitante = await validarDocumentoVisitante(documento);
    if (!esVisitante) {
        document.getElementById('errorDocumentoSinVehiculo').textContent = 'El documento no corresponde a un visitante registrado';
        return;
    }

    if (!validarApartamento(apartamento)) {
        document.getElementById('errorApartamentoSinVehiculo').textContent = 'El apartamento no existe';
        return;
    }

    try {
        const response = await fetch('/api/accesos/visitante-sin-vehiculo/registrar-entrada', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                numeroDocumento: parseInt(documento),
                numeroApartamento: apartamento
            })
        });

        const responseText = await response.text();
        
        if (!response.ok) {
            throw new Error(responseText || 'Error al registrar');
        }

        await cargarVisitantesSinVehiculo();
        closeVisitanteSinVehiculoModal();
        alert('Visitante registrado exitosamente');
    } catch (error) {
        console.error('Error completo:', error);
        alert('Error al registrar el visitante: ' + error.message);
    }
});

document.getElementById('accesoPiscinaForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const documento = document.getElementById('documentoPiscina').value;
    const apartamento = document.getElementById('apartamentoPiscina').value.toUpperCase();
    
    // ✅ VALIDAR DOCUMENTO
    if (!/^\d{8,}$/.test(documento)) {
        document.getElementById('errorDocumentoPiscina').textContent = 'El documento debe tener al menos 8 dígitos numéricos';
        return;
    }
    
    document.getElementById('errorDocumentoPiscina').textContent = '';
    document.getElementById('errorApartamentoPiscina').textContent = '';

    const esResidente = await validarDocumentoResidente(documento);
    if (!esResidente) {
        document.getElementById('errorDocumentoPiscina').textContent = 'El documento no corresponde a un residente o usuario autorizado';
        return;
    }

    if (!validarApartamento(apartamento)) {
        document.getElementById('errorApartamentoPiscina').textContent = 'El apartamento no existe';
        return;
    }

    try {
        const response = await fetch('/api/accesos/piscina/registrar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                numeroDocumento: parseInt(documento),
                numeroApartamento: apartamento
            })
        });

        const responseText = await response.text();
        
        if (!response.ok) {
            throw new Error(responseText || 'Error al registrar');
        }

        await cargarHistorialPiscina();
        closeAccesoPiscinaModal();
        alert('Acceso registrado exitosamente');
    } catch (error) {
        console.error('Error completo:', error);
        alert('Error al registrar el acceso: ' + error.message);
    }
});

async function confirmarSalida() {
    try {
        let response;
        
        if (currentSalidaTipo === 'vehiculo') {
            response = await fetch(`/api/accesos/visitante-vehiculo/registrar-salida/${currentSalidaId}`, {
                method: 'PUT'
            });
            if (response.ok) await cargarVisitantesConVehiculo();
        } else if (currentSalidaTipo === 'sinvehiculo') {
            response = await fetch(`/api/accesos/visitante-sin-vehiculo/registrar-salida/${currentSalidaId}`, {
                method: 'PUT'
            });
            if (response.ok) await cargarVisitantesSinVehiculo();
        }

        if (response && response.ok) {
            alert('Salida registrada exitosamente');
        } else {
            alert('Error al registrar la salida');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error al registrar la salida');
    }
    closeSalidaModal();
}

async function eliminarAccesoPiscina(id) {
    if (confirm('¿Está seguro de eliminar este registro?')) {
        try {
            const response = await fetch(`/api/accesos/piscina/eliminar/${id}`, { method: 'DELETE' });
            if (response.ok) {
                await cargarHistorialPiscina();
                alert('Registro eliminado exitosamente');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Error al eliminar');
        }
    }
}


function renderVisitantesConVehiculo() {
    const tbody = document.getElementById('visitantesVehiculoTableBody');
    tbody.innerHTML = '';
    
    if (!visitantesConVehiculo || visitantesConVehiculo.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="p-4 text-center text-slate-400">
                    No hay registros de visitantes con vehículo
                </td>
            </tr>
        `;
        return;
    }
    
    visitantesConVehiculo.forEach(v => {
        // ✅ Usar v.id que es el ID real del registro
        const doc = v.numeroDocumento || 'N/A';
        const apto = v.numeroApartamento || 'N/A';
        const fechaEntrada = v.fechaHoraEntrada;
        const fechaSalida = v.fechaHoraSalida;
        const id = v.id; // ✅ CORREGIDO
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-sm font-semibold">${doc}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${apto}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs font-semibold">${v.placa || 'N/A'}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${formatFecha(fechaEntrada)}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${fechaSalida ? formatFecha(fechaSalida) : '-'}</p>
            </td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                ${fechaSalida ? 
                    '<span class="text-xs text-slate-400">Finalizado</span>' : 
                    `<button onclick="openSalidaModal('${id}', 'vehiculo', '${doc}')" 
                        class="text-xs font-semibold text-emerald-500 hover:text-emerald-700 mr-2">
                        <i class="fas fa-sign-out-alt mr-1"></i>Registrar Salida
                    </button>`
                }
            </td>
        `;
        tbody.appendChild(row);
    });
}

function renderVisitantesSinVehiculo() {
    const tbody = document.getElementById('visitantesSinVehiculoTableBody');
    tbody.innerHTML = '';
    
    if (!visitantesSinVehiculo || visitantesSinVehiculo.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="p-4 text-center text-slate-400">
                    No hay registros de visitantes sin vehículo
                </td>
            </tr>
        `;
        return;
    }
    
    visitantesSinVehiculo.forEach(v => {
        const doc = v.numeroDocumento || v.numero_documento || 'N/A';
        const apto = v.numeroApartamento || v.numero_apartamento || v.idApartamento || 'N/A';
        const fechaEntrada = v.fechaHoraEntrada || v.fecha_hora_entrada;
        const fechaSalida = v.fechaHoraSalida || v.fecha_hora_salida;
        const id = v.id;
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-sm font-semibold">${doc}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${apto}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${formatFecha(fechaEntrada)}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${fechaSalida ? formatFecha(fechaSalida) : '-'}</p>
            </td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                ${fechaSalida ? 
                    '<span class="text-xs text-slate-400">Finalizado</span>' : 
                    `<button onclick="openSalidaModal('${id}', 'sinvehiculo', '${doc}')" class="text-xs font-semibold text-emerald-500 hover:text-emerald-700">
                        <i class="fas fa-sign-out-alt mr-1"></i>Registrar Salida
                    </button>`
                }
            </td>
        `;
        tbody.appendChild(row);
    });
}

function renderHistorialPiscina() {
    const tbody = document.getElementById('historialPiscinaBody');
    tbody.innerHTML = '';
    
    if (!accesosPiscina || accesosPiscina.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="p-4 text-center text-slate-400">
                    No hay registros de acceso a piscina
                </td>
            </tr>
        `;
        return;
    }
    
    accesosPiscina.forEach(acceso => {
        // ✅ camelCase igual que los otros endpoints
        const doc = acceso.numeroDocumento || acceso.numero_documento || 'N/A';
        const apto = acceso.numeroApartamento || acceso.numero_apartamento || 'N/A';
        const fecha = acceso.fechaHora || acceso.fecha_hora;
        const id = acceso.id || acceso.id_acceso_piscina;
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-sm font-semibold">${doc}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${apto}</p>
            </td>
            <td class="p-2 align-middle bg-transparent border-b">
                <p class="px-6 mb-0 text-xs">${formatFecha(fecha)}</p>
            </td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="openModificarPiscinaModal('${id}', '${doc}', '${apto}')" 
                    class="text-xs font-semibold text-blue-500 hover:text-blue-700 mr-3">
                    <i class="fas fa-edit mr-1"></i>Modificar
                </button>
                <button onclick="eliminarAccesoPiscina('${id}')" 
                    class="text-xs font-semibold text-red-500 hover:text-red-700">
                    <i class="fas fa-trash mr-1"></i>Eliminar
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function verificarAcceso() {
    const apto = document.getElementById('searchApartamento').value.trim().toUpperCase();
    const resultado = document.getElementById('resultadoVerificacion');
    
    if (!apto) {
        resultado.innerHTML = '<p class="text-sm text-red-500"><i class="fas fa-exclamation-circle mr-2"></i>Ingrese un número de apartamento</p>';
        return;
    }

    if (!validarApartamento(apto)) {
        resultado.innerHTML = '<p class="text-sm text-red-500"><i class="fas fa-times-circle mr-2"></i>Apartamento no encontrado</p>';
        return;
    }

    resultado.innerHTML = `
        <div class="p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <p class="text-sm font-semibold text-blue-700">
                <i class="fas fa-info-circle mr-2"></i>Apartamento ${apto} encontrado
            </p>
        </div>
    `;
}

// Funciones para modificar acceso piscina
function openModificarPiscinaModal(id, documento, apartamento) {
    document.getElementById('modificarPiscinaModal').classList.add('show');
    document.getElementById('idAccesoModificar').value = id;
    document.getElementById('documentoPiscinaModificar').value = documento;
    document.getElementById('apartamentoPiscinaModificar').value = apartamento;
    document.getElementById('errorDocumentoPiscinaModificar').textContent = '';
    document.getElementById('errorApartamentoPiscinaModificar').textContent = '';
}

function closeModificarPiscinaModal() {
    document.getElementById('modificarPiscinaModal').classList.remove('show');
    document.getElementById('modificarPiscinaForm').reset();
}

// Form submission para modificar
document.getElementById('modificarPiscinaForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const id = document.getElementById('idAccesoModificar').value;
    const documento = document.getElementById('documentoPiscinaModificar').value;
    const apartamento = document.getElementById('apartamentoPiscinaModificar').value.toUpperCase();
    
    // ✅ VALIDAR DOCUMENTO
    if (!/^\d{8,}$/.test(documento)) {
        document.getElementById('errorDocumentoPiscinaModificar').textContent = 'El documento debe tener al menos 8 dígitos numéricos';
        return;
    }
    
    document.getElementById('errorDocumentoPiscinaModificar').textContent = '';
    document.getElementById('errorApartamentoPiscinaModificar').textContent = '';

    const esResidente = await validarDocumentoResidente(documento);
    if (!esResidente) {
        document.getElementById('errorDocumentoPiscinaModificar').textContent = 'El documento no corresponde a un residente o usuario autorizado';
        return;
    }

    if (!validarApartamento(apartamento)) {
        document.getElementById('errorApartamentoPiscinaModificar').textContent = 'El apartamento no existe';
        return;
    }

    try {
        const response = await fetch(`/api/accesos/piscina/modificar/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                numeroDocumento: parseInt(documento),
                numeroApartamento: apartamento
            })
        });

        const responseText = await response.text();
        
        if (!response.ok) {
            throw new Error(responseText || 'Error al modificar');
        }

        await cargarHistorialPiscina();
        closeModificarPiscinaModal();
        alert('Acceso modificado exitosamente');
    } catch (error) {
        console.error('Error completo:', error);
        alert('Error al modificar el acceso: ' + error.message);
    }
});

// ========== VALIDACIÓN EN TIEMPO REAL ==========
window.addEventListener('DOMContentLoaded', function() {
    console.log('Iniciando carga de datos...');
    cargarApartamentos();
    cargarVisitantesConVehiculo();
    cargarVisitantesSinVehiculo();
    cargarHistorialPiscina();
    
    // Visitante con vehículo
    const docVehiculo =document.getElementById('documentoVehiculo');
if (docVehiculo) {
docVehiculo.addEventListener('input', function() {
validarSoloNumeros(this, 'errorDocumentoVehiculo');
});
}
// Visitante sin vehículo
const docSinVehiculo = document.getElementById('documentoSinVehiculo');
if (docSinVehiculo) {
    docSinVehiculo.addEventListener('input', function() {
        validarSoloNumeros(this, 'errorDocumentoSinVehiculo');
    });
}

// Piscina
const docPiscina = document.getElementById('documentoPiscina');
if (docPiscina) {
    docPiscina.addEventListener('input', function() {
        validarSoloNumeros(this, 'errorDocumentoPiscina');
    });
}

// Modificar piscina
const docPiscinaModificar = document.getElementById('documentoPiscinaModificar');
if (docPiscinaModificar) {
    docPiscinaModificar.addEventListener('input', function() {
        validarSoloNumeros(this, 'errorDocumentoPiscinaModificar');
    });
// ✅ NUEVAS VALIDACIONES PARA APARTAMENTOS
    const aptoVehiculo = document.getElementById('apartamentoVehiculo');
    if (aptoVehiculo) {
        aptoVehiculo.addEventListener('input', function() {
            validarApartamentoNumero(this, 'errorApartamentoVehiculo');
        });
    }

    const aptoSinVehiculo = document.getElementById('apartamentoSinVehiculo');
    if (aptoSinVehiculo) {
        aptoSinVehiculo.addEventListener('input', function() {
            validarApartamentoNumero(this, 'errorApartamentoSinVehiculo');
        });
    }

    const aptoPiscina = document.getElementById('apartamentoPiscina');
    if (aptoPiscina) {
        aptoPiscina.addEventListener('input', function() {
            validarApartamentoNumero(this, 'errorApartamentoPiscina');
        });
    }

    const aptoPiscinaModificar = document.getElementById('apartamentoPiscinaModificar');
    if (aptoPiscinaModificar) {
        aptoPiscinaModificar.addEventListener('input', function() {
            validarApartamentoNumero(this, 'errorApartamentoPiscinaModificar');
        });
    }
}

// Validación de parqueadero
const numParqueadero = document.getElementById('numeroParqueadero');
if (numParqueadero) {
    numParqueadero.addEventListener('input', function() {
        validarFormatoParqueadero(this, 'errorParqueadero');
    });
}
});
function confirmarCerrarSesion(event) {
event.preventDefault();
if (confirm('¿Está seguro que desea cerrar sesión?')) {
    sessionStorage.clear();
    window.location.href = '/logout';
}

return false;
}

