/**
 * <nano-barcode-scanner> Web Component
 * Escáner óptico con soporte de cámara y BarcodeDetector API nativa.
 * 
 * Uso:
 * <nano-barcode-scanner target-input="#barCode"></nano-barcode-scanner>
 */
class NanoBarcodeScanner extends HTMLElement {
    connectedCallback() {
        if (!this._initialized) {
            this._render();
            this._bindEvents();
            this._initialized = true;
        }
    }

    _render() {
        const btnText = this.getAttribute('button-text') || 'Escanear con Cámara';
        const targetInputSelector = this.getAttribute('target-input') || '#barCode';

        this.innerHTML = `
            <div class="nano-scanner-container is-inline-block">
                <button type="button" class="button is-info is-light nano-scan-trigger">
                    <span>📷</span>
                    <span class="ml-1">${btnText}</span>
                </button>

                <div class="modal nano-scanner-modal">
                    <div class="modal-background nano-scanner-close"></div>
                    <div class="modal-card" style="max-width: 500px;">
                        <header class="modal-card-head">
                            <p class="modal-card-title is-size-5">Escanear Código de Barras</p>
                            <button type="button" class="delete nano-scanner-close" aria-label="close"></button>
                        </header>
                        <section class="modal-card-body has-text-centered p-4">
                            <div class="scanner-viewport-box" style="position: relative; width: 100%; height: 280px; background: #000; border-radius: 8px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                                <video playsinline autoplay muted style="width: 100%; height: 100%; object-fit: cover;"></video>
                                <div class="scanner-aim-line" style="position: absolute; width: 80%; height: 2px; background: rgba(255, 71, 87, 0.85); box-shadow: 0 0 8px #ff4757;"></div>
                            </div>
                            <p class="help is-info mt-2">Apunta la cámara trasera del dispositivo al código de barras del producto (EAN/UPC).</p>
                            <div class="scanner-status is-size-7 has-text-grey mt-1">Iniciando cámara...</div>
                        </section>
                        <footer class="modal-card-foot is-justify-content-space-between">
                            <button type="button" class="button is-small is-light nano-manual-input-btn">Ingreso Manual</button>
                            <button type="button" class="button is-small nano-scanner-close">Cancelar</button>
                        </footer>
                    </div>
                </div>
            </div>
        `;

        this._triggerBtn = this.querySelector('.nano-scan-trigger');
        this._modal = this.querySelector('.nano-scanner-modal');
        this._video = this.querySelector('video');
        this._statusEl = this.querySelector('.scanner-status');
        this._closeBtns = this.querySelectorAll('.nano-scanner-close');
        this._manualBtn = this.querySelector('.nano-manual-input-btn');
        this._targetInputSelector = targetInputSelector;
        this._stream = null;
        this._scanning = false;
    }

    _bindEvents() {
        this._triggerBtn.addEventListener('click', () => this.startScanning());
        this._closeBtns.forEach(btn => btn.addEventListener('click', () => this.stopScanning()));

        this._manualBtn.addEventListener('click', () => {
            const manualCode = prompt('Ingresa el código de barras numérico manualmente:');
            if (manualCode && manualCode.trim()) {
                this._onBarcodeSuccess(manualCode.trim(), 'manual');
            }
        });
    }

    async startScanning() {
        this._modal.classList.add('is-active');
        this._statusEl.textContent = 'Buscando cámara trasera...';

        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            this._statusEl.textContent = 'Cámara no soportada por el navegador.';
            if (window.nanoToast) {
                window.nanoToast('Tu navegador no soporta acceso a la cámara.', 'is-warning');
            }
            return;
        }

        try {
            // Intentar usar la cámara trasera (environment)
            this._stream = await navigator.mediaDevices.getUserMedia({
                video: {
                    facingMode: { ideal: 'environment' },
                    width: { ideal: 1280 },
                    height: { ideal: 720 }
                }
            });

            this._video.srcObject = this._stream;
            await this._video.play();
            this._statusEl.textContent = 'Enfoca el código de barras...';
            this._scanning = true;

            this._detectLoop();
        } catch (err) {
            console.error('Error accediendo a la cámara:', err);
            this._statusEl.textContent = 'No se pudo acceder a la cámara. Permiso denegado.';
            if (window.nanoToast) {
                window.nanoToast('No se otorgaron permisos de cámara.', 'is-danger');
            }
        }
    }

    async _detectLoop() {
        if (!this._scanning) return;

        if ('BarcodeDetector' in window) {
            try {
                const detector = new window.BarcodeDetector({
                    formats: ['ean_13', 'ean_8', 'upc_a', 'upc_e', 'code_128', 'qr_code']
                });

                const barcodes = await detector.detect(this._video);
                if (barcodes.length > 0) {
                    const detected = barcodes[0];
                    this._onBarcodeSuccess(detected.rawValue, detected.format);
                    return;
                }
            } catch (err) {
                // Silencioso para frame sin detección
            }
        }

        if (this._scanning) {
            requestAnimationFrame(() => this._detectLoop());
        }
    }

    _onBarcodeSuccess(barcodeValue, format) {
        this._beep();
        this.stopScanning();

        const input = document.querySelector(this._targetInputSelector);
        if (input) {
            input.value = barcodeValue;
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
            input.focus();
        }

        this.dispatchEvent(new CustomEvent('nano:barcode-detected', {
            bubbles: true,
            detail: { barcode: barcodeValue, format: format }
        }));

        if (window.nanoToast) {
            window.nanoToast(`Código detectado: <strong>${barcodeValue}</strong>`, 'is-success');
        }
    }

    _beep() {
        try {
            const ctx = new (window.AudioContext || window.webkitAudioContext)();
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.frequency.setValueAtTime(800, ctx.currentTime);
            gain.gain.setValueAtTime(0.2, ctx.currentTime);
            osc.start();
            osc.stop(ctx.currentTime + 0.15);
            if (navigator.vibrate) {
                navigator.vibrate(100);
            }
        } catch (e) {
            // Audio opcional no soportado
        }
    }

    stopScanning() {
        this._scanning = false;
        if (this._stream) {
            this._stream.getTracks().forEach(track => track.stop());
            this._stream = null;
        }
        if (this._modal) {
            this._modal.classList.remove('is-active');
        }
    }

    disconnectedCallback() {
        this.stopScanning();
    }
}

customElements.define('nano-barcode-scanner', NanoBarcodeScanner);
