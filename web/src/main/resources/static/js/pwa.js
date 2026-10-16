// PWA: registra el service worker (/sw.js) y maneja el botón "Instalar
// aplicación" (cualquier elemento con data-pwa-instalar). El botón solo
// aparece cuando el navegador ofrece instalar: Chrome/Edge en Android y
// computador, con https (o localhost). En iPhone se instala desde Safari:
// Compartir → "Agregar a inicio".
(function () {
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js').catch(() => { /* sin PWA, la web funciona igual */ });
        });
    }

    let ofrecimiento = null;
    const botones = () => document.querySelectorAll('[data-pwa-instalar]');
    const mostrar = visible => botones().forEach(b => { b.hidden = !visible; });

    window.addEventListener('beforeinstallprompt', evento => {
        evento.preventDefault();
        ofrecimiento = evento;
        mostrar(true);
    });

    window.addEventListener('appinstalled', () => {
        ofrecimiento = null;
        mostrar(false);
    });

    document.addEventListener('click', async evento => {
        const boton = evento.target.closest('[data-pwa-instalar]');
        if (!boton || !ofrecimiento) return;
        evento.preventDefault();
        ofrecimiento.prompt();
        await ofrecimiento.userChoice;
        ofrecimiento = null;
        mostrar(false);
    });
})();
