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

function agregarEventosLogin() {
    // Mostrar el modal de inicio de sesión
    document.getElementById('showLoginButton').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'block';
    });

    // Mostrar el modal de registro
    document.getElementById('showRegisterButton').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'block';
    });

    // Cerrar el modal de inicio de sesión
    document.getElementById('closeLoginModal').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'none';
    });

    // Cerrar el modal de registro
    document.getElementById('closeRegisterModal').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'none';
    });

    // Manejo del formulario de inicio de sesión
    document.getElementById('loginForm').addEventListener('submit', login);
    document.getElementById('registroForm').addEventListener('submit', registro);
}

function login(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData);

    fetch('http://localhost:8080/api/usuarios/login', {
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

    fetch('http://localhost:8080/api/usuarios/registrar', {
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

// Luego asigna los event listeners
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
    document.getElementById('registroForm').addEventListener('submit', registro);
}

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
        logoutBtn.addEventListener('click', function (e) {
            e.preventDefault(); // Evita que el <a href="#"> recargue la página

            fetch('http://localhost:8080/api/usuarios/logout', {
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
        });
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

    fetch('http://localhost:8080/api/eventos/filtrar', {
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
    fetch('http://localhost:8080/api/eventos/listar', {
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

// Función para procesar pago según método seleccionado
function processPayment(method) {
    console.log('Procesando pago - ID del evento:', idEventoToBuy, typeof idEventoToBuy); // Debug

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

    if (method === 'ticketflex') {
        // Lógica para TicketFlex
        const cuotas = parseInt(document.getElementById('tf-cuotas')?.value || '1');
        const graderia = document.getElementById('tf-graderia')?.value || 'general';

        console.log('Datos de compra TicketFlex:', { eventoId, userId, cantidad, cuotas, graderia }); // Debug

        fetch(`/api/boletas/comprar?idEvento=${eventoId}&idUsuario=${userId}&cantidad=${cantidad}&metodoPago=TICKETFLEX&cuotas=${cuotas}&graderia=${graderia}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error en la compra: ${response.status} ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
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
        // Lógica para pago tradicional
        const cardNumber = document.getElementById('traditionalCardNumber')?.value;
        const expiryDate = document.getElementById('traditionalExpiryDate')?.value;
        const cvv = document.getElementById('traditionalCvv2')?.value;
        const graderia = document.getElementById('graderia')?.value;

        console.log('cardNumber:', cardNumber, 'expiryDate:', expiryDate, 'cvv:', cvv);

        if (!cardNumber || !expiryDate || !cvv) {
            alert('Por favor, complete todos los datos de la tarjeta');
            return;
        }

        fetch(`http://localhost:8080/api/eventos/${eventoId}/comprar?idUsuario=${userId}&cantidad=${cantidad}&graderia=${graderia}`, {
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
                alert("Compra exitosa: " + data);
                buyTicketModal.style.display = "none";
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al procesar el pago: ' + error.message);
            });
    }
}

// Mostrar confirmación de TicketFlex como modal temporal
function showTicketFlexConfirmation(data) {
    // Log para depuración
    console.log('Datos de confirmación TicketFlex:', data);

    // Buscar el campo correcto para el id del boleto
    const boletoId = data.idBoleto || data.boletoId || data.id || '';

    // Eliminar cualquier modal de confirmación anterior
    const oldModal = document.getElementById('ticketflexConfirmationModal');
    if (oldModal) oldModal.remove();

    // Crear el modal
    const modal = document.createElement('div');
    modal.id = 'ticketflexConfirmationModal';
    modal.className = 'modal';
    modal.style.display = 'block';
    modal.innerHTML = `
        <div class="modal-content" style="max-width: 500px; margin: 5% auto; text-align: center;">
            <span class="close" id="closeTicketflexConfirmation">&times;</span>
            <h3 style="color: #4caf50;"><i class="fas fa-check-circle"></i> ¡Reserva Exitosa!</h3>
            <div class="qr-placeholder">
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=TEMP-${boletoId}" alt="QR Temporal">
                <p class="text-muted">QR temporal - Se activará al completar el pago</p>
            </div>
            <div class="payment-details">
                <p><strong>Total a pagar:</strong> $${data.precioTotal}</p>
                <p><strong>Fecha límite:</strong> ${data.fechaLimitePago ? data.fechaLimitePago : 'Por definir'}</p>
                <button onclick="location.href='/completar-pago?id=${boletoId}'" class="btn-pay-now">
                    Completar Pago Ahora
                </button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);

    // Cerrar el modal al hacer clic en la X
    document.getElementById('closeTicketflexConfirmation').onclick = function () {
        modal.remove();
    };
    // Cerrar el modal al hacer clic fuera del contenido
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
        nombre: document.getElementById('profileFirstName').value,
        apellido: document.getElementById('profileLastName').value,
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


// Cargar historial de compras SOLO en la pestaña del perfil
function loadPurchaseHistory() {
    const userId = getCurrentUserId();
    const purchasesList = document.getElementById('purchasesList');
    if (!purchasesList) return;
    purchasesList.innerHTML = '<p>Cargando compras...</p>';

    fetch(`/api/usuarios/${userId}/historial`)
        .then(response => response.json())
        .then(purchases => {
            if (!purchases || purchases.length === 0) {
                purchasesList.innerHTML = '<p class="no-purchases">No has realizado ninguna compra aún.</p>';
                return;
            }

            let html = '';
            purchases.forEach(purchase => {
                // Fecha segura
                let fechaCompra = 'Sin fecha';
                if (purchase.fecha) {
                    const fechaObj = new Date(purchase.fecha);
                    if (!isNaN(fechaObj.getTime())) {
                        fechaCompra = fechaObj.toLocaleDateString();
                    }
                }

                // Estado seguro
                const estadoCompra = purchase.estado ? purchase.estado : 'Sin estado';

                html += `
                <div class="purchase-item">
                    <div class="purchase-header">
                        <h3>${purchase.evento && purchase.evento.nombre ? purchase.evento.nombre : 'Evento desconocido'}</h3>
                        <span class="purchase-amount">$${purchase.total ? purchase.total.toLocaleString() : '0'}</span>
                    </div>
                    <div class="purchase-meta">
                        <span class="purchase-date">${fechaCompra}</span>
                        <span class="purchase-status ${estadoCompra}">${estadoCompra}</span>
                    </div>
                    <div class="purchase-details">
                        ${Array.isArray(purchase.boletos) && purchase.boletos.length > 0 ? purchase.boletos.map(boleto => `
                            <div class="ticket-item">
                                <span>${boleto.cantidad} x ${boleto.tipo} (${boleto.graderia})</span>
                                <span>$${(boleto.precio * boleto.cantidad).toLocaleString()}</span>
                            </div>
                        `).join('') : '<div class="ticket-item">Sin información de boletos</div>'}
                    </div>
                </div>
                `;
            });

            purchasesList.innerHTML = html;
        })
        .catch(error => {
            console.error('Error al cargar historial:', error);
            purchasesList.innerHTML = '<p class="error">Error al cargar el historial de compras</p>';
        });
}

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
            fetch(`http://localhost:8080/api/eventos/${idEventoToBuy}/comprar?idUsuario=${userId}&cantidad=${cantidad}`, {
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




