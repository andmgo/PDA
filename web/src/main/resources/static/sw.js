// Service worker de Aurum Condominios (PWA).
//
// Qué guarda: solo archivos estáticos (estilos, scripts, imágenes, fuentes)
// y la página "sin conexión". Nunca guarda páginas ni respuestas de /api:
// tienen datos personales y cambian todo el tiempo.
//
//  · Páginas ............ red primero; sin red → /offline.html
//  · CSS y JS ........... red primero (siempre la versión nueva); sin red → caché
//  · Imágenes y fuentes . caché primero (casi nunca cambian)
//
// Al cambiar archivos precargados, subir VERSION para renovar la caché.
const VERSION = 'aurum-v1';
const PRECARGA = [
    '/offline.html',
    '/img/pwa/icono-192.png',
    '/img/aurum-isotipo.png',
    '/css/gescazone-theme.css',
    '/css/aurum-ui.css'
];

self.addEventListener('install', evento => {
    evento.waitUntil(caches.open(VERSION).then(c => c.addAll(PRECARGA)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', evento => {
    evento.waitUntil(
        caches.keys()
            .then(claves => Promise.all(claves.filter(k => k !== VERSION).map(k => caches.delete(k))))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', evento => {
    const peticion = evento.request;
    if (peticion.method !== 'GET') return;
    const url = new URL(peticion.url);
    if (url.origin !== self.location.origin) return;

    if (peticion.mode === 'navigate') {
        evento.respondWith(fetch(peticion).catch(() => caches.match('/offline.html')));
        return;
    }
    if (/^\/(css|js)\//.test(url.pathname)) {
        evento.respondWith(redPrimero(peticion));
        return;
    }
    if (/^\/(img|fonts)\//.test(url.pathname)) {
        evento.respondWith(cachePrimero(peticion));
    }
    // Todo lo demás (/api, formularios, etc.) va directo a la red.
});

async function redPrimero(peticion) {
    const cache = await caches.open(VERSION);
    try {
        const respuesta = await fetch(peticion);
        if (respuesta.ok) cache.put(peticion, respuesta.clone());
        return respuesta;
    } catch (e) {
        return (await cache.match(peticion)) || Response.error();
    }
}

async function cachePrimero(peticion) {
    const cache = await caches.open(VERSION);
    const guardada = await cache.match(peticion);
    if (guardada) return guardada;
    const respuesta = await fetch(peticion);
    if (respuesta.ok) cache.put(peticion, respuesta.clone());
    return respuesta;
}
