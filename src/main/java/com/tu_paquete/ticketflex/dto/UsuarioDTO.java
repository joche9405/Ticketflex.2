package com.tu_paquete.ticketflex.dto;

public class UsuarioDTO {
    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private String password;

    // Constructor sin argumentos (requerido para frameworks como Spring)
    public UsuarioDTO() {
    }

    // Constructor con todos los campos
    public UsuarioDTO(String id, String nombre, String apellido, String email,
            String telefono, String direccion, String password) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Método toString() para logging/depuración
    @Override
    public String toString() {
        return "UsuarioDTO{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                // No incluir password por seguridad
                '}';
    }

    // Builder Pattern (opcional)
    public static UsuarioDTOBuilder builder() {
        return new UsuarioDTOBuilder();
    }

    public static class UsuarioDTOBuilder {
        private String id;
        private String nombre;
        private String apellido;
        private String email;
        private String telefono;
        private String direccion;
        private String password;

        public UsuarioDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UsuarioDTOBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        // ... otros métodos para cada campo

        public UsuarioDTO build() {
            return new UsuarioDTO(id, nombre, apellido, email, telefono, direccion, password);
        }
    }
}