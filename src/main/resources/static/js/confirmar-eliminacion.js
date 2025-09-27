/**
 * Función para confirmar y eliminar un evento
 * @param {number} eventoId - ID del evento a eliminar
 */
function confirmarEliminacion(eventoId) {
    Swal.fire({
        title: '¿Estás seguro?',
        text: "¡No podrás revertir esta acción!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar',
        backdrop: `
            rgba(0,0,0,0.7)
            url("/images/trash-icon.gif")
            center top
            no-repeat
        `
    }).then((result) => {
        if (result.isConfirmed) {
            // Mostrar loader mientras se procesa
            Swal.fire({
                title: 'Eliminando...',
                text: 'Por favor espere',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });
            
            // Enviar petición DELETE al servidor
            fetch(`/api/eventos/${eventoId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => {
                if (response.ok) {
                    // Mostrar confirmación de éxito
                    Swal.fire({
                        title: '¡Eliminado!',
                        text: 'El evento ha sido eliminado',
                        icon: 'success',
                        confirmButtonText: 'Aceptar'
                    }).then(() => {
                        // Recargar la página después de aceptar
                        window.location.reload();
                    });
                } else {
                    // Mostrar error si la respuesta no fue ok
                    response.json().then(data => {
                        Swal.fire(
                            'Error',
                            data.message || 'No se pudo eliminar el evento',
                            'error'
                        );
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire(
                    'Error',
                    'Ocurrió un error al conectar con el servidor',
                    'error'
                );
            });
        }
    });
}