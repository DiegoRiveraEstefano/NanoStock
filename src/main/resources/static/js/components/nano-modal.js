/**
 * <nano-modal> Web Component
 * Modal interactivo accesible y compatible con Bulma CSS.
 * 
 * Uso:
 * <nano-modal id="my-modal" title="Confirmar Acción">
 *     <p>¿Estás seguro de registrar esta merma?</p>
 *     <div slot="footer">
 *         <button class="button is-danger">Confirmar</button>
 *         <button class="button" data-close>Cancelar</button>
 *     </div>
 * </nano-modal>
 */
class NanoModal extends HTMLElement {
    static get observedAttributes() {
        return ['open', 'title'];
    }

    connectedCallback() {
        if (!this._initialized) {
            this._render();
            this._bindEvents();
            this._initialized = true;
        }
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name === 'open') {
            this._updateOpenState(newValue !== null);
        } else if (name === 'title' && this._titleEl) {
            this._titleEl.textContent = newValue || '';
        }
    }

    _render() {
        const title = this.getAttribute('title') || '';
        const bodyContent = Array.from(this.childNodes)
            .filter(node => node.nodeType !== Node.ELEMENT_NODE || !node.hasAttribute('slot'));
        const footerSlot = this.querySelector('[slot="footer"]');

        this.innerHTML = `
            <div class="modal ${this.hasAttribute('open') ? 'is-active' : ''}">
                <div class="modal-background" data-close></div>
                <div class="modal-card">
                    <header class="modal-card-head">
                        <p class="modal-card-title">${title}</p>
                        <button class="delete" aria-label="close" data-close></button>
                    </header>
                    <section class="modal-card-body nano-modal-body">
                    </section>
                    <footer class="modal-card-foot nano-modal-footer is-justify-content-flex-end">
                    </footer>
                </div>
            </div>
        `;

        this._modal = this.querySelector('.modal');
        this._titleEl = this.querySelector('.modal-card-title');
        const bodyContainer = this.querySelector('.nano-modal-body');
        const footerContainer = this.querySelector('.nano-modal-footer');

        // Mover los nodos del contenido original al cuerpo del modal
        bodyContent.forEach(node => bodyContainer.appendChild(node));

        if (footerSlot) {
            footerContainer.appendChild(footerSlot);
        } else {
            footerContainer.remove();
        }
    }

    _bindEvents() {
        this.addEventListener('click', (e) => {
            if (e.target.closest('[data-close]')) {
                this.close();
            }
        });

        // Manejo de tecla Escape
        this._onKeyDown = (e) => {
            if (e.key === 'Escape' && this.hasAttribute('open')) {
                this.close();
            }
        };
        document.addEventListener('keydown', this._onKeyDown);
    }

    disconnectedCallback() {
        if (this._onKeyDown) {
            document.removeEventListener('keydown', this._onKeyDown);
        }
    }

    open() {
        this.setAttribute('open', '');
    }

    close() {
        this.removeAttribute('open');
    }

    toggle() {
        if (this.hasAttribute('open')) {
            this.close();
        } else {
            this.open();
        }
    }

    _updateOpenState(isOpen) {
        if (this._modal) {
            this._modal.classList.toggle('is-active', isOpen);
            if (isOpen) {
                document.documentElement.classList.add('is-clipped');
                this.dispatchEvent(new CustomEvent('nano:open', { bubbles: true }));
            } else {
                document.documentElement.classList.remove('is-clipped');
                this.dispatchEvent(new CustomEvent('nano:close', { bubbles: true }));
            }
        }
    }
}

customElements.define('nano-modal', NanoModal);
