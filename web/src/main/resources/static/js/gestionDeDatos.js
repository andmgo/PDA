// ========== VARIABLES GLOBALES ==========
let currentEditNumber = null;
let currentDeleteNumber = null;
let currentDeleteType = null;

let apartamentosData = [];
let parqueaderosData = [];
let salonesData = [];
let usuariosData = [];
let residentesData = [];

let catalogos = {
    tiposDocumento: [],
    roles: [],
    estados: [],
    estadosCuenta: [],
    tiposOcupacion: [],
    tiposResidente: []
};

// ========== VALIDACIONES MEJORADAS ==========
function validarSoloNumeros(input, errorSpanId) {
    const valor = input.value;
    const errorSpan = document.getElementById(errorSpanId);
    
    if (!errorSpan) return true;
    
    if (valor === '') {
        errorSpan.textContent = '';
        return true;
    }
    
    if (!/^\d*$/.test(valor)) {
        errorSpan.textContent = 'Solo se permiten números';
        input.value = valor.replace(/\D/g, '');
        return false;
    }
    
    errorSpan.textContent = '';
    return true;
}

function validarTelefono(input, errorSpanId) {
    const valor = input.value;
    const errorSpan = document.getElementById(errorSpanId);
    if (!errorSpan) return true;
    if (!/^\d*$/.test(valor)) {
        input.value = valor.replace(/\D/g, '');
        errorSpan.textContent = 'Solo se permiten números';
        return false;
    }
    if (valor === '') { errorSpan.textContent = ''; return true; }
    if (valor.length < 10) {
        errorSpan.textContent = 'El teléfono debe tener 10 dígitos';
        return false;
    }
    if (valor.length > 10) {
        errorSpan.textContent = 'El teléfono no puede tener más de 10 dígitos';
        return false;
    }
    errorSpan.textContent = '';
    return true;
}

function validarDocumento(input, errorSpanId) {
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

function validarCorreo(input, errorSpanId) {
    const valor = input.value.trim();
    const errorSpan = document.getElementById(errorSpanId);
    if (!errorSpan) return true;
    if (valor === '') { errorSpan.textContent = ''; return true; }
    const regexCorreo = /^[a-zA-Z0-9._%+-]+@(gmail\.com|hotmail\.com|outlook\.com|yahoo\.com|live\.com|icloud\.com|protonmail\.com|mail\.com)$/i;
    if (!regexCorreo.test(valor)) {
        errorSpan.textContent = 'Ingrese un correo válido (gmail, hotmail, outlook, yahoo, etc.)';
        return false;
    }
    errorSpan.textContent = '';
    return true;
}

function validarContrasenas() {
    const password = document.getElementById('contrasenaUsuario').value;
    const confirmPassword = document.getElementById('repetirContrasena').value;
    const errorSpan = document.getElementById('contrasenaError');
    if (!errorSpan) return true;
    if (confirmPassword === '') { errorSpan.textContent = ''; return true; }
    if (password !== confirmPassword) {
        errorSpan.textContent = 'Las contraseñas no coinciden';
        return false;
    }
    if (password.length < 6) {
        errorSpan.textContent = 'La contraseña debe tener al menos 6 caracteres';
        return false;
    }
    errorSpan.textContent = '';
    return true;
}

function validarMedidas(input, errorSpanId) {
    const valor = input.value;
    const errorSpan = document.getElementById(errorSpanId);
    if (!errorSpan) return true;
    if (valor === '') { errorSpan.textContent = ''; return true; }
    if (!/^\d*\.?\d*$/.test(valor)) {
        errorSpan.textContent = 'Solo se permiten números';
        input.value = valor.replace(/[^\d.]/g, '');
        return false;
    }
    errorSpan.textContent = '';
    return true;
}

// ========== CARGAR CATÁLOGOS ==========
async function cargarCatalogos() {
    try {
        const response = await fetch('/api/catalogos/todos');
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const data = await response.json();
        catalogos.tiposDocumento = data.tiposDocumento || [];
        catalogos.roles = data.roles || [];
        catalogos.estados = data.estados || [];
        catalogos.estadosCuenta = data.estadosCuenta || [];
        catalogos.tiposOcupacion = data.tiposOcupacion || [];
        catalogos.tiposResidente = data.tiposResidente || [];
        poblarSelects();
        return true;
    } catch (error) {
        console.error('Error al cargar catálogos:', error);
        alert('Error al cargar los datos iniciales.');
        return false;
    }
}

function poblarSelects() {
    const selectTipoOcup = document.getElementById('tipoOcupacion');
    if (selectTipoOcup) {
        selectTipoOcup.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.tiposOcupacion.forEach(t => 
            selectTipoOcup.innerHTML += `<option value="${t.nombre}">${t.nombre}</option>`
        );
    }

    const selectEstadoCta = document.getElementById('estadoCuenta');
    if (selectEstadoCta) {
        selectEstadoCta.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.estadosCuenta.forEach(e =>
            selectEstadoCta.innerHTML += `<option value="${e.nombre}">${e.nombre}</option>`
        );
    }

    const selectEstadoPark = document.getElementById('estadoParqueadero');
    if (selectEstadoPark) {
        selectEstadoPark.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.estados.forEach(e =>
            selectEstadoPark.innerHTML += `<option value="${e.nombre}">${e.nombre}</option>`
        );
    }

    const selectEstadoSalon = document.getElementById('estadoSalon');
    if (selectEstadoSalon) {
        selectEstadoSalon.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.estados.forEach(e =>
            selectEstadoSalon.innerHTML += `<option value="${e.nombre}">${e.nombre}</option>`
        );
    }

    // Alta masiva
    const selectTipoOcupMasivo = document.getElementById('tipoOcupacionMasivo');
    if (selectTipoOcupMasivo) {
        selectTipoOcupMasivo.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.tiposOcupacion.forEach(t =>
            selectTipoOcupMasivo.innerHTML += `<option value="${t.nombre}">${t.nombre}</option>`
        );
    }
    const selectEstadoCtaMasivo = document.getElementById('estadoCuentaMasivo');
    if (selectEstadoCtaMasivo) {
        selectEstadoCtaMasivo.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.estadosCuenta.forEach(e =>
            selectEstadoCtaMasivo.innerHTML += `<option value="${e.nombre}">${e.nombre}</option>`
        );
    }
    const selectEstadoParkMasivo = document.getElementById('estadoParqueaderoMasivo');
    if (selectEstadoParkMasivo) {
        selectEstadoParkMasivo.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.estados.forEach(e =>
            selectEstadoParkMasivo.innerHTML += `<option value="${e.nombre}">${e.nombre}</option>`
        );
    }
    const selectEstadoSalonMasivo = document.getElementById('estadoSalonMasivo');
    if (selectEstadoSalonMasivo) {
        selectEstadoSalonMasivo.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.estados.forEach(e =>
            selectEstadoSalonMasivo.innerHTML += `<option value="${e.nombre}">${e.nombre}</option>`
        );
    }

    const selectTipoDocUsr = document.getElementById('tipoDocumentoUsuario');
    if (selectTipoDocUsr) {
        selectTipoDocUsr.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.tiposDocumento.forEach(t => 
            selectTipoDocUsr.innerHTML += `<option value="${t.nombre}">${t.nombre}</option>`
        );
    }

    const selectRol = document.getElementById('rolUsuario');
    if (selectRol) {
        selectRol.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.roles.forEach(r => 
            selectRol.innerHTML += `<option value="${r.nombre}">${r.nombre}</option>`
        );
    }

    const selectTipoDocRes = document.getElementById('tipoDocumentoResidente');
    if (selectTipoDocRes) {
        selectTipoDocRes.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.tiposDocumento.forEach(t => 
            selectTipoDocRes.innerHTML += `<option value="${t.nombre}">${t.nombre}</option>`
        );
    }

    const selectTipoRes = document.getElementById('tipoResidente');
    if (selectTipoRes) {
        selectTipoRes.innerHTML = '<option value="">Seleccionar...</option>';
        catalogos.tiposResidente.forEach(t => 
            selectTipoRes.innerHTML += `<option value="${t.nombre}">${t.nombre}</option>`
        );
    }
}

// ========== TABS ==========
function openTab(evt, tabName) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-button').forEach(b => { b.classList.remove('active', 'text-slate-700'); b.classList.add('text-slate-400'); });
    document.getElementById(tabName).classList.add('active');
    evt.currentTarget.classList.add('active', 'text-slate-700');
    evt.currentTarget.classList.remove('text-slate-400');
}

// ========== APARTAMENTOS ==========
async function cargarApartamentos() {
    try {
        const response = await fetch('/api/apartamentos/todos');
        apartamentosData = await response.json();
        mostrarApartamentos(apartamentosData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarApartamentos(apartamentos) {
    const tbody = document.getElementById('apartamentosTableBody');
    tbody.innerHTML = '';
    if (apartamentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="p-8 text-center">No se encontraron apartamentos</td></tr>';
        return;
    }
    apartamentos.forEach(apt => {
        const tipoOcup = apt.tipoOcupacion || 'N/A';
        const estadoCta = apt.estadoCuenta || 'N/A';
        const statusClass = estadoCta.toLowerCase().includes('día') ? 'from-emerald-500 to-teal-400' : 'from-red-600 to-orange-600';
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-sm font-semibold">${apt.numero}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs font-semibold">${tipoOcup}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b"><span class="bg-gradient-to-tl ${statusClass} px-2.5 text-xs rounded-1.8 py-1.4 inline-block font-bold uppercase text-white">${estadoCta}</span></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${apt.medidas} m²</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${apt.telefono}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="editApartamento('${apt.numero}')" class="text-xs font-semibold text-blue-500 hover:text-blue-700 mr-3"><i class="fas fa-edit mr-1"></i>Editar</button>
                <button onclick="openDeleteModal('${apt.numero}', 'apartamento', '${apt.numero}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-trash mr-1"></i>Eliminar</button>
            </td></tr>`;
    });
}

function buscarApartamentos() {
    const texto = document.getElementById('searchInputApartamentos').value.toLowerCase();
    mostrarApartamentos(apartamentosData.filter(a => a.numero.toLowerCase().includes(texto) || (a.tipoOcupacion?.nombre||'').toLowerCase().includes(texto) || (a.estadoCuenta?.nombre||'').toLowerCase().includes(texto) || a.telefono.toString().includes(texto)));
}

function openApartamentoModal() {
    currentEditNumber = null;
    document.getElementById('apartamentoModalTitle').textContent = 'Agregar Apartamento';
    document.getElementById('apartamentoForm').reset();
    ['numeroApartamentoError','medidasApartamentoError','telefonoApartamentoError'].forEach(id => { const s = document.getElementById(id); if(s) s.textContent = ''; });
    poblarSelects();
    document.getElementById('apartamentoModal').classList.add('show');
}

async function editApartamento(numero) {
    try {
        const response = await fetch(`/api/apartamentos/${numero}`);
        const apt = await response.json();
        currentEditNumber = numero;
        document.getElementById('apartamentoModalTitle').textContent = 'Editar Apartamento';
        document.getElementById('numeroApartamento').value = apt.numero;
        document.getElementById('tipoOcupacion').value = apt.idTipoOcupacion || '';
        document.getElementById('estadoCuenta').value = apt.idEstadoCuenta || '';
        document.getElementById('medidasApartamento').value = apt.medidas;
        document.getElementById('telefonoApartamento').value = apt.telefono;
        ['numeroApartamentoError','medidasApartamentoError','telefonoApartamentoError'].forEach(id => { const s = document.getElementById(id); if(s) s.textContent = ''; });
        document.getElementById('apartamentoModal').classList.add('show');
    } catch (error) { alert('Error al cargar el apartamento'); }
}

function closeApartamentoModal() {
    document.getElementById('apartamentoModal').classList.remove('show');
}

async function submitApartamento(event) {
    event.preventDefault();
    const numero = document.getElementById('numeroApartamento').value;
    const medidas = document.getElementById('medidasApartamento').value;
    const telefono = document.getElementById('telefonoApartamento').value;
    let hayErrores = false;
    
    if (!/^\d+$/.test(numero)) { document.getElementById('numeroApartamentoError').textContent = 'Solo se permiten números'; hayErrores = true; }
    else { document.getElementById('numeroApartamentoError').textContent = ''; }
    
    if (!/^\d+\.?\d*$/.test(medidas)) { document.getElementById('medidasApartamentoError').textContent = 'Solo se permiten números'; hayErrores = true; }
    else { document.getElementById('medidasApartamentoError').textContent = ''; }
    
    if (!/^\d{10}$/.test(telefono)) { document.getElementById('telefonoApartamentoError').textContent = 'El teléfono debe tener exactamente 10 dígitos'; hayErrores = true; }
    else { document.getElementById('telefonoApartamentoError').textContent = ''; }
    
    if (hayErrores) return false;

    const data = { 
    numero, 
    tipoOcupacion: document.getElementById('tipoOcupacion').value,
    estadoCuenta: document.getElementById('estadoCuenta').value,
    medidas, 
    telefono: parseInt(telefono) 
    };

    try {
        const url = currentEditNumber ? '/api/apartamentos/actualizar' : '/api/apartamentos/crear';
        const response = await fetch(url, { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) });
        if (!response.ok) throw new Error(await response.text());
        alert(await response.text());
        closeApartamentoModal();
        await cargarApartamentos();
    } catch (error) { alert('Error: ' + error.message); }
    return false;
}

// ========== ALTA MASIVA (helper compartido) ==========
function resumenAltaMasiva(resultado) {
    let mensaje = `Se crearon ${resultado.creados.length} de ${resultado.creados.length + resultado.omitidos.length}.`;
    if (resultado.omitidos.length > 0) {
        mensaje += '\n\nOmitidos:\n' + resultado.omitidos.map(o => `- ${o.numero}: ${o.motivo}`).join('\n');
    }
    alert(mensaje);
}

function openApartamentoMasivoModal() {
    document.getElementById('apartamentoMasivoForm').reset();
    poblarSelects();
    document.getElementById('apartamentoMasivoModal').classList.add('show');
}

function closeApartamentoMasivoModal() {
    document.getElementById('apartamentoMasivoModal').classList.remove('show');
}

async function submitApartamentosMasivo(event) {
    event.preventDefault();
    const data = {
        cantidad: parseInt(document.getElementById('cantidadApartamentoMasivo').value),
        numeroInicial: document.getElementById('numeroInicialApartamentoMasivo').value,
        tipoOcupacion: document.getElementById('tipoOcupacionMasivo').value,
        estadoCuenta: document.getElementById('estadoCuentaMasivo').value,
        medidas: document.getElementById('medidasApartamentoMasivo').value || null,
        telefono: document.getElementById('telefonoApartamentoMasivo').value || null
    };
    try {
        const response = await fetch('/api/apartamentos/crear-varios', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) });
        const resultado = await response.json();
        if (!response.ok) throw new Error(resultado.error || 'Error al generar los apartamentos');
        closeApartamentoMasivoModal();
        resumenAltaMasiva(resultado);
        await cargarApartamentos();
    } catch (error) { alert('Error: ' + error.message); }
    return false;
}

function openParqueaderoMasivoModal() {
    document.getElementById('parqueaderoMasivoForm').reset();
    poblarSelects();
    document.getElementById('parqueaderoMasivoModal').classList.add('show');
}

function closeParqueaderoMasivoModal() {
    document.getElementById('parqueaderoMasivoModal').classList.remove('show');
}

async function submitParqueaderosMasivo(event) {
    event.preventDefault();
    const data = {
        cantidad: parseInt(document.getElementById('cantidadParqueaderoMasivo').value),
        numeroInicial: document.getElementById('numeroInicialParqueaderoMasivo').value,
        estado: document.getElementById('estadoParqueaderoMasivo').value,
        medidas: document.getElementById('medidasParqueaderoMasivo').value || null,
        telefono: document.getElementById('telefonoParqueaderoMasivo').value || null
    };
    try {
        const response = await fetch('/api/parqueaderos/crear-varios', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) });
        const resultado = await response.json();
        if (!response.ok) throw new Error(resultado.error || 'Error al generar los parqueaderos');
        closeParqueaderoMasivoModal();
        resumenAltaMasiva(resultado);
        await cargarParqueaderos();
    } catch (error) { alert('Error: ' + error.message); }
    return false;
}

function openSalonMasivoModal() {
    document.getElementById('salonMasivoForm').reset();
    poblarSelects();
    document.getElementById('salonMasivoModal').classList.add('show');
}

function closeSalonMasivoModal() {
    document.getElementById('salonMasivoModal').classList.remove('show');
}

async function submitSalonesMasivo(event) {
    event.preventDefault();
    const data = {
        cantidad: parseInt(document.getElementById('cantidadSalonMasivo').value),
        numeroInicial: document.getElementById('numeroInicialSalonMasivo').value,
        nombreEstado: document.getElementById('estadoSalonMasivo').value,
        medidas: document.getElementById('medidasSalonMasivo').value || null,
        telefono: document.getElementById('telefonoSalonMasivo').value || null
    };
    try {
        const response = await fetch('/api/salones/crear-varios', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) });
        const resultado = await response.json();
        if (!response.ok) throw new Error(resultado.error || 'Error al generar los salones sociales');
        closeSalonMasivoModal();
        resumenAltaMasiva(resultado);
        await cargarSalones();
    } catch (error) { alert('Error: ' + error.message); }
    return false;
}

// ========== PARQUEADEROS ==========
async function cargarParqueaderos() {
    try {
        const response = await fetch('/api/parqueaderos/todos');
        parqueaderosData = await response.json();
        mostrarParqueaderos(parqueaderosData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarParqueaderos(parqueaderos) {
    const tbody = document.getElementById('parqueaderosTableBody');
    tbody.innerHTML = parqueaderos.length === 0 ? '<tr><td colspan="5" class="p-8 text-center">No se encontraron parqueaderos</td></tr>' : '';
    parqueaderos.forEach(p => {
        const estado = p.estado || 'N/A';
        const statusClass = estado === 'Disponible' ? 'from-emerald-500 to-teal-400' : 'from-red-600 to-orange-600';
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-sm font-semibold">${p.numero}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b"><span class="bg-gradient-to-tl ${statusClass} px-2.5 text-xs rounded-1.8 py-1.4 inline-block font-bold uppercase text-white">${estado}</span></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${p.medidas} m²</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${p.telefono}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="editParqueadero('${p.numero}')" class="text-xs font-semibold text-blue-500 hover:text-blue-700 mr-3"><i class="fas fa-edit mr-1"></i>Editar</button>
                <button onclick="openDeleteModal('${p.numero}', 'parqueadero', '${p.numero}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-trash mr-1"></i>Eliminar</button>
            </td></tr>`;
    });
}

function buscarParqueaderos() {
    const texto = document.getElementById('searchInputParqueaderos').value.toLowerCase();
    mostrarParqueaderos(parqueaderosData.filter(p => p.numero.toLowerCase().includes(texto) || (p.estado?.nombre||'').toLowerCase().includes(texto) || p.telefono.toString().includes(texto)));
}

function openParqueaderoModal() {
    currentEditNumber = null;
    document.getElementById('parqueaderoModalTitle').textContent = 'Agregar Parqueadero';
    document.getElementById('parqueaderoForm').reset();
    const err = document.getElementById('telefonoParqueaderoError'); if(err) err.textContent = '';
    poblarSelects();
    document.getElementById('parqueaderoModal').classList.add('show');
}

async function editParqueadero(numero) {
    try {
        const response = await fetch(`/api/parqueaderos/${numero}`);
        const p = await response.json();
        currentEditNumber = numero;
        document.getElementById('parqueaderoModalTitle').textContent = 'Editar Parqueadero';
        document.getElementById('numeroParqueadero').value = p.numero;
        document.getElementById('estadoParqueadero').value = p.estado?.nombre || '';
        document.getElementById('medidasParqueadero').value = p.medidas;
        document.getElementById('telefonoParqueadero').value = p.telefono;
        document.getElementById('parqueaderoModal').classList.add('show');
    } catch (error) { alert('Error al cargar el parqueadero'); }
}

function closeParqueaderoModal() { document.getElementById('parqueaderoModal').classList.remove('show'); }

async function submitParqueadero(event) {
    event.preventDefault();
    const telefono = document.getElementById('telefonoParqueadero').value;
    const medidas = document.getElementById('medidasParqueadero').value;
    let hayErrores = false;
    
    // Validar medidas
    if (!/^\d+\.?\d*$/.test(medidas)) {
        document.getElementById('medidasParqueaderoError').textContent = 'Solo se permiten números';
        hayErrores = true;
    } else {
        document.getElementById('medidasParqueaderoError').textContent = '';
    }
    
    // Validar teléfono
    if (!/^\d{10}$/.test(telefono)) {
        document.getElementById('telefonoParqueaderoError').textContent = 'El teléfono debe tener exactamente 10 dígitos';
        hayErrores = true;
    } else {
        document.getElementById('telefonoParqueaderoError').textContent = '';
    }
    
    if (hayErrores) return false;

    const data = {
        numero: document.getElementById('numeroParqueadero').value,
        estado: document.getElementById('estadoParqueadero').value,
        medidas: medidas,
        telefono: parseInt(telefono)
    };

    try {
        const url = currentEditNumber ? `/api/parqueaderos/actualizar/${currentEditNumber}` : '/api/parqueaderos/crear';
        const response = await fetch(url, {
            method: currentEditNumber ? 'PUT' : 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(await response.text());
        alert(await response.text());
        closeParqueaderoModal();
        await cargarParqueaderos();
    } catch (error) {
        alert('Error: ' + error.message);
    }
    return false;
}

// ========== SALONES ==========
async function cargarSalones() {
    try {
        const response = await fetch('/api/salones/todos');
        salonesData = await response.json();
        mostrarSalones(salonesData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarSalones(salones) {
    const tbody = document.getElementById('salonesTableBody');
    tbody.innerHTML = salones.length === 0 ? '<tr><td colspan="5" class="p-8 text-center">No se encontraron salones</td></tr>' : '';
    salones.forEach(s => {
        const estado = s.nombreEstado || 'N/A';
        const statusClass = estado === 'Disponible' ? 'from-emerald-500 to-teal-400' : 'from-red-600 to-orange-600';
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-sm font-semibold">${s.numero}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b"><span class="bg-gradient-to-tl ${statusClass} px-2.5 text-xs rounded-1.8 py-1.4 inline-block font-bold uppercase text-white">${estado}</span></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.medidas} m²</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.telefono}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="editSalon('${s.numero}')" class="text-xs font-semibold text-blue-500 hover:text-blue-700 mr-3"><i class="fas fa-edit mr-1"></i>Editar</button>
                <button onclick="openDeleteModal('${s.numero}', 'salon', '${s.numero}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-trash mr-1"></i>Eliminar</button>
            </td></tr>`;
    });
}

function buscarSalones() {
    const texto = document.getElementById('searchInputSalones').value.toLowerCase();
    mostrarSalones(salonesData.filter(s => s.numero.toLowerCase().includes(texto) || (s.estado?.nombre||'').toLowerCase().includes(texto) || s.telefono.toString().includes(texto)));
}

function openSalonModal() {
    currentEditNumber = null;
    document.getElementById('salonModalTitle').textContent = 'Agregar Salón Social';
    document.getElementById('salonForm').reset();
    const err = document.getElementById('telefonoSalonError'); if(err) err.textContent = '';
    poblarSelects();
    document.getElementById('salonModal').classList.add('show');
}

async function editSalon(numero) {
    try {
        const response = await fetch(`/api/salones/${numero}`);
        const s = await response.json();
        currentEditNumber = numero;
        document.getElementById('salonModalTitle').textContent = 'Editar Salón';
        document.getElementById('numeroSalon').value = s.numero;
        document.getElementById('estadoSalon').value = s.estado?.nombre || '';
        document.getElementById('medidasSalon').value = s.medidas;
        document.getElementById('telefonoSalon').value = s.telefono;
        document.getElementById('salonModal').classList.add('show');
    } catch (error) { alert('Error al cargar el salón'); }
}

function closeSalonModal() { document.getElementById('salonModal').classList.remove('show'); }

async function submitSalon(event) {
    event.preventDefault();
    const telefono = document.getElementById('telefonoSalon').value;
    const medidas = document.getElementById('medidasSalon').value;
    let hayErrores = false;
    
    // Validar medidas
    if (!/^\d+\.?\d*$/.test(medidas)) {
        document.getElementById('medidasSalonError').textContent = 'Solo se permiten números';
        hayErrores = true;
    } else {
        document.getElementById('medidasSalonError').textContent = '';
    }
    
    // Validar teléfono
    if (!/^\d{10}$/.test(telefono)) {
        document.getElementById('telefonoSalonError').textContent = 'El teléfono debe tener exactamente 10 dígitos';
        hayErrores = true;
    } else {
        document.getElementById('telefonoSalonError').textContent = '';
    }
    
    if (hayErrores) return false;

    const data = {
        numero: document.getElementById('numeroSalon').value,
        nombreEstado: document.getElementById('estadoSalon').value,
        medidas: medidas,
        telefono: parseInt(telefono)
    };

    try {
        const url = currentEditNumber ? `/api/salones/actualizar/${currentEditNumber}` : '/api/salones/crear';
        const response = await fetch(url, {
            method: currentEditNumber ? 'PUT' : 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(await response.text());
        alert(await response.text());
        closeSalonModal();
        await cargarSalones();
    } catch (error) {
        alert('Error: ' + error.message);
    }
    return false;
}

// ========== USUARIOS ==========
async function cargarUsuarios() {
    try {
        const response = await fetch('/api/usuarios/todos');
        usuariosData = await response.json();
        mostrarUsuarios(usuariosData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarUsuarios(usuarios) {
    const tbody = document.getElementById('usuariosTableBody');
    tbody.innerHTML = usuarios.length === 0 ? '<tr><td colspan="7" class="p-8 text-center">No se encontraron usuarios</td></tr>' : '';
    usuarios.forEach(u => {
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs font-semibold">${u.nombreTipoDocumento || 'N/A'}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs gsz-mono">${u.numeroDocumento}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${u.nombre}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${u.apellido}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${u.correo}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${u.nombreRol || 'N/A'}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="editUsuario('${u.numeroDocumento}')" class="text-xs font-semibold text-blue-500 hover:text-blue-700 mr-3"><i class="fas fa-edit mr-1"></i>Editar</button>
                <button onclick="openDeleteModal('${u.numeroDocumento}', 'usuario', '${u.nombre} ${u.apellido}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-trash mr-1"></i>Eliminar</button>
            </td></tr>`;
    });
}

function buscarUsuarios() {
    const texto = document.getElementById('searchInputUsuarios').value.toLowerCase();
    mostrarUsuarios(usuariosData.filter(u => u.numeroDocumento?.toString().includes(texto) || u.nombre?.toLowerCase().includes(texto) || u.apellido?.toLowerCase().includes(texto) || u.correo?.toLowerCase().includes(texto) || u.rol?.nombre?.toLowerCase().includes(texto)));
}

function openUsuarioModal() {
    currentEditNumber = null;
    document.getElementById('usuarioModalTitle').textContent = 'Agregar Usuario';
    document.getElementById('usuarioForm').reset();
    document.getElementById('numeroDocumento').readOnly = false;
    document.getElementById('passwordFields').style.display = 'block';
    document.getElementById('confirmPasswordFields').style.display = 'block';
    document.getElementById('contrasenaUsuario').required = true;
    document.getElementById('repetirContrasena').required = true;
    ['numeroDocumentoError','correoUsuarioError','contrasenaError'].forEach(id => { const s = document.getElementById(id); if(s) s.textContent = ''; });
    poblarSelects();
    document.getElementById('usuarioModal').classList.add('show');
}

async function editUsuario(numeroDoc) {
    try {
        const response = await fetch(`/api/usuarios/${numeroDoc}`);
        const u = await response.json();
        currentEditNumber = numeroDoc;
        document.getElementById('usuarioModalTitle').textContent = 'Editar Usuario';
        document.getElementById('tipoDocumentoUsuario').value = u.nombreTipoDocumento || '';
        document.getElementById('numeroDocumento').value = u.numeroDocumento;
        document.getElementById('numeroDocumento').readOnly = true;
        document.getElementById('nombresUsuario').value = u.nombre;
        document.getElementById('apellidosUsuario').value = u.apellido;
        document.getElementById('correoUsuario').value = u.correo;
        document.getElementById('rolUsuario').value = u.nombreRol || '';
        document.getElementById('passwordFields').style.display = 'none';
        document.getElementById('confirmPasswordFields').style.display = 'none';
        document.getElementById('contrasenaUsuario').required = false;
        document.getElementById('repetirContrasena').required = false;
        ['numeroDocumentoError','correoUsuarioError','contrasenaError'].forEach(id => { const s = document.getElementById(id); if(s) s.textContent = ''; });
        document.getElementById('usuarioModal').classList.add('show');
    } catch (error) { alert('Error al cargar el usuario'); }
}

function closeUsuarioModal() {
    document.getElementById('usuarioModal').classList.remove('show');
    document.getElementById('numeroDocumento').readOnly = false;
    document.getElementById('passwordFields').style.display = 'block';
    document.getElementById('confirmPasswordFields').style.display = 'block';
}

async function submitUsuario(event) {
    event.preventDefault();

    const numeroDoc = document.getElementById('numeroDocumento').value;
    const correo = document.getElementById('correoUsuario').value;
    let hayErrores = false;

    if (!/^\d{8,}$/.test(numeroDoc)) {
        document.getElementById('numeroDocumentoError').textContent =
            'El documento debe tener al menos 8 dígitos numéricos';
        hayErrores = true;
    } else {
        document.getElementById('numeroDocumentoError').textContent = '';
    }

    const regexCorreo = /^[a-zA-Z0-9._%+-]+@(gmail\.com|hotmail\.com|outlook\.com|yahoo\.com|live\.com|icloud\.com|protonmail\.com|mail\.com)$/i;

    if (!regexCorreo.test(correo)) {
        document.getElementById('correoUsuarioError').textContent =
            'Ingrese un correo válido';
        hayErrores = true;
    } else {
        document.getElementById('correoUsuarioError').textContent = '';
    }

    if (document.getElementById('passwordFields').style.display !== 'none') {
        const password = document.getElementById('contrasenaUsuario').value;
        const confirmPassword = document.getElementById('repetirContrasena').value;

        if (password.length < 6) {
            document.getElementById('contrasenaError').textContent =
                'La contraseña debe tener al menos 6 caracteres';
            hayErrores = true;
        } else if (password !== confirmPassword) {
            document.getElementById('contrasenaError').textContent =
                'Las contraseñas no coinciden';
            hayErrores = true;
        } else {
            document.getElementById('contrasenaError').textContent = '';
        }
    }

    if (hayErrores) return false;

    const data = {
    nombreTipoDocumento: document.getElementById('tipoDocumentoUsuario').value,
    numeroDocumento: numeroDoc,
    nombre: document.getElementById('nombresUsuario').value,
    apellido: document.getElementById('apellidosUsuario').value,
    correo: correo,
    nombreRol: document.getElementById('rolUsuario').value
};

if (!currentEditNumber) {
    data.contrasena = document.getElementById('contrasenaUsuario').value;
}

    if (!currentEditNumber) {
        data.contrasena = document.getElementById('contrasenaUsuario').value;
    }

    try {
        const url = currentEditNumber
            ? `/api/usuarios/actualizar/${currentEditNumber}`
            : '/api/usuarios/crear';

        const response = await fetch(url, {
            method: currentEditNumber ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error(await response.text());

        alert(await response.text());
        closeUsuarioModal();
        await cargarUsuarios();

    } catch (error) {
        alert('Error: ' + error.message);
    }

    return false;
}

// ========== RESIDENTES ==========
async function cargarResidentes() {
    try {
        const response = await fetch('/api/residentes/todos');
        residentesData = await response.json();
        mostrarResidentes(residentesData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarResidentes(residentes) {
    const tbody = document.getElementById('residentesTableBody');
    tbody.innerHTML = residentes.length === 0 ? '<tr><td colspan="7" class="p-8 text-center">No se encontraron residentes</td></tr>' : '';
    residentes.forEach(r => {
        const tipoRes = r.nombreTipoResidente || 'N/A';
        let tipoClass = 'from-green-600 to-emerald-500';
        if (tipoRes === 'Arrendatario') tipoClass = 'from-orange-500 to-yellow-500';
        else if (tipoRes === 'Visitante') tipoClass = 'from-blue-500 to-cyan-500';
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs font-semibold">${r.nombreTipoDocumento || 'N/A'}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs gsz-mono">${r.numeroDocumento}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${r.nombre}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${r.apellido}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${r.celular}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b"><span class="bg-gradient-to-tl ${tipoClass} px-2.5 text-xs rounded-1.8 py-1.4 inline-block font-bold uppercase text-white">${tipoRes}</span></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="editResidente('${r.numeroDocumento}')" class="text-xs font-semibold text-blue-500 hover:text-blue-700 mr-3"><i class="fas fa-edit mr-1"></i>Editar</button>
                <button onclick="openDeleteModal('${r.numeroDocumento}', 'residente', '${r.nombre} ${r.apellido}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-trash mr-1"></i>Eliminar</button>
            </td></tr>`;
    });
}

function buscarResidentes() {
    const texto = document.getElementById('searchInputResidentes').value.toLowerCase();
    mostrarResidentes(residentesData.filter(r => r.numeroDocumento.toString().includes(texto) || r.nombre.toLowerCase().includes(texto) || r.apellido.toLowerCase().includes(texto) || r.celular.toString().includes(texto) || (r.tipoDocumento?.nombre||'').toLowerCase().includes(texto) || (r.tipoResidente?.nombre||'').toLowerCase().includes(texto)));
}

function openResidenteModal() {
    currentEditNumber = null;
    document.getElementById('residenteModalTitle').textContent = 'Agregar Residente';
    document.getElementById('residenteForm').reset();
    document.getElementById('documentoResidente').readOnly = false;
    ['documentoResidenteError','celularResidenteError'].forEach(id => { const s = document.getElementById(id); if(s) s.textContent = ''; });
    poblarSelects();
    document.getElementById('residenteModal').classList.add('show');
}

async function editResidente(numeroDoc) {
    try {
        const response = await fetch(`/api/residentes/${numeroDoc}`);
        const r = await response.json();
        currentEditNumber = numeroDoc;
        document.getElementById('residenteModalTitle').textContent = 'Editar Residente';
        document.getElementById('tipoDocumentoResidente').value = r.nombreTipoDocumento || '';
        document.getElementById('documentoResidente').value = r.numeroDocumento;
        document.getElementById('documentoResidente').readOnly = true;
        document.getElementById('nombreResidente').value = r.nombre;
        document.getElementById('apellidoResidente').value = r.apellido;
        document.getElementById('celularResidente').value = r.celular;
        document.getElementById('tipoResidente').value = r.nombreTipoResidente || '';
        ['documentoResidenteError','celularResidenteError'].forEach(id => { const s = document.getElementById(id); if(s) s.textContent = ''; });
        document.getElementById('residenteModal').classList.add('show');
    } catch (error) { alert('Error al cargar el residente'); }
}

function closeResidenteModal() {
    document.getElementById('residenteModal').classList.remove('show');
    document.getElementById('documentoResidente').readOnly = false;
    currentEditNumber = null;
}

async function submitResidente(event) {
    event.preventDefault();
    const documento = document.getElementById('documentoResidente').value;
    const celular = document.getElementById('celularResidente').value;
    let hayErrores = false;

    // Validar documento (mínimo 6 dígitos)
    if (!/^\d{6,}$/.test(documento)) {
        document.getElementById('documentoResidenteError').textContent = 'El documento debe tener al menos 6 dígitos numéricos';
        hayErrores = true;
    } else { document.getElementById('documentoResidenteError').textContent = ''; }

    // Validar celular (exactamente 10 dígitos)
    if (!/^\d{10}$/.test(celular)) {
        document.getElementById('celularResidenteError').textContent = 'El celular debe tener exactamente 10 dígitos';
        hayErrores = true;
    } else { document.getElementById('celularResidenteError').textContent = ''; }

    if (hayErrores) return false;

    const data = {
        nombreTipoDocumento: document.getElementById('tipoDocumentoResidente').value,
        numeroDocumento: parseInt(documento),
        nombre: document.getElementById('nombreResidente').value,
        apellido: document.getElementById('apellidoResidente').value,
        celular: parseInt(celular),
        nombreTipoResidente: document.getElementById('tipoResidente').value
    };
    try {
        const url = currentEditNumber ? `/api/residentes/actualizar/${currentEditNumber}` : '/api/residentes/crear';
        const response = await fetch(url, { method: currentEditNumber ? 'PUT' : 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) });
        if (!response.ok) throw new Error(await response.text());
        alert(await response.text());
        closeResidenteModal();
        await cargarResidentes();
    } catch (error) { alert('Error: ' + error.message); }
    return false;
}

// ========== SOLICITUDES DE RESERVA EXCEPCIONAL ==========
let solicitudesData = [];

async function cargarSolicitudes() {
    try {
        const response = await fetch('/api/solicitudes-reserva/pendientes');
        solicitudesData = await response.json();
        mostrarSolicitudes(solicitudesData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarSolicitudes(solicitudes) {
    const tbody = document.getElementById('solicitudesTableBody');
    tbody.innerHTML = '';
    if (solicitudes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="p-8 text-center">No hay solicitudes pendientes</td></tr>';
        return;
    }
    solicitudes.forEach(s => {
        const fecha = new Date(s.fechaSolicitada).toLocaleString('es-CO', {
            year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
        });
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.idUsuario}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.idSalon}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${fecha}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.justificacion || ''}</p></td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="aprobarSolicitud('${s.id}')" class="text-xs font-semibold text-emerald-600 hover:text-emerald-800 mr-3"><i class="fas fa-check mr-1"></i>Aprobar</button>
                <button onclick="rechazarSolicitud('${s.id}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-times mr-1"></i>Rechazar</button>
            </td></tr>`;
    });
}

async function aprobarSolicitud(id) {
    if (!confirm('¿Aprobar esta solicitud y crear la reserva?')) return;
    try {
        const response = await fetch(`/api/solicitudes-reserva/${id}/aprobar`, { method: 'POST' });
        const mensaje = await response.text();
        alert(mensaje);
        if (response.ok) await cargarSolicitudes();
    } catch (error) { alert('Error al aprobar la solicitud: ' + error.message); }
}

async function rechazarSolicitud(id) {
    const motivo = prompt('Motivo del rechazo (opcional):');
    if (motivo === null) return;
    try {
        const response = await fetch(`/api/solicitudes-reserva/${id}/rechazar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ motivo })
        });
        const mensaje = await response.text();
        alert(mensaje);
        if (response.ok) await cargarSolicitudes();
    } catch (error) { alert('Error al rechazar la solicitud: ' + error.message); }
}

// ========== SOLICITUDES DE REGISTRO ==========
let solicitudesRegistroData = [];

async function cargarSolicitudesRegistro() {
    try {
        const response = await fetch('/api/solicitudes-registro/pendientes');
        solicitudesRegistroData = await response.json();
        mostrarSolicitudesRegistro(solicitudesRegistroData);
    } catch (error) { console.error('Error:', error); }
}

function mostrarSolicitudesRegistro(solicitudes) {
    const tbody = document.getElementById('registrosTableBody');
    tbody.innerHTML = '';
    if (solicitudes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="p-8 text-center">No hay solicitudes de registro pendientes</td></tr>';
        return;
    }
    const opcionesRol = catalogos.roles.map(r => `<option value="${r.nombre}">${r.nombre}</option>`).join('');
    solicitudes.forEach(s => {
        const fecha = new Date(s.fechaCreacion).toLocaleString('es-CO', {
            year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
        });
        tbody.innerHTML += `<tr>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs gsz-mono">${s.numeroDocumento}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.nombre} ${s.apellido}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.correo}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${s.nombreTipoDocumento}</p></td>
            <td class="p-2 align-middle bg-transparent border-b"><p class="px-6 mb-0 text-xs">${fecha}</p></td>
            <td class="p-2 align-middle bg-transparent border-b">
                <select id="rolRegistro-${s.id}" class="px-2 py-1 text-xs border border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none">
                    <option value="">Seleccionar...</option>
                    ${opcionesRol}
                </select>
            </td>
            <td class="p-2 text-center align-middle bg-transparent border-b">
                <button onclick="aprobarSolicitudRegistro('${s.id}')" class="text-xs font-semibold text-emerald-600 hover:text-emerald-800 mr-3"><i class="fas fa-check mr-1"></i>Aprobar</button>
                <button onclick="rechazarSolicitudRegistro('${s.id}')" class="text-xs font-semibold text-red-500 hover:text-red-700"><i class="fas fa-times mr-1"></i>Rechazar</button>
            </td></tr>`;
    });
}

async function aprobarSolicitudRegistro(id) {
    const nombreRol = document.getElementById(`rolRegistro-${id}`).value;
    if (!nombreRol) { alert('Selecciona un rol antes de aprobar.'); return; }
    if (!confirm('¿Aprobar esta solicitud y crear el usuario con el rol seleccionado?')) return;
    try {
        const response = await fetch(`/api/solicitudes-registro/${id}/aprobar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombreRol })
        });
        const mensaje = await response.text();
        alert(mensaje);
        if (response.ok) await cargarSolicitudesRegistro();
    } catch (error) { alert('Error al aprobar la solicitud: ' + error.message); }
}

async function rechazarSolicitudRegistro(id) {
    const motivo = prompt('Motivo del rechazo (opcional):');
    if (motivo === null) return;
    try {
        const response = await fetch(`/api/solicitudes-registro/${id}/rechazar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ motivo })
        });
        const mensaje = await response.text();
        alert(mensaje);
        if (response.ok) await cargarSolicitudesRegistro();
    } catch (error) { alert('Error al rechazar la solicitud: ' + error.message); }
}

// ========== DELETE MODAL ==========
function openDeleteModal(identificador, type, name) {
    currentDeleteNumber = identificador;
    currentDeleteType = type;
    const tipos = { apartamento: 'el apartamento', parqueadero: 'el parqueadero', salon: 'el salón social', usuario: 'el usuario', residente: 'el residente' };
    document.getElementById('deleteMessage').innerHTML = `¿Está seguro que desea eliminar ${tipos[type]} <span class="font-bold">${name}</span>? Esta acción no se puede deshacer.`;
    document.getElementById('deleteModal').classList.add('show');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.remove('show');
    currentDeleteNumber = null;
    currentDeleteType = null;
}

async function confirmDelete() {
    if (!currentDeleteNumber || !currentDeleteType) return;
    
    const urls = { 
        apartamento: `/api/apartamentos/eliminar/${currentDeleteNumber}`, 
        parqueadero: `/api/parqueaderos/eliminar/${currentDeleteNumber}`, 
        salon: `/api/salones/eliminar/${currentDeleteNumber}`, 
        usuario: `/api/usuarios/eliminar/${currentDeleteNumber}`, 
        residente: `/api/residentes/eliminar/${currentDeleteNumber}` 
    };
    
    const cargas = { 
        apartamento: cargarApartamentos, 
        parqueadero: cargarParqueaderos, 
        salon: cargarSalones, 
        usuario: cargarUsuarios, 
        residente: cargarResidentes 
    };
    
    try {
        const response = await fetch(urls[currentDeleteType], { method: 'DELETE' });
        const mensaje = await response.text();
        
        if (!response.ok) {
            // Mejorar el mensaje de error para restricciones de clave foránea
            if (mensaje.includes('foreign key constraint') || 
                mensaje.includes('FOREIGN KEY') || 
                mensaje.includes('registros relacionados')) {
                
                const tipoMensajes = {
                    residente: 'No se puede eliminar el residente porque tiene registros relacionados (parqueaderos, visitantes, etc.). Elimine primero esos registros.',
                    usuario: 'No se puede eliminar el usuario porque tiene registros relacionados. Elimine primero esos registros.',
                    apartamento: 'No se puede eliminar el apartamento porque tiene residentes o registros asociados.',
                    parqueadero: 'No se puede eliminar el parqueadero porque tiene registros asociados.',
                    salon: 'No se puede eliminar el salón porque tiene reservas asociadas.'
                };
                
                alert(tipoMensajes[currentDeleteType] || 'No se puede eliminar porque tiene registros relacionados.');
            } else {
                alert('Error: ' + mensaje);
            }
        } else {
            alert(mensaje);
            await cargas[currentDeleteType]();
        }
        
        closeDeleteModal();
    } catch (error) { 
        alert('Error al eliminar: ' + error.message); 
        closeDeleteModal(); 
    }
}

// ========== EVENTOS DE VALIDACIÓN EN TIEMPO REAL ==========
document.addEventListener('DOMContentLoaded', function() {
    // Apartamentos
    const numApt = document.getElementById('numeroApartamento');
    if (numApt) numApt.addEventListener('input', function() { validarSoloNumeros(this, 'numeroApartamentoError'); });
    
    const medApt = document.getElementById('medidasApartamento');
    if (medApt) medApt.addEventListener('input', function() { validarMedidas(this, 'medidasApartamentoError'); });
    
    const telApt = document.getElementById('telefonoApartamento');
    if (telApt) telApt.addEventListener('input', function() { validarTelefono(this, 'telefonoApartamentoError'); });

    // Parqueaderos
    const telPark = document.getElementById('telefonoParqueadero');
    if (telPark) telPark.addEventListener('input', function() { validarTelefono(this, 'telefonoParqueaderoError'); });

    // Salones
    const telSalon = document.getElementById('telefonoSalon');
    if (telSalon) telSalon.addEventListener('input', function() { validarTelefono(this, 'telefonoSalonError'); });

    // Usuarios
    const numDocUsr = document.getElementById('numeroDocumento');
    if (numDocUsr) numDocUsr.addEventListener('input', function() { validarDocumento(this, 'numeroDocumentoError'); });
    
    const correoUsr = document.getElementById('correoUsuario');
    if (correoUsr) correoUsr.addEventListener('input', function() { validarCorreo(this, 'correoUsuarioError'); });
    
    const repPass = document.getElementById('repetirContrasena');
    if (repPass) repPass.addEventListener('input', validarContrasenas);
    
    const pass = document.getElementById('contrasenaUsuario');
    if (pass) pass.addEventListener('input', validarContrasenas);

    // Residentes
    const docRes = document.getElementById('documentoResidente');
    if (docRes) docRes.addEventListener('input', function() { validarDocumento(this, 'documentoResidenteError'); });
    
    const celRes = document.getElementById('celularResidente');
    if (celRes) celRes.addEventListener('input', function() { validarTelefono(this, 'celularResidenteError'); });

    // Parqueaderos - medidas
    const medPark = document.getElementById('medidasParqueadero');
    if (medPark) medPark.addEventListener('input', function() { validarMedidas(this, 'medidasParqueaderoError'); });

    // Salones - medidas
    const medSalon = document.getElementById('medidasSalon');
    if (medSalon) medSalon.addEventListener('input', function() { validarMedidas(this, 'medidasSalonError'); });
});

// ========== INICIALIZACIÓN (SIN CERRAR AL CLICK FUERA) ==========

window.addEventListener('DOMContentLoaded', async function() {
    try {
        await cargarCatalogos();
        await Promise.all([cargarApartamentos(), cargarParqueaderos(), cargarSalones(), cargarUsuarios(), cargarResidentes(), cargarSolicitudes(), cargarSolicitudesRegistro()]);
    } catch (error) { alert('Error al inicializar la aplicación.'); }
});

function confirmarCerrarSesion(event) {
    event.preventDefault();
    if (confirm('¿Está seguro que desea cerrar sesión?')) {
        sessionStorage.clear();
        window.location.href = '/logout';
    }
    return false;
}