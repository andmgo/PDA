// ══════════════════════════════════════════════════════════════════════════════
//  login.js
//  El formulario se somete de forma nativa a POST /login (Spring Security,
//  ver SecurityConfig en gescazone-web) — no hace falta interceptar el submit.
//
//  "Recordar mi documento": guarda solo el número de documento en el navegador
//  (nunca la contraseña) para precargarlo la próxima visita. No mantiene la
//  sesión iniciada — la persona siempre debe escribir su contraseña.
// ══════════════════════════════════════════════════════════════════════════════

const CLAVE_DOCUMENTO_RECORDADO = 'gescazone_documento';

document.getElementById('loginForm').addEventListener('submit', function (e) {
    const identificador = document.getElementById('username').value.trim();
    const contrasena = document.getElementById('password').value;

    if (!identificador || !contrasena) {
        e.preventDefault();
        mostrarError('Por favor, completa todos los campos.');
        return;
    }

    if (identificador.length < 6) {
        e.preventDefault();
        mostrarError('Ingresa tu número de documento o tu correo completo.');
        return;
    }

    try {
        if (document.getElementById('recordar').checked) {
            localStorage.setItem(CLAVE_DOCUMENTO_RECORDADO, identificador);
        } else {
            localStorage.removeItem(CLAVE_DOCUMENTO_RECORDADO);
        }
    } catch (error) {
        // Almacenamiento no disponible (privado/bloqueado) — no es crítico, se sigue con el login.
    }
});

function mostrarError(mensaje) {
    let errorDiv = document.querySelector('.error-message.custom');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'error-message custom show';
        document.querySelector('.form-title').after(errorDiv);
    }
    errorDiv.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + mensaje;
    errorDiv.classList.add('show');
    setTimeout(() => errorDiv.classList.remove('show'), 5000);
}

function cerrarLogin() {
    window.close();
}

window.addEventListener('DOMContentLoaded', function () {
    try {
        const documentoRecordado = localStorage.getItem(CLAVE_DOCUMENTO_RECORDADO);
        if (documentoRecordado) {
            document.getElementById('username').value = documentoRecordado;
            document.getElementById('recordar').checked = true;
        }
    } catch (error) {
        // Almacenamiento no disponible — simplemente no se precarga nada.
    }

    setTimeout(() => {
        document.querySelectorAll('.error-message.show, .success-message.show')
            .forEach(msg => msg.classList.remove('show'));
    }, 5000);
});
