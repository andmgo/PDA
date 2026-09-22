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