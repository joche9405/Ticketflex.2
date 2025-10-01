// Estado de autenticación
let isAuthenticated = false; // Variable para verificar si el usuario está autenticado
let userId = null; // Variable para almacenar el ID del usuario logueado
let idEventoToBuy = null;
let currentEventPrice = 0; // Agregamos la variable para el precio del evento

// VARIABLES PARA LA COMPRA DEL BOLETO
let buyTicketModal;
let closeModalButton;
let buyButton;
let ticketCountInput;
let graderiaInput;

// Variable para controlar si los event listeners ya están inicializados
let eventListenersInitialized = false;

// Funcion para el DOM
document.addEventListener('DOMContentLoaded', function () {
    cargarEventos(); // Cargar eventos al inicio
    agregarEventosLogin();
    agregarEventosFiltros();
    initializeEventListeners();
    initializeBuyTicketModal(); // Agregar esta línea
});


// ========================================================
// Manejo de eventos relacionados con la autenticación

function login(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData);

    fetch('/api/usuarios/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(data),
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.error || 'Credenciales incorrectas.');
                });
            }
            return response.json();
        })
        .then(data => {
            // Solo se ejecuta si las credenciales son correctas
            isAuthenticated = true;
            localStorage.setItem('usuario', JSON.stringify(data));
            userId = data.id;
            document.getElementById('userName').innerText = data.nombre;
            document.getElementById('loginButtonSection').classList.add('hidden');
            document.getElementById('userSection').classList.remove('hidden');
            alert('¡Bienvenido, ' + data.nombre + '!');
            document.getElementById('loginModal').style.display = 'none';
        })
        .catch(error => {
            // Este bloque solo se ejecuta si hubo un error (como 401)
            console.error('Error al iniciar sesión:', error.message);
            alert(error.message); // O muestra el mensaje en el DOM si prefieres
        });
}


// Manejo del formulario de registro de un usuario
function registro(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData);

    console.log("Datos a enviar:", data); // Para depuración

    fetch('/api/usuarios/registrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(async response => {
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.error || 'Error al registrar');
            }
            alert('Registro exitoso. Puedes iniciar sesión ahora.');
            document.getElementById('registerModal').style.display = 'none';
            event.target.reset();
        })
        .catch(error => {
            console.error('Error completo:', error);
            alert('Error al registrar: ' + error.message);
        });
}
document.addEventListener('DOMContentLoaded', function () {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser) {
        isAuthenticated = true;
        userId = currentUser.id;
        document.getElementById('userName').innerText = currentUser.nombre;
        document.getElementById('loginButtonSection').classList.add('hidden');
        document.getElementById('userSection').classList.remove('hidden');
    }
});

// La única y correcta versión de la función
function agregarEventosLogin() {
    // Mostrar modales
    document.getElementById('showLoginButton').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'block';
    });

    document.getElementById('showRegisterButton').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'block';
    });

    // Cerrar modales
    document.getElementById('closeLoginModal').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'none';
    });

    document.getElementById('closeRegisterModal').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'none';
    });

    // Asignar manejadores de formularios
    document.getElementById('loginForm').addEventListener('submit', login);
    // Asegúrate de tener una función 'registro' definida en tu código
    document.getElementById('registroForm').addEventListener('submit', registro);

    // --- Lógica del Restablecimiento de Contraseña ---

    const forgotPasswordLink = document.querySelector('.forgot-password');
    const loginModal = document.getElementById('loginModal');
    const forgotPasswordModal = document.getElementById('forgotPasswordModal');
    const closeForgotPasswordModal = document.getElementById('closeForgotPasswordModal');
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');

    // Evento para mostrar el modal de restablecer contraseña
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', (e) => {
            e.preventDefault();
            if (loginModal) loginModal.style.display = 'none';
            if (forgotPasswordModal) forgotPasswordModal.style.display = 'block';
        });
    }

    // Evento para cerrar el modal de restablecer contraseña
    if (closeForgotPasswordModal) {
        closeForgotPasswordModal.addEventListener('click', () => {
            if (forgotPasswordModal) forgotPasswordModal.style.display = 'none';
        });
    }

    // Manejo del formulario de restablecimiento de contraseña
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const email = document.getElementById('forgotEmail').value;

            fetch('/api/usuarios/reset-password-request', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email })
            })
                .then(response => {
                    if (response.ok) {
                        alert('Si el correo electrónico está registrado, recibirás un enlace para restablecer tu contraseña.');
                        if (forgotPasswordModal) forgotPasswordModal.style.display = 'none';
                    } else {
                        alert('Ocurrió un error. Por favor, inténtalo de nuevo.');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Ocurrió un error de conexión.');
                });
        });
    }

    // Evento para cerrar modales al hacer clic fuera
    window.onclick = function (event) {
        const loginModal = document.getElementById('loginModal');
        const registerModal = document.getElementById('registerModal');
        const forgotPasswordModal = document.getElementById('forgotPasswordModal');
        if (event.target === loginModal) {
            loginModal.style.display = 'none';
        }
        if (event.target === registerModal) {
            registerModal.style.display = 'none';
        }
        if (event.target === forgotPasswordModal) {
            forgotPasswordModal.style.display = 'none';
        }
    };
}

// Ahora, tu única llamada a la función cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function () {
    cargarEventos(); // Asegúrate de que esta función exista
    agregarEventosLogin();
    agregarEventosFiltros(); // Asegúrate de que esta función exista
});

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function () {
    cargarEventos();
    agregarEventosLogin();
    agregarEventosFiltros();
});

// Cerrar el modal al hacer clic fuera de él
window.onclick = function (event) {
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    if (event.target === loginModal) {
        loginModal.style.display = 'none';
    }
    if (event.target === registerModal) {
        registerModal.style.display = 'none';
    }
};

// Manejo del cierre de sesión
document.addEventListener('DOMContentLoaded', function () {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.onclick = function (e) {
            e.preventDefault(); // Evita que el <a href="#"> recargue la página

            fetch('/api/usuarios/logout', {
                method: 'POST'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error al cerrar sesión');
                    }
                    alert('Sesión cerrada exitosamente');
                    isAuthenticated = false;
                    userId = null;
                    document.getElementById('loginButtonSection').classList.remove('hidden');
                    document.getElementById('userSection').classList.add('hidden');
                    document.getElementById('userName').innerText = '';
                    localStorage.removeItem('currentUser');
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Error al cerrar sesión: ' + error.message);
                });
        };
    }
});


// ========================================================
/// Función para cargar y filtrar eventos
function agregarEventosFiltros() {
    // Manejadores de eventos para aplicar y resetear filtros
    document.querySelector('#resetFilters').addEventListener('click', resetearFiltros);
    document.querySelector('#filtrar').addEventListener('click', aplicarFiltros);
}

// Resetear filtros y recargar todos los eventos
function resetearFiltros() {
    document.querySelector('#lugar').value = '';
    document.querySelector('#fecha').value = '';
    document.querySelector('#categoria').value = '';
    document.querySelector('#artista').value = '';
    cargarEventos(); // Recargar todos los eventos sin aplicar filtros
}

// Función para aplicar los filtros
function aplicarFiltros() {
    const filters = {
        lugar: document.querySelector('#lugar').value.trim(),
        fecha: document.querySelector('#fecha').value.trim(),
        categoria: document.querySelector('#categoria').value.trim(),
        artista: document.querySelector('#artista').value.trim()
    };

    fetch('/api/eventos/filtrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(filters),
        credentials: 'include'
    })
        .then(async response => {
            if (response.redirected) {
                document.getElementById('loginModal').style.display = 'block';
                throw new Error('Por favor inicie sesión');
            }
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Error al filtrar');
            }
            return response.json();
        })
        .then(mostrarEventos) // Usamos la misma función para mostrar
        .catch(error => {
            console.error('Error:', error);
            alert(error.message);
        });
}
// Mostrar los eventos filtrados en la interfaz
function mostrarEventos(eventos) {
    const contenedor = document.querySelector('#eventosList');
    contenedor.innerHTML = '';

    if (!eventos?.length) {
        contenedor.innerHTML = '<p class="no-events-message">No se encontraron eventos.</p>';
        return;
    }

    eventos.forEach(evento => {
        const eventoDiv = document.createElement('div');
        eventoDiv.classList.add('evento-card');

        // Usar la URL correcta para las imágenes
        const imagenUrl = evento.imagen || '/api/imagen/default.jpg';

        eventoDiv.innerHTML = `
            <div class="evento-imagen">
                <img src="${imagenUrl}" alt="${evento.nombreEvento || 'Evento'}" onerror="this.src='/images/default-event.jpg'">
            </div>
            <div class="evento-info">
                <h3>${evento.nombreEvento || 'Evento sin nombre'}</h3>
                <p class="evento-artista"><i class="fas fa-user"></i> ${evento.artista || 'Artista por confirmar'}</p>
                <p class="evento-fecha"><i class="far fa-calendar-alt"></i> ${formatearFecha(evento.fecha)}</p>
                <p class="evento-lugar"><i class="fas fa-map-marker-alt"></i> ${evento.lugar || 'Sede por definir'}</p>
                ${evento.descripcion ? `<p class="evento-descripcion">${evento.descripcion}</p>` : ''}
                <div class="precio-disponibilidad">
                    <p class="evento-precio"><strong>Desde:</strong> $${Number(evento.precioBase || 0).toLocaleString('es-CO')}</p>
                    <p class="evento-disponibilidad"><i class="fas fa-ticket"></i> ${evento.disponibilidad} disponibles</p>
                </div>
                <button class="btn-comprar" data-evento-id="${evento.id}" data-evento-precio="${evento.precioBase || 0}">
                    <i class="fas fa-ticket-alt"></i> Comprar
                </button>
            </div>
        `;

        eventoDiv.querySelector('.btn-comprar').addEventListener('click', (e) => {
            const btn = e.currentTarget;
            const eventoId = btn.getAttribute('data-evento-id');
            const precio = parseFloat(btn.getAttribute('data-evento-precio')) || 0;
            comprarBoleto(eventoId, precio);
        });


        contenedor.appendChild(eventoDiv);
    });
}


function cargarEventos() {
    fetch('/api/eventos/listar', {
        credentials: 'include'
    })
        .then(response => {
            if (response.redirected) {
                document.getElementById('loginModal').style.display = 'block';
                throw new Error('Debes iniciar sesión');
            }
            return response.json();
        })
        .then(mostrarEventos) // Usamos la misma función para mostrar
        .catch(error => {
            console.error('Error:', error);
            document.querySelector('#eventosList').innerHTML = `
            <p class="error-message">Error al cargar eventos: ${error.message}</p>
        `;
        });
}

// Función auxiliar para formatear la fecha (opcional)
function formatearFecha(fechaString) {
    if (!fechaString) return '';

    try {
        const fecha = new Date(fechaString);
        if (isNaN(fecha.getTime())) return fechaString;

        return fecha.toLocaleDateString('es-CO', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        console.error('Error formateando fecha:', e);
        return fechaString;
    }
}


// Función para abrir el modal y manejar la compra
function comprarBoleto(eventoId, precio) {
    console.log('Comprar boleto - ID recibido:', eventoId, typeof eventoId); // Debug

    // Asegurarnos de que el ID sea un string
    if (typeof eventoId === 'object') {
        eventoId = eventoId._id || eventoId.id;
    }
    eventoId = String(eventoId);

    if (!eventoId || eventoId === 'undefined' || eventoId === 'null') {
        console.error('Error: ID del evento no definido o inválido');
        alert('Error: No se pudo identificar el evento');
        return;
    }

    if (!isAuthenticated) {
        alert('Por favor, inicie sesión para comprar boletos.');
        return;
    }

    // Guardamos el ID del evento y el precio para hacer la compra posteriormente
    idEventoToBuy = eventoId;
    currentEventPrice = precio || 0;

    console.log('ID del evento guardado:', idEventoToBuy, typeof idEventoToBuy); // Debug adicional

    // Mostrar el modal
    const buyTicketModal = document.getElementById('buyTicketModal');
    if (buyTicketModal) {
        buyTicketModal.style.display = "block";
        // Reinicializar los event listeners
        eventListenersInitialized = false;
        initializeEventListeners();
        // Calcular cuotas iniciales
        calcularCuotasTicketFlex();
    }
}

function validarTarjetaFrontend(cardNumber, expiryDate, cvv) {
  // --- Validar número de tarjeta: aceptar solo dígitos, sin espacios ni guiones ---
  if (!cardNumber || !/^\d{10,19}$/.test(cardNumber)) {
    return "Número de tarjeta inválido. Debe contener solo dígitos y tener entre 10 y 19 dígitos";
  }



  // --- Validar fecha (formato MM/AA) ---
  if (!expiryDate || !/^\d{2}\/\d{2}$/.test(expiryDate)) {
    return "Fecha de vencimiento inválida. Use formato MM/AA";
  }

  const [mes, año] = expiryDate.split('/').map(v => parseInt(v, 10));
  if (mes < 1 || mes > 12) {
    return "Mes de vencimiento inválido";
  }

  const currentDate = new Date();
  const currentMonth = currentDate.getMonth() + 1;
  const currentYear = currentDate.getFullYear() % 100; // Tomamos solo 2 dígitos del año

  if (año < currentYear || (año === currentYear && mes < currentMonth)) {
    return "La tarjeta está vencida";
  }

  // --- Validar CVV ---
  if (!cvv || !/^\d{3,4}$/.test(cvv)) {
    return "CVV inválido. Debe tener 3 o 4 dígitos";
  }

  return null; // ✅ Sin errores
}

function processPayment(method) {
    console.log('Procesando pago - ID del evento:', idEventoToBuy, typeof idEventoToBuy);

    // Asegurarnos de que el ID sea un string
    let eventoId = idEventoToBuy;
    if (typeof eventoId === 'object') {
        eventoId = eventoId._id || eventoId.id;
    }
    eventoId = String(eventoId);

    if (!eventoId || eventoId === 'undefined' || eventoId === 'null') {
        console.error('Error: ID del evento no definido o inválido en processPayment');
        alert('Error: No se ha seleccionado ningún evento');
        return;
    }

    const cantidad = (method === 'ticketflex') ?
        parseInt(document.getElementById('tf-ticketCount')?.value || '1') :
        parseInt(document.getElementById('ticketCount')?.value || '1');

    if (!isAuthenticated) {
        alert("Debes iniciar sesión para comprar boletos");
        return;
    }

    const userId = getCurrentUserId();
    if (!userId) {
        alert('Error: No se pudo identificar al usuario');
        return;
    }

    const graderia = (method === 'ticketflex') ?
        document.getElementById('tf-graderia')?.value || 'general' :
        document.getElementById('graderia')?.value || 'general';

    if (method === 'ticketflex') {
        // Lógica para TicketFlex
        const cuotas = parseInt(document.getElementById('tf-cuotas')?.value || '1');

        console.log('Datos de compra TicketFlex:', { eventoId, userId, cantidad, cuotas, graderia });

        fetch(`/api/boletas/comprar?idEvento=${eventoId}&idUsuario=${userId}&cantidad=${cantidad}&metodoPago=TICKETFLEX&cuotas=${cuotas}&graderia=${graderia}`, {
            method: 'POST',
              headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({}) // 👈 body vacío requerido

        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error en la compra: ${response.status} ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('RESPUESTA COMPLETA DEL BACKEND:', data);
                console.log('Fecha límite recibida:', data.fechaLimitePago);

                // Validar que los datos esenciales estén presentes
                if (!data.id && !data._id) {
                    console.warn('La respuesta no contiene ID de boleto');
                }

                showTicketFlexConfirmation(data);
                const buyTicketModal = document.getElementById('buyTicketModal');
                if (buyTicketModal) {
                    buyTicketModal.style.display = "none";
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al procesar el pago: ' + error.message);
            });
    } else {
        // Lógica para pago tradicional - CORREGIDO
        const cardNumber = document.getElementById('traditionalCardNumber')?.value;
        const expiryDate = document.getElementById('traditionalExpiryDate')?.value;
        const cvv = document.getElementById('traditionalCvv')?.value;

        console.log('cardNumber:', cardNumber, 'expiryDate:', expiryDate, 'cvv:', cvv);

        // ✅ PRIMERO validación frontend
        const errorValidacion = validarTarjetaFrontend(cardNumber, expiryDate, cvv);
        if (errorValidacion) {
            alert(errorValidacion);
            return;
        }

        if (!cardNumber || !expiryDate || !cvv) {
            alert('Por favor, complete todos los datos de la tarjeta');
            return;
        }

        // Crear objeto con datos de la tarjeta
        const paymentData = {
            cardNumber: cardNumber,
            expiryDate: expiryDate,
            cvv: cvv
        };

        // Usar el mismo endpoint que TicketFlex pero con método de pago diferente
        fetch(`/api/boletas/comprar?idEvento=${eventoId}&idUsuario=${userId}&cantidad=${cantidad}&metodoPago=TRADICIONAL&graderia=${graderia}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(paymentData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error en la compra: ${response.status} ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                alert("¡Compra exitosa!");
                const buyTicketModal = document.getElementById('buyTicketModal');
                if (buyTicketModal) {
                    buyTicketModal.style.display = "none";
                }

                // Mostrar confirmación para pago tradicional
                showTraditionalConfirmation(data);
            })
            .catch(error => {
                console.error('Error:', error);

                // ✅ Mostrar mensaje más específico para errores 400
                if (error.message.includes('400')) {
                    alert('Error: Datos de tarjeta inválidos. Verifique que:\n- Número de tarjeta tenga al menos 10 dígitos\n- Fecha esté en formato MM/AA\n- CVV tenga 3-4 dígitos');
                } else {
                    alert('Error al procesar el pago: ' + error.message);
                }
            });
    }
}

// Función para mostrar confirmación de pago tradicional
function showTraditionalConfirmation(data) {
    console.log('Datos de confirmación Pago Tradicional:', data);

    const boletoId = data.id || data._id || '';
    const nombreEvento = data.evento?.nombre || 'Evento no definido';
    const precioTotal = data.precioTotal || data.precio || 0;

    // 🔑 Texto QR para TRADICIONAL - Acceso inmediato
    const qrText = `✅ ENTRADA CONFIRMADA - ACCESO PERMITIDO
Evento: ${nombreEvento}
Código: ${boletoId}
Total: $${precioTotal.toLocaleString('es-CO')}
Método: Pago Tradicional
ESTADO: ACTIVA - VÁLIDA PARA ENTRADA`;

    const qrData = encodeURIComponent(qrText);

    const oldModal = document.getElementById('traditionalConfirmationModal');
    if (oldModal) oldModal.remove();

    const modal = document.createElement('div');
    modal.id = 'traditionalConfirmationModal';
    modal.className = 'modal';
    modal.style.display = 'block';

    modal.innerHTML = `
        <div class="modal-content" style="max-width: 500px; margin: 5% auto; text-align: center;">
            <span class="close" id="closeTraditionalConfirmation">&times;</span>
            <h3 style="color: #4caf50;"><i class="fas fa-check-circle"></i> ¡Compra Exitosa!</h3>
            <div class="qr-placeholder">
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${qrData}" alt="QR de la compra">
                <p style="color: black; font-weight: bold; color: #2ecc71;">✅ QR ACTIVO - VÁLIDO PARA ENTRADA</p>
                <p style="color: black;">Escanea este código para acceder al evento</p>
            </div>
            <div class="payment-details">
                <p style="color: black;"><strong>Evento:</strong> ${nombreEvento}</p>
                <p style="color: black;"><strong>Total pagado:</strong> $${precioTotal.toLocaleString('es-CO')}</p>
                <p style="color: black;"><strong>Método de pago:</strong> TRADICIONAL</p>
                <p style="color: black;"><strong>Estado:</strong> CONFIRMADO</p>
                <p style="color: green; font-weight: bold;">✔ Pago realizado exitosamente</p>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    document.getElementById('closeTraditionalConfirmation').onclick = function () {
        modal.remove();
    };

    modal.onclick = function (event) {
        if (event.target === modal) {
            modal.remove();
        }
    };
}

// Mostrar confirmación de TicketFlex como modal temporal
function showTicketFlexConfirmation(data) {
    console.log('Datos de confirmación TicketFlex:', data);

    const boletoId = data.id || data._id || '';
    const nombreEvento = data.evento?.nombre || 'Evento no definido';
    const precioTotal = data.precioTotal || data.precio || 0;
    const graderia = data.graderia || 'General';
    const estado = data.estado || 'DESCONOCIDO';

    // =====================
    // 📌 FECHA LÍMITE
    // =====================
    let fechaLimite = 'Por definir';
    let fechaLimiteObj = null;

    if (!data.fechaLimitePago) {
        fechaLimiteObj = new Date();
        fechaLimiteObj.setDate(fechaLimiteObj.getDate() + 7);
        fechaLimite = fechaLimiteObj.toLocaleDateString('es-CO', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } else {
        try {
            fechaLimiteObj = new Date(data.fechaLimitePago);
            if (!isNaN(fechaLimiteObj.getTime())) {
                fechaLimite = fechaLimiteObj.toLocaleDateString('es-CO', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });
            }
        } catch (e) {
            console.error('Error formateando fecha límite:', e);
        }
    }

    // =====================
    // 📌 FECHA PRÓXIMO PAGO
    // =====================
    let fechaProximoPago = 'Por definir';
    if (data.fechaProximoPago) {
        try {
            const fechaProximoPagoObj = new Date(data.fechaProximoPago);
            if (!isNaN(fechaProximoPagoObj.getTime())) {
                fechaProximoPago = fechaProximoPagoObj.toLocaleDateString('es-CO', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });
            }
        } catch (e) {
            console.error('Error formateando fecha próximo pago:', e);
        }
    }

    // ✅ Estado y método de pago
    const metodoPago = data.metodoPago || 'N/A';

    // 🔑 Texto QR para TICKETFLEX - Diferente según estado
    let qrText, qrStatusText, qrStatusColor;
    
    if (estado === 'PENDIENTE') {
        qrText = `⏳ RESERVA PENDIENTE - PAGO INCOMPLETO
Evento: ${nombreEvento}
Gradería: ${graderia}
Código: ${boletoId}
Total: $${precioTotal.toLocaleString('es-CO')}
Método: TicketFlex
ESTADO: PENDIENTE DE PAGO
FECHA LÍMITE: ${fechaLimite}`;
        qrStatusText = "⏳ QR NO ACTIVO - PENDIENTE DE PAGO";
        qrStatusColor = "#f39c12";
    } else {
        qrText = `✅ ENTRADA CONFIRMADA - ACCESO PERMITIDO
Evento: ${nombreEvento}
Gradería: ${graderia}
Código: ${boletoId}
Total: $${precioTotal.toLocaleString('es-CO')}
Método: TicketFlex
ESTADO: ACTIVA - VÁLIDA PARA ENTRADA`;
        qrStatusText = "✅ QR ACTIVO - VÁLIDO PARA ENTRADA";
        qrStatusColor = "#2ecc71";
    }

    const qrData = encodeURIComponent(qrText);

    const oldModal = document.getElementById('ticketflexConfirmationModal');
    if (oldModal) oldModal.remove();

    const modal = document.createElement('div');
    modal.id = 'ticketflexConfirmationModal';
    modal.className = 'modal';
    modal.style.display = 'block';

    modal.innerHTML = `
        <div class="modal-content" style="max-width: 500px; margin: 5% auto; text-align: center;">
            <span class="close" id="closeTicketflexConfirmation">&times;</span>
            <h3 style="color: #4caf50;"><i class="fas fa-check-circle"></i> ¡Reserva Exitosa!</h3>
            <div class="qr-placeholder">
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${qrData}" alt="QR de la compra">
                <p style="color: black; font-weight: bold; color: ${qrStatusColor};">${qrStatusText}</p>
                <p style="color: black;">Escanea este código para ver los detalles de tu boleta</p>
            </div>
            <div class="payment-details">
                <p style="color: black;"><strong>Total a pagar:</strong> $${precioTotal.toLocaleString('es-CO')}</p>
                <p style="color: black;"><strong>Estado:</strong> ${estado}</p>
                <p style="color: black;"><strong>Método de pago:</strong> ${metodoPago}</p>
                <p style="color: black;"><strong>Fecha límite:</strong> ${fechaLimite}</p>
                <p style="color: black;"><strong>Próximo pago:</strong> ${fechaProximoPago}</p>
                ${estado === 'PENDIENTE' ? `
                    
                ` : `<p style="color: green; font-weight: bold;">✔ Boleta cancelada totalmente</p>`}
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    document.getElementById('closeTicketflexConfirmation').onclick = function () {
        modal.remove();
    };

    modal.onclick = function (event) {
        if (event.target === modal) {
            modal.remove();
        }
    };
}






// Mostrar el botón de "Scroll to top"
window.onscroll = function () {
    var footer = document.getElementById("footer");
    var scrollToTopButton = document.getElementById("scrollToTopButton");

    // Comprobar si el usuario está cerca del footer
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - footer.offsetHeight) {
        scrollToTopButton.style.display = "block";  // Mostrar el botón cuando esté cerca del footer
    } else {
        scrollToTopButton.style.display = "none";  // Ocultar el botón cuando no esté cerca
    }
};


document.addEventListener('DOMContentLoaded', function () {
    // Mostrar modal de perfil
    document.getElementById('viewProfileBtn')?.addEventListener('click', function (e) {
        e.preventDefault();
        console.log('Botón Mi Perfil clickeado');
        loadProfileData();
        document.getElementById('profileModal').style.display = 'block';
    });

    // Cerrar modal con botón (la X)
    document.getElementById('closeProfileModal')?.addEventListener('click', function () {
        document.getElementById('profileModal').style.display = 'none';
    });

    // Cerrar modal al hacer clic fuera del contenido
    const profileModal = document.getElementById('profileModal');
    profileModal?.addEventListener('click', function (event) {
        if (event.target === profileModal) {
            profileModal.style.display = 'none';
        }
    });
});

// Cambio de pestañas en el perfil (cargar historial solo al abrir la pestaña de compras)
document.querySelectorAll('.tab-btn').forEach(button => {
    button.addEventListener('click', function () {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
        this.classList.add('active');
        const tabId = this.getAttribute('data-tab');
        document.getElementById(tabId).classList.add('active');
        if (tabId === 'purchases') {
            loadPurchaseHistory();
        }
    });
});

// Guardar cambios del perfil
document.getElementById('profileForm')?.addEventListener('submit', function (e) {
    e.preventDefault();
    saveProfileChanges();
});

function getCurrentUserId() {
    const user = JSON.parse(localStorage.getItem('usuario'));
    return user?.id;
}

// Cargar datos del perfil
function loadProfileData() {
    const userId = getCurrentUserId(); // Implementa esta función según tu sistema de autenticación

    fetch(`/api/usuarios/${userId}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('profileFirstName').value = data.nombre || '';
            document.getElementById('profileLastName').value = data.apellido || '';
            document.getElementById('profileEmail').value = data.email || '';
            document.getElementById('profilePhone').value = data.telefono || '';
            document.getElementById('profileAddress').value = data.direccion || '';
        })
        .catch(error => {
            console.error('Error al cargar perfil:', error);
            alert('Error al cargar los datos del perfil');
        });
}

// Guardar cambios del perfil
function saveProfileChanges() {
    const userId = getCurrentUserId();
    const profileData = {
        email: document.getElementById('profileEmail').value,
        telefono: document.getElementById('profilePhone').value,
        direccion: document.getElementById('profileAddress').value,
    };

    fetch(`/api/usuarios/${userId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(profileData),
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) throw new Error('Error al guardar');
            return response.json();
        })
        .then(data => {
            alert('Perfil actualizado correctamente');
            localStorage.setItem('usuario', JSON.stringify(data));
            updateUserDisplay(data); // Actualiza la visualización del usuario
            document.getElementById('profileModal').style.display = 'none';
        })
        .catch(error => {
            console.error('Error al guardar perfil:', error);
            alert('Error al guardar los cambios del perfil');
        });
}

// Modificacion de la Contraseña
document.getElementById('securityForm').addEventListener('submit', function (e) {
    e.preventDefault(); // Evita recarga
    console.log('Formulario de seguridad enviado'); // <-- Añade esto
    changePassword();   // Llama la función para cambiar contraseña
});

function changePassword() {
    const currentPassword = document.getElementById('currentPassword').value.trim();
    const newPassword = document.getElementById('newPassword').value.trim();
    const confirmPassword = document.getElementById('confirmPassword').value.trim();
    const userId = getCurrentUserId();

    if (!currentPassword || !newPassword || !confirmPassword) {
        alert('Por favor, completa todos los campos');
        return;
    }

    if (newPassword !== confirmPassword) {
        alert('La nueva contraseña y su confirmación no coinciden');
        return;
    }

    const passwordData = {
        currentPassword,
        newPassword
    };

    fetch(`/api/usuarios/${userId}/cambiar-contrasena`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(passwordData)
    })
        .then(response => {
            if (!response.ok) throw new Error('Error al cambiar la contraseña');
            return response.text(); // O .json() si el backend devuelve JSON
        })
        .then(message => {
            alert(message || 'Contraseña actualizada correctamente');
            document.getElementById('securityForm').reset();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al cambiar la contraseña. Verifica que la actual sea correcta.');
        });
}

function adjustModalHeight() {
    const modalContent = document.querySelector('#profileModal .tab-content.active');
    if (!modalContent) return;

    const maxHeight = 500; // px, ajusta según tu pantalla
    if (modalContent.scrollHeight > maxHeight) {
        modalContent.style.height = maxHeight + 'px';
        modalContent.style.overflowY = 'auto';
    } else {
        modalContent.style.height = 'auto';
        modalContent.style.overflowY = 'visible';
    }
}
// Este es el código de tu función original, pero con un cambio crucial.
function loadPurchaseHistory() {
    const userId = getCurrentUserId();
    const purchasesList = document.getElementById('purchasesList');
    if (!purchasesList) return;

    purchasesList.innerHTML = '<p>Cargando compras...</p>';

    fetch(`/api/usuarios/${userId}/historial`)
        .then(response => {
            // Maneja el caso de respuesta 204 "No Content" para una lista vacía
            if (response.status === 204) {
                return [];
            }
            if (!response.ok) {
                throw new Error('Error al cargar historial de compras');
            }
            return response.json();
        })
        .then(boletos => {
            if (!boletos || boletos.length === 0) {
                purchasesList.innerHTML = `
                    <div class="no-purchases">
                        <i class="fas fa-ticket-alt"></i>
                        <p>No has realizado ninguna compra aún</p>
                        <button class="btn-primary">Explorar eventos</button>
                    </div>
                `;
                return;
            }

            let html = '';
            // Construye el HTML de las compras
            boletos.forEach(boleto => {
                // Accede a los datos directamente del objeto `boleto`
                const fechaCompra = boleto.fechaCompra
                    ? new Date(boleto.fechaCompra).toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric' })
                    : 'Sin fecha';

                const fechaLimite = boleto.fechaLimitePago
                    ? new Date(boleto.fechaLimitePago).toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric' })
                    : 'Por definir';

                const fechaProximoPago = boleto.fechaProximoPago
                    ? new Date(boleto.fechaProximoPago).toLocaleDateString('es-CO', { year: 'numeric', month: 'long', day: 'numeric' })
                    : 'N/A';

                const nombreEvento = boleto.evento?.nombre || 'Evento desconocido';
                const precioTotal = boleto.precioTotal || 0;
                const estadoCompra = boleto.estado || 'DESCONOCIDO';
                const metodoPago = boleto.metodoPago || 'N/A';
                const boletoId = boleto.id || 'SIN-CODIGO';
                const cuotas = boleto.cuotas || 1; // Obtiene el número de cuotas, por defecto 1
                const graderia = boleto.graderia || 'General';
                
                // Obtener fecha y lugar del evento
                const fechaEvento = boleto.evento?.fecha 
                    ? new Date(boleto.evento.fecha).toLocaleDateString('es-CO', { 
                        year: 'numeric', 
                        month: 'long', 
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    })
                    : 'Fecha por definir';
                    
                // Lógica para mostrar la cuota actual (asumiendo que siempre es la primera)
                const cuotasDisplay = cuotas > 1 ? `1/${cuotas}` : 'Pago único';

                // 🔑 Texto QR diferenciado según método de pago y estado
                let qrText, qrStatusText, qrStatusColor;
                
                if (metodoPago === 'TICKETFLEX') {
                    if (estadoCompra === 'PENDIENTE') {
                        qrText = `⏳ RESERVA PENDIENTE - PAGO INCOMPLETO
Evento: ${nombreEvento}
Fecha: ${fechaEvento}
Gradería: ${graderia}
N° Boletas: ${boleto.cantidad}
Código: ${boletoId}
ESTADO: PENDIENTE DE PAGO
FECHA LÍMITE: ${fechaLimite}`;
                        qrStatusText = "⏳ QR NO ACTIVO - PENDIENTE DE PAGO";
                        qrStatusColor = "#f39c12";
                    } else {
                        qrText = `✅ ENTRADA CONFIRMADA - ACCESO PERMITIDO
Evento: ${nombreEvento}
Fecha: ${fechaEvento}
Gradería: ${graderia}
N° Boletas: ${boleto.cantidad}
Código: ${boletoId}
ESTADO: ACTIVA - VÁLIDA PARA ENTRADA`;
                        qrStatusText = "✅ QR ACTIVO - VÁLIDO PARA ENTRADA";
                        qrStatusColor = "#2ecc71";
                    }
                } else {
                    // Pago tradicional - siempre activo
                    qrText = `✅ ENTRADA CONFIRMADA - ACCESO PERMITIDO
Evento: ${nombreEvento}
Fecha: ${fechaEvento}
Graderia: ${graderia}
N° Boletas: ${boleto.cantidad}
Código: ${boletoId}
ESTADO: ACTIVA - VÁLIDA PARA ENTRADA`;
                    qrStatusText = "✅ QR ACTIVO - VÁLIDO PARA ENTRADA";
                    qrStatusColor = "#2ecc71";
                }

                const qrData = encodeURIComponent(qrText);

                html += `
                    <div class="purchase-item" data-date="${boleto.fechaCompra}">
                        <div class="purchase-header">
                            <h3>${nombreEvento}</h3>
                            <span class="purchase-date"><strong>Fecha compra:</strong> ${fechaCompra}</span>
                        </div>
                        
                        <div class="purchase-details">
                            <p><strong>Fecha evento:</strong> ${fechaEvento}</p>
                            <p><strong>Cantidad:</strong> ${boleto.cantidad}</p>
                            <p><strong>Precio total:</strong> $${precioTotal.toLocaleString('es-CO')}</p>
                            <p><strong>Estado:</strong> ${estadoCompra}</p>
                            <p><strong>Método de pago:</strong> ${metodoPago}</p>
                            <p><strong>Cuotas:</strong> ${cuotasDisplay}</p>
                            
                            ${metodoPago === 'TICKETFLEX' ? `
                                <p><strong>Fecha límite:</strong> ${fechaLimite}</p>
                                <p><strong>Próximo pago:</strong> ${fechaProximoPago}</p>
                            ` : ''}
                            
                            <div class="qr-placeholder" style="margin-top: 10px; text-align: center;">
                                <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${qrData}" alt="QR de la boleta">
                                <p style="font-weight: bold; color: ${qrStatusColor};">${qrStatusText}</p>
                                <p>Escanea para ver detalles de tu boleta</p>
                            </div>
                        </div>
                    </div>
                `;
            });
            purchasesList.innerHTML = html;
        })
        .catch(error => {
            console.error('Error al cargar historial:', error);
            purchasesList.innerHTML = '<p class="error">Ocurrió un error al cargar tus compras. Inténtalo de nuevo más tarde.</p>';
        });
}



// La función que aplica el filtro
function applyPurchaseFilter() {
    const purchaseFilter = document.getElementById('purchaseFilter');
    const filterValue = purchaseFilter.value;
    const purchaseItems = document.querySelectorAll('.purchase-item');
    const now = new Date();

    purchaseItems.forEach(item => {
        // Usa el atributo `data-date` para una fecha más precisa
        const purchaseDateString = item.dataset.date;

        // Si no hay fecha, muestra el elemento por defecto
        if (!purchaseDateString) {
            item.style.display = 'block';
            return;
        }

        const purchaseDate = new Date(purchaseDateString);
        let showItem = false;

        switch (filterValue) {
            case 'all':
                showItem = true;
                break;
            case 'last30':
                const thirtyDaysAgo = new Date();
                thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
                showItem = purchaseDate >= thirtyDaysAgo;
                break;
            case 'last6':
                const sixMonthsAgo = new Date();
                sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
                showItem = purchaseDate >= sixMonthsAgo;
                break;
            case 'year':
                const startOfYear = new Date(now.getFullYear(), 0, 1);
                showItem = purchaseDate >= startOfYear;
                break;
            default:
                showItem = true;
        }

        item.style.display = showItem ? 'block' : 'none';
    });
}

// Escucha el evento 'change' del filtro, independientemente de la carga de compras
document.addEventListener('DOMContentLoaded', function () {
    const purchaseFilter = document.getElementById('purchaseFilter');
    if (purchaseFilter) {
        purchaseFilter.addEventListener('change', applyPurchaseFilter);
    }
});




// Actualizar la visualización del usuario después de cambios
function updateUserDisplay(userData) {
    const userNameElement = document.getElementById('userNameText');
    if (userNameElement && userData.nombre) {
        userNameElement.textContent = `${userData.nombre} ${userData.apellido}`.trim();
    }
}

// Función para inicializar los event listeners
function initializeEventListeners() {
    // Si ya están inicializados, no hacer nada
    if (eventListenersInitialized) {
        console.log('Los event listeners ya están inicializados');
        return;
    }

    console.log('Inicializando event listeners...'); // Debug

    // Event listeners para las pestañas de pago
    const paymentTabs = document.querySelector('.payment-tabs');
    if (!paymentTabs) {
        console.warn('No se encontró el contenedor de pestañas de pago');
        return;
    }

    // Agregar event listeners a los botones de pestaña
    const tabButtons = paymentTabs.querySelectorAll('.tab-btn');
    if (tabButtons && tabButtons.length > 0) {
        tabButtons.forEach(button => {
            if (button) {
                // Remover event listener existente si lo hay
                const newButton = button.cloneNode(true);
                button.parentNode.replaceChild(newButton, button);

                // Agregar nuevo event listener
                newButton.addEventListener('click', function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    const tabName = this.getAttribute('data-tab');
                    if (tabName) {
                        switchTab(tabName);
                    }
                });
            }
        });
    } else {
        console.warn('No se encontraron botones de pestaña');
    }

    // Event listeners para los campos de TicketFlex
    const tfTicketCount = document.getElementById('tf-ticketCount');
    const tfCuotas = document.getElementById('tf-cuotas');

    if (tfTicketCount) {
        tfTicketCount.addEventListener('change', calcularCuotasTicketFlex);
    }

    if (tfCuotas) {
        tfCuotas.addEventListener('change', calcularCuotasTicketFlex);
    }

    // Marcar como inicializados
    eventListenersInitialized = true;
}

// Función para cambiar entre pestañas
function switchTab(tabName) {
    console.log('Cambiando a pestaña:', tabName); // Debug

    const paymentTabs = document.querySelector('.payment-tabs');
    if (!paymentTabs) {
        console.error('No se encontró el contenedor de pestañas');
        return;
    }

    // Obtener todos los elementos necesarios
    const tabContents = document.querySelectorAll('.payment-tab-content');
    const tabButtons = paymentTabs.querySelectorAll('.tab-btn');
    const selectedTab = document.getElementById(`${tabName}-tab`);
    const selectedButton = paymentTabs.querySelector(`.tab-btn[data-tab="${tabName}"]`);

    // Verificar que todos los elementos existan
    if (!tabContents || !tabButtons || !selectedTab || !selectedButton) {
        console.error('No se encontraron todos los elementos necesarios para cambiar de pestaña');
        return;
    }

    try {
        // Ocultar todos los contenidos de pestañas
        tabContents.forEach(tab => {
            if (tab) {
                tab.style.display = 'none';
            }
        });

        // Desactivar todos los botones de pestaña
        tabButtons.forEach(btn => {
            if (btn && btn.classList) {
                btn.classList.remove('active');
            }
        });

        // Mostrar la pestaña seleccionada y activar su botón
        if (selectedTab) {
            selectedTab.style.display = 'block';
        }

        if (selectedButton && selectedButton.classList) {
            selectedButton.classList.add('active');
        }

        // Si es la pestaña de TicketFlex, calcular las cuotas
        if (tabName === 'ticketflex') {
            calcularCuotasTicketFlex();
        }
    } catch (error) {
        console.error('Error al cambiar de pestaña:', error);
    }
}

// Función para calcular el valor de las cuotas de TicketFlex
function calcularCuotasTicketFlex() {
    const tfTicketCount = document.getElementById('tf-ticketCount');
    const tfCuotas = document.getElementById('tf-cuotas');
    const valorCuotaElement = document.getElementById('valorCuota');
    const totalTicketFlexElement = document.getElementById('totalTicketFlex');

    if (!tfTicketCount || !tfCuotas || !valorCuotaElement || !totalTicketFlexElement) {
        console.error('No se encontraron todos los elementos necesarios para calcular cuotas');
        return;
    }

    const cantidad = parseInt(tfTicketCount.value || '1');
    const cuotas = parseInt(tfCuotas.value || '1');

    if (!currentEventPrice) {
        console.error('Precio del evento no definido');
        return;
    }

    const total = currentEventPrice * cantidad;
    const tasaInteres = 0.005; // 0.5% mensual

    // Calcular valor de cuotas con interés
    const valorCuota = (total * (1 + (tasaInteres * cuotas))) / cuotas;
    const totalConInteres = valorCuota * cuotas;

    // Actualizar la interfaz
    valorCuotaElement.textContent = `$${valorCuota.toFixed(2)}`;
    totalTicketFlexElement.textContent = `$${totalConInteres.toFixed(2)}`;
}

// Función para mostrar/ocultar campos de tarjeta
function toggleCardFields() {
    const bankSelect = document.getElementById('bank');
    const cardDetails = document.getElementById('cardDetails');

    if (bankSelect.value) {
        cardDetails.style.display = 'block';
    } else {
        cardDetails.style.display = 'none';
    }
}

// Función para inicializar los elementos del modal de compra
function initializeBuyTicketModal() {
    buyTicketModal = document.getElementById("buyTicketModal");
    closeModalButton = document.getElementById("closeModal");
    buyButton = document.getElementById("buyButton");
    ticketCountInput = document.getElementById("ticketCount");
    graderiaInput = document.getElementById("graderia");

    // Agregar event listeners solo si los elementos existen
    if (closeModalButton) {
        closeModalButton.addEventListener("click", function () {
            if (buyTicketModal) {
                buyTicketModal.style.display = "none";
            }
        });
    }

    if (buyButton) {
        buyButton.addEventListener("click", function () {
            if (!idEventoToBuy) {
                alert('Error: No se ha seleccionado ningún evento');
                return;
            }

            const cantidad = parseInt(ticketCountInput?.value || '1', 10);
            const userId = getCurrentUserId();

            // Validación de la cantidad
            if (isNaN(cantidad) || cantidad < 1 || cantidad > 5) {
                alert('Cantidad no válida. Debe ser un número entre 1 y 5.');
                return;
            }

            if (!userId) {
                alert('Debes iniciar sesión para realizar una compra.');
                return;
            }

            // Realizamos la solicitud de compra a la API
            fetch(`/api/eventos/${idEventoToBuy}/comprar?idUsuario=${userId}&cantidad=${cantidad}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Error en la compra: ${response.status} ${response.statusText}`);
                    }
                    return response.text();
                })
                .then(data => {
                    alert('¡Compra realizada con éxito!');
                    if (buyTicketModal) {
                        buyTicketModal.style.display = "none";
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Error al realizar la compra: ' + error.message);
                });
        });
        
    }
}
const mobileMenu = document.getElementById("mobileMenu");
const navLinks = document.getElementById("navLinks");

mobileMenu.addEventListener("click", () => {
  navLinks.classList.toggle("active");
});




