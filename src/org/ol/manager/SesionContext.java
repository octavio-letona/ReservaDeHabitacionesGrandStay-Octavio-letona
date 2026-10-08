package org.ol.manager;

import org.ol.model.Usuario;
import java.time.LocalDateTime;

public class SesionContext {

    private static SesionContext instancia;

    private Usuario usuarioActual;
    private String rol;
    private LocalDateTime horaInicioSesion;

    private SesionContext() {
    }

    public static synchronized SesionContext getInstancia() {
        if (instancia == null) {
            instancia = new SesionContext();
        }
        return instancia;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            this.rol = usuario.getRol();
            this.horaInicioSesion = LocalDateTime.now();
        } else {
            this.rol = null;
            this.horaInicioSesion = null;
        }
    }

    public String getRol() {
        return rol;
    }

    public LocalDateTime getHoraInicioSesion() {
        return horaInicioSesion;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
        this.rol = null;
        this.horaInicioSesion = null;
    }
}