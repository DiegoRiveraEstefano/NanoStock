/**
 * <nano-shopping-item> Web Component
 * Elemento táctil e interactivo para gestionar compras en el supermercado.
 * 
 * Permite marcar estados rápidamente: PENDING -> IN_CART -> BOUGHT / OUT_OF_STOCK
 */
class NanoShoppingItem extends HTMLElement {
    connectedCallback() {
        if (!this._initialized) {
            this._bindEvents();
            this._initialized = true;
        }
    }

    _bindEvents() {
        const itemId = this.getAttribute('item-id');
        const cartBtn = this.querySelector('[data-action="toggle-cart"]');
        const outBtn = this.querySelector('[data-action="mark-out"]');

        if (cartBtn) {
            cartBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                const isCart = this.getAttribute('status') === 'IN_CART';
                const nextStatus = isCart ? 'PENDING' : 'IN_CART';
                await this._updateStatus(itemId, nextStatus);
            });
        }

        if (outBtn) {
            outBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                await this._updateStatus(itemId, 'OUT_OF_STOCK');
            });
        }
    }

    async _updateStatus(itemId, status) {
        try {
            const response = await fetch(`/purchases/shopping-lists/items/${itemId}/status`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ status: status })
            });

            if (response.ok) {
                this.setAttribute('status', status);
                this.classList.toggle('has-background-light', status === 'IN_CART');
                this.classList.toggle('is-in-cart', status === 'IN_CART');
                
                const titleEl = this.querySelector('.item-title');
                if (titleEl) {
                    titleEl.style.textDecoration = (status === 'IN_CART' || status === 'BOUGHT') ? 'line-through' : 'none';
                    titleEl.classList.toggle('has-text-grey', status === 'IN_CART');
                }

                if (window.nanoToast) {
                    const msg = status === 'IN_CART' ? 'En el carrito 🛒' : (status === 'OUT_OF_STOCK' ? 'Agotado en tienda ⚠️' : 'Pendiente');
                    window.nanoToast(msg, status === 'IN_CART' ? 'is-success' : 'is-info', 2000);
                }
            }
        } catch (err) {
            console.error('Error actualizando estado del ítem:', err);
        }
    }
}

customElements.define('nano-shopping-item', NanoShoppingItem);
