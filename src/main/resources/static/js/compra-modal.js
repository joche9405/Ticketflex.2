// Variables globales
let currentEvent = null;
let currentEventPrice = 0;
let maxInstallments = 6;

// Cuando se abre el modal de compra
document.getElementById('buyTicketModal').addEventListener('show.bs.modal', function(event) {
    const button = event.relatedTarget;
    currentEvent = JSON.parse(button.getAttribute('data-event'));
    currentEventPrice = getPriceBySection(document.getElementById('graderia').value);
    
    updateTraditionalPrices();
    updateTicketFlexPrices();
});

// Cambios en la pestaña de pago tradicional
document.getElementById('ticketCount').addEventListener('change', updateTraditionalPrices);
document.getElementById('graderia').addEventListener('change', function() {
    currentEventPrice = getPriceBySection(this.value);
    updateTraditionalPrices();
    updateTicketFlexPrices();
});

// Cambios en la pestaña de TicketFlex
document.getElementById('tf-ticketCount').addEventListener('change', updateTicketFlexPrices);
document.getElementById('tf-graderia').addEventListener('change', function() {
    currentEventPrice = getPriceBySection(this.value);
    updateTicketFlexPrices();
});

// Función para obtener precio por sección
function getPriceBySection(section) {
    switch(section) {
        case 'vip': return currentEvent.precioVIP || currentEvent.precioBase * 1.5;
        case 'preferente': return currentEvent.precioPreferencial || currentEvent.precioBase * 1.3;
        default: return currentEvent.precioBase;
    }
}

// Actualizar precios en pago tradicional
function updateTraditionalPrices() {
    const count = parseInt(document.getElementById('ticketCount').value);
    const total = currentEventPrice * count;
    
    document.getElementById('traditionalPrice').textContent = formatCurrency(currentEventPrice);
    document.getElementById('traditionalTotal').textContent = formatCurrency(total);
}

// Actualizar precios en TicketFlex
function updateTicketFlexPrices() {
    const count = parseInt(document.getElementById('tf-ticketCount').value);
    const total = currentEventPrice * count;
    
    // Calcular cuotas disponibles (máximo 6 o hasta 1 mes antes del evento)
    const eventDate = new Date(currentEvent.fecha);
    const today = new Date();
    const monthsDiff = (eventDate.getFullYear() - today.getFullYear()) * 12 + 
                      (eventDate.getMonth() - today.getMonth()) - 1;
    
    const availableInstallments = Math.min(maxInstallments, monthsDiff);
    const monthlyPayment = total / availableInstallments;
    
    // Actualizar UI
    document.getElementById('ticketflexPrice').textContent = formatCurrency(currentEventPrice);
    document.getElementById('monthlyPayment').textContent = formatCurrency(monthlyPayment);
    document.getElementById('ticketflexTotal').textContent = formatCurrency(total);
    
    // Calcular fechas de pago
    const nextPaymentDate = new Date();
    nextPaymentDate.setMonth(nextPaymentDate.getMonth() + 1);
    
    document.getElementById('nextPaymentDate').textContent = formatDate(nextPaymentDate);
    
    // Generar calendario de pagos
    generatePaymentCalendar(total, availableInstallments);
}

// Generar calendario de pagos
function generatePaymentCalendar(total, installments) {
    const calendarList = document.getElementById('paymentCalendar');
    calendarList.innerHTML = '';
    
    const monthlyPayment = total / installments;
    const today = new Date();
    
    for(let i = 1; i <= installments; i++) {
        const paymentDate = new Date();
        paymentDate.setMonth(today.getMonth() + i);
        
        const listItem = document.createElement('li');
        listItem.innerHTML = `
            <span class="payment-number">Cuota ${i}/${installments}</span>
            <span class="payment-amount">${formatCurrency(monthlyPayment)}</span>
            <span class="payment-date">Vence ${formatDate(paymentDate)}</span>
        `;
        
        calendarList.appendChild(listItem);
    }
}

// Formatear moneda
function formatCurrency(amount) {
    return '$' + amount.toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

// Formatear fecha
function formatDate(date) {
    return date.toLocaleDateString('es-CO', { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric' 
    });
}

// Cambiar entre pestañas
function openPaymentTab(tabName, event) {
    // Actualizar botones activos
    const tabButtons = document.querySelectorAll('.payment-tabs .tab-btn');
    tabButtons.forEach(btn => btn.classList.remove('active'));
    event.currentTarget.classList.add('active');
    
    // Mostrar contenido correspondiente
    const tabContents = document.querySelectorAll('.payment-tab-content');
    tabContents.forEach(content => content.style.display = 'none');
    document.getElementById(`${tabName}-tab`).style.display = 'block';
}

// Botón de compra con TicketFlex
document.getElementById('buyTicketflexButton').addEventListener('click', function() {
    if (!currentEvent) return;
    
    const userId = getCurrentUserId(); // Implementa esta función según tu auth system
    if (!userId) {
        alert('Debes iniciar sesión para usar TicketFlex');
        return;
    }
    
    const count = parseInt(document.getElementById('tf-ticketCount').value);
    const section = document.getElementById('tf-graderia').value;
    const total = currentEventPrice * count;
    
    // Calcular cuotas
    const eventDate = new Date(currentEvent.fecha);
    const today = new Date();
    const monthsDiff = (eventDate.getFullYear() - today.getFullYear()) * 12 + 
                      (eventDate.getMonth() - today.getMonth()) - 1;
    
    const installments = Math.min(maxInstallments, monthsDiff);
    
    // Preparar datos para la API
    const requestData = {
        idEvento: currentEvent.id,
        fechaEvento: currentEvent.fecha,
        idUsuario: userId,
        detalles: [{
            cantidad: count,
            valor: currentEventPrice,
            graderia: section
        }]
    };
    
    // Llamar al backend
    fetch('/api/pagos-cuotas/generar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getAuthToken()
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.formulario) {
            // Mostrar confirmación
            showConfirmationModal(data);
        } else {
            throw new Error('Error al generar cuotas');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ocurrió un error al procesar tu solicitud');
    });
});

// Mostrar modal de confirmación
function showConfirmationModal(data) {
    const modal = document.getElementById('confirmationModal');
    const content = document.getElementById('confirmationContent');
    
    content.innerHTML = `
        <div class="ticketflex-confirmation">
            <div class="confirmation-header">
                <i class="fas fa-check-circle success-icon"></i>
                <h3>¡Plan de pagos creado con éxito!</h3>
            </div>
            
            <div class="confirmation-details">
                <p><strong>Evento:</strong> ${currentEvent.nombreEvento}</p>
                <p><strong>Total:</strong> ${formatCurrency(data.cuotas.reduce((sum, cuota) => sum + cuota.valor, 0))}</p>
                <p><strong>Cuotas:</strong> ${data.cuotas.length}</p>
                <p><strong>Próximo pago:</strong> ${formatDate(new Date(data.cuotas[0].fechaVencimiento))}</p>
            </div>
            
            <div class="confirmation-actions">
                <button id="proceedToPayment" class="btn-primary">Pagar primera cuota ahora</button>
                <button id="viewPaymentPlan" class="btn-outline">Ver plan de pagos</button>
            </div>
        </div>
    `;
    
    // Cerrar modal de compra
    const buyModal = bootstrap.Modal.getInstance(document.getElementById('buyTicketModal'));
    buyModal.hide();
    
    // Mostrar modal de confirmación
    const confirmationModal = new bootstrap.Modal(modal);
    confirmationModal.show();
    
    // Manejar botones de confirmación
    document.getElementById('proceedToPayment').addEventListener('click', function() {
        // Crear un div oculto para renderizar el formulario PayU
        const formContainer = document.createElement('div');
        formContainer.style.display = 'none';
        formContainer.innerHTML = data.formulario;
        document.body.appendChild(formContainer);
        
        // Enviar el formulario automáticamente
        const form = formContainer.querySelector('form');
        form.submit();
    });
    
    document.getElementById('viewPaymentPlan').addEventListener('click', function() {
        // Redirigir a la página de "Mis cuotas"
        window.location.href = '/mis-cuentas.html';
        confirmationModal.hide();
    });
}

// Cerrar modales
document.getElementById('closeConfirmationModal').addEventListener('click', function() {
    const modal = bootstrap.Modal.getInstance(document.getElementById('confirmationModal'));
    modal.hide();
});