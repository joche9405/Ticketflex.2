// Funcionalidad del Modal de Compra
document.addEventListener('DOMContentLoaded', function() {
    const purchaseModal = document.getElementById('purchaseModal');
    const closePurchaseModal = document.getElementById('closePurchaseModal');
    const paymentTabs = document.querySelectorAll('.payment-tab');
    const paymentForms = document.querySelectorAll('.payment-form');
    const installmentsSelect = document.getElementById('installments');
    const installmentValue = document.getElementById('installmentValue');
    const paymentDate = document.getElementById('paymentDate');
    const traditionalPaymentForm = document.getElementById('traditionalPaymentForm');
    const ticketflexPaymentForm = document.getElementById('ticketflexPaymentForm');

    // Función para abrir el modal de compra
    window.openPurchaseModal = function(eventData) {
        // Llenar los datos del evento en el modal
        document.getElementById('modalEventImage').src = eventData.image;
        document.getElementById('modalEventName').textContent = eventData.name;
        document.getElementById('modalEventDate').textContent = eventData.date;
        document.getElementById('modalEventLocation').textContent = eventData.location;
        document.getElementById('modalEventTime').textContent = eventData.time;
        document.getElementById('modalEventDescription').textContent = eventData.description;
        document.getElementById('modalEventPrice').textContent = eventData.price;

        // Mostrar el modal
        purchaseModal.style.display = 'block';
    };

    // Cerrar el modal
    closePurchaseModal.onclick = function() {
        purchaseModal.style.display = 'none';
    };

    // Cerrar el modal al hacer clic fuera de él
    window.onclick = function(event) {
        if (event.target == purchaseModal) {
            purchaseModal.style.display = 'none';
        }
    };

    // Cambiar entre métodos de pago
    paymentTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // Remover clase active de todas las pestañas y formularios
            paymentTabs.forEach(t => t.classList.remove('active'));
            paymentForms.forEach(f => f.classList.remove('active'));

            // Agregar clase active a la pestaña seleccionada
            this.classList.add('active');

            // Mostrar el formulario correspondiente
            const paymentType = this.dataset.payment;
            document.getElementById(paymentType + 'Payment').classList.add('active');
        });
    });

    // Calcular cuotas
    installmentsSelect.addEventListener('change', function() {
        const totalPrice = parseFloat(document.getElementById('modalEventPrice').textContent.replace(/[^0-9.-]+/g, ''));
        const numInstallments = parseInt(this.value);
        const eventDate = new Date(document.getElementById('modalEventDate').textContent);
        
        if (numInstallments && totalPrice) {
            const installmentAmount = totalPrice / numInstallments;
            installmentValue.textContent = `$${installmentAmount.toFixed(2)}`;

            // Calcular fecha de pago (un mes antes del evento)
            const paymentDateObj = new Date(eventDate);
            paymentDateObj.setMonth(paymentDateObj.getMonth() - 1);
            paymentDate.textContent = paymentDateObj.toLocaleDateString();
        }
    });

    // Manejar pago tradicional
    traditionalPaymentForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Aquí iría la lógica de procesamiento del pago
        const paymentData = {
            cardNumber: document.getElementById('traditionalCardNumber2').value,
            expiryDate: document.getElementById('traditionalExpiryDate2').value,
            cvv: document.getElementById('traditionalCvv2').value,
            cardName: document.getElementById('traditionalCardName2').value,
            eventId: currentEventId, // Necesitarás definir esta variable globalmente
            paymentType: 'traditional'
        };

        // Simular procesamiento del pago
        processPayment(paymentData);
    });

    // Manejar pago TicketFlex
    ticketflexPaymentForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const paymentData = {
            name: document.getElementById('ticketflexName').value,
            email: document.getElementById('ticketflexEmail').value,
            phone: document.getElementById('ticketflexPhone').value,
            installments: document.getElementById('installments').value,
            eventId: currentEventId, // Necesitarás definir esta variable globalmente
            paymentType: 'ticketflex'
        };

        // Simular procesamiento del pago
        processPayment(paymentData);
    });

    // Función para procesar el pago
    function processPayment(paymentData) {
        // Aquí iría la lógica para enviar los datos al servidor
        console.log('Procesando pago:', paymentData);

        // Simular respuesta del servidor
        setTimeout(() => {
            alert(paymentData.paymentType === 'traditional' 
                ? '¡Pago realizado con éxito!' 
                : '¡Compra a cuotas confirmada!');
            
            // Cerrar el modal
            purchaseModal.style.display = 'none';
            
            // Actualizar el historial de compras si es necesario
            updatePurchaseHistory(paymentData);
        }, 1500);
    }

    // Función para actualizar el historial de compras
    function updatePurchaseHistory(paymentData) {
        // Aquí iría la lógica para actualizar el historial de compras
        console.log('Actualizando historial de compras:', paymentData);
    }
}); 