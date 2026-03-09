// Funcionalidad del Modal de Compra - TicketFlex con PayU
document.addEventListener('DOMContentLoaded', function() {
    const purchaseModal = document.getElementById('purchaseModal');
    const closePurchaseModal = document.getElementById('closePurchaseModal');
    const paymentTabs = document.querySelectorAll('.payment-tab');
    const paymentForms = document.querySelectorAll('.payment-form');
    
    // Elementos de la UI para cálculos
    const ticketCountInput = document.getElementById('ticketCountTraditional');
    const btnPayNormal = document.getElementById('btnPayNormal');
    const btnPayFlex = document.getElementById('btnPayFlex');

    // Variable global para mantener el ID del evento de MongoDB
    let currentEventId = null;

    /**
     * Función global para abrir el modal y cargar los datos
     * @param {Object} eventData - Objeto con los datos del evento (id, name, price, etc.)
     */
    window.openPurchaseModal = function(eventData) {
        // 1. Verificación del ID de MongoDB
        if (!eventData.id) {
            console.error("Error: El evento no tiene un ID de MongoDB válido.");
            return;
        }
        
        currentEventId = eventData.id;

        // 2. Llenar los datos visuales del modal
        document.getElementById('modalEventImage').src = eventData.image || 'default.jpg';
        document.getElementById('modalEventName').textContent = eventData.name;
        document.getElementById('modalEventDate').textContent = eventData.date;
        document.getElementById('modalEventLocation').textContent = eventData.location;
        document.getElementById('modalEventTime').textContent = eventData.time || '--:--';
        document.getElementById('modalEventDescription').textContent = eventData.description;
        document.getElementById('modalEventPrice').textContent = eventData.price;

        // 3. Mostrar el modal
        purchaseModal.style.display = 'block';
    };

    // --- MANEJO DE CIERRE ---
    closePurchaseModal.onclick = () => purchaseModal.style.display = 'none';

    window.onclick = (event) => {
        if (event.target == purchaseModal) purchaseModal.style.display = 'none';
    };

    // --- PESTAÑAS DE PAGO ---
    paymentTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            paymentTabs.forEach(t => t.classList.remove('active'));
            paymentForms.forEach(f => f.classList.remove('active'));

            this.classList.add('active');
            const paymentType = this.dataset.payment;
            document.getElementById(paymentType + 'Payment').classList.add('active');
        });
    });

    // --- LÓGICA DE PROCESAMIENTO DE PAGO ---

    // 1. Pago Tradicional (Redirección a PayU vía Backend)
    btnPayNormal.addEventListener('click', async function() {
        const cantidad = ticketCountInput ? ticketCountInput.value : 1;

        if (!currentEventId) {
            alert("Error: No se ha seleccionado ningún evento.");
            return;
        }

        try {
            // Cambiar el botón a estado de carga
            btnPayNormal.disabled = true;
btnPayNormal.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Conectando...';
            const response = await fetch('/api/pagos/pagar', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    idBoleto: currentEventId, // ID de MongoDB enviado al backend
                    cantidad: parseInt(cantidad),
                    metodoPago: "TRADICIONAL"
                })
            });

            if (response.ok) {
                // El backend debe devolver el HTML del formulario oculto de PayU
                const htmlForm = await response.text();
                
                const div = document.createElement('div');
                div.style.display = 'none'; // Oculto para el usuario
                div.innerHTML = htmlForm;
                document.body.appendChild(div);

                // El script devuelto por el backend enviará el formulario automáticamente
            } else {
                const errorData = await response.json().catch(() => ({}));
                alert("Error al iniciar el pago: " + (errorData.message || "Servidor no disponible"));
            }
        } catch (error) {
            console.error("Error en la petición:", error);
            alert("Ocurrió un error al intentar conectar con la pasarela de pagos.");
        } finally {
            btnPayNormal.disabled = false;
            btnPayNormal.textContent = "Pagar con PayU";
        }
    });

    // 2. Pago TicketFlex (Lógica futura)
    btnPayFlex.addEventListener('click', function() {
        alert("El sistema de cuotas TicketFlex está en mantenimiento. Por favor, usa el método tradicional temporalmente.");
    });
});