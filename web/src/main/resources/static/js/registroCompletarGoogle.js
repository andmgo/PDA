// ══════════════════════════════════════════════════════════════════════════════
//  registroCompletarGoogle.js — último paso de "Registrarte con Google"
//  (/registro/completar-google), solo pide número de documento.
// ══════════════════════════════════════════════════════════════════════════════

document.getElementById('numeroDocumento').addEventListener('input', function () {
    this.value = this.value.replace(/\D/g, '');
});
