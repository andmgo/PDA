// ══════════════════════════════════════════════════════════════════════════════
//  registroCompletarGoogle.js — último paso de "Registrarte con Google"
//  (/registro/completar-google): documento y contraseña (misma validación
//  que registro.js, para poder loguearse con documento+contraseña además
//  de con Google).
// ══════════════════════════════════════════════════════════════════════════════

document.getElementById('numeroDocumento').addEventListener('input', function () {
    this.value = this.value.replace(/\D/g, '');
});

document.getElementById('completarGoogleForm').addEventListener('submit', function (e) {
    const contrasena = document.getElementById('contrasena').value;
    const confirmar  = document.getElementById('confirmarContrasena').value;

    if (contrasena !== confirmar) {
        e.preventDefault();
        mostrarError('Las contraseñas no coinciden.');
        return;
    }
    if (contrasena.length < 6) {
        e.preventDefault();
        mostrarError('La contraseña debe tener al menos 6 caracteres.');
    }
});

function mostrarError(mensaje) {
    let errorDiv = document.querySelector('.error-message.custom');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'error-message custom show';
        document.querySelector('.form-title').after(errorDiv);
    }
    errorDiv.textContent = mensaje;
    errorDiv.classList.add('show');
    setTimeout(() => errorDiv.classList.remove('show'), 5000);
}
