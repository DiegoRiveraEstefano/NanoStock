/**
 * NanoStock - JavaScript Vanilla Core
 * Manejo accesible de elementos Bulma sin librerías pesadas.
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Toggle de Navbar Burger (Móvil / Tablet)
    const navbarBurgers = Array.from(document.querySelectorAll('.navbar-burger'));
    navbarBurgers.forEach(burger => {
        burger.addEventListener('click', () => {
            const targetId = burger.dataset.target;
            const target = document.getElementById(targetId);
            burger.classList.toggle('is-active');
            if (target) {
                target.classList.toggle('is-active');
            }
        });
    });

    // 2. Cierre de Alertas / Notificaciones (.delete)
    document.addEventListener('click', (event) => {
        if (event.target.matches('.notification .delete, .message .delete')) {
            const container = event.target.closest('.notification, .message');
            if (container) {
                container.style.opacity = '0';
                container.style.transition = 'opacity 0.2s ease-out';
                setTimeout(() => container.remove(), 200);
            }
        }
    });

    // 3. Dropdowns de Bulma
    const dropdowns = Array.from(document.querySelectorAll('.dropdown:not(.is-hoverable)'));
    dropdowns.forEach(dropdown => {
        const trigger = dropdown.querySelector('.dropdown-trigger button, .dropdown-trigger a');
        if (trigger) {
            trigger.addEventListener('click', (e) => {
                e.stopPropagation();
                dropdown.classList.toggle('is-active');
            });
        }
    });

    // Cerrar dropdowns al hacer clic fuera
    document.addEventListener('click', () => {
        dropdowns.forEach(dropdown => dropdown.classList.remove('is-active'));
    });

    // 4. Tabs genéricas de Bulma
    const tabContainers = document.querySelectorAll('.tabs:not([data-no-tab-auto])');
    tabContainers.forEach(container => {
        const tabs = container.querySelectorAll('li');
        tabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                const targetSelector = tab.querySelector('a')?.getAttribute('href');
                if (targetSelector && targetSelector.startsWith('#')) {
                    e.preventDefault();
                    tabs.forEach(t => t.classList.remove('is-active'));
                    tab.classList.add('is-active');

                    // Si existen paneles de contenido correspondientes
                    const parentSection = container.closest('section, .card, .tabs-wrapper') || document;
                    const panels = parentSection.querySelectorAll('.tab-panel');
                    panels.forEach(p => p.classList.add('is-hidden'));
                    const activePanel = parentSection.querySelector(targetSelector);
                    if (activePanel) {
                        activePanel.classList.remove('is-hidden');
                    }
                }
            });
        });
    });
});

/**
 * Toast Helper global para disparar notificaciones en tiempo real
 * @param {string} message - Mensaje a mostrar
 * @param {'is-primary'|'is-success'|'is-warning'|'is-danger'|'is-info'} type - Clase Bulma
 * @param {number} durationMs - Duración en milisegundos (por defecto 4000ms)
 */
window.nanoToast = function(message, type = 'is-primary', durationMs = 4000) {
    let container = document.getElementById('nano-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'nano-toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `notification ${type} is-light`;
    toast.innerHTML = `
        <button class="delete" aria-label="close"></button>
        <div>${message}</div>
    `;

    toast.querySelector('.delete').addEventListener('click', () => toast.remove());
    container.appendChild(toast);

    if (durationMs > 0) {
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, durationMs);
    }
};
