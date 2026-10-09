package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ol.dao.UsuarioDAO;
import org.ol.exception.DBException;
import org.ol.model.Usuario;
import org.ol.util.Conexion;

public class UsuarioDAOImpl implements UsuarioDAO {
    private static final String INSERTAR = "{call sp_insertarusuario(?, ?, ?, ?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizarusuario(?, ?, ?, ?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminarusuario(?)}";
    private static final String CAMBIAR_PASSWORD = "{call sp_cambiarpasswordusuario(?, ?)}";
    private static final String DESACTIVAR = "{call sp_desactivarusuario(?)}";
    private static final String COLUMNAS = "id_usuario, username, email, first_name, last_name, password_hash, rol, activo, fecha_creacion";

    @Override public boolean crear(Usuario u) {
        Objects.requireNonNull(u, "El usuario no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(INSERTAR)) {
            s.setString(1, u.getUsername()); s.setString(2, u.getEmail()); s.setString(3, u.getFirstName());
            s.setString(4, u.getLastName()); s.setString(5, u.getPasswordHash()); s.setString(6, u.getRol());
            s.setBoolean(7, u.isActivo()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo crear el usuario", e); }
    }

    @Override public boolean actualizar(Usuario u) {
        Objects.requireNonNull(u, "El usuario no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ACTUALIZAR)) {
            s.setInt(1, u.getId()); s.setString(2, u.getUsername()); s.setString(3, u.getEmail());
            s.setString(4, u.getFirstName()); s.setString(5, u.getLastName()); s.setString(6, u.getPasswordHash());
            s.setString(7, u.getRol()); s.setBoolean(8, u.isActivo()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo actualizar el usuario", e); }
    }

    @Override public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id de usuario no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ELIMINAR)) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo eliminar el usuario", e); }
    }

    @Override public Usuario buscarPorId(Integer id) {
        Objects.requireNonNull(id, "El id de usuario no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM usuario WHERE id_usuario = ?", id);
    }

    @Override public Usuario buscarPorUsername(String username) {
        Objects.requireNonNull(username, "El nombre de usuario no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM usuario WHERE username = ?", username);
    }

    @Override public Usuario buscarPorEmail(String email) {
        Objects.requireNonNull(email, "El email no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM usuario WHERE email = ?", email);
    }

    @Override public Usuario iniciarSesion(String username, String passwordHash) {
        Objects.requireNonNull(username, "El nombre de usuario no puede ser null");
        Objects.requireNonNull(passwordHash, "El hash de contraseña no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM usuario WHERE username = ? AND password_hash = ? AND activo = true",
                username, passwordHash);
    }

    @Override public boolean cambiarPassword(int idUsuario, String passwordHash) {
        Objects.requireNonNull(passwordHash, "El hash de contraseña no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(CAMBIAR_PASSWORD)) {
            s.setInt(1, idUsuario); s.setString(2, passwordHash); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo cambiar la contraseña del usuario", e); }
    }

    @Override public boolean desactivarUsuario(int idUsuario) {
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(DESACTIVAR)) {
            s.setInt(1, idUsuario); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo desactivar el usuario", e); }
    }

    @Override public List<Usuario> listarActivos() {
        return listar("SELECT " + COLUMNAS + " FROM usuario WHERE activo = true ORDER BY username");
    }

    @Override public ArrayList<Usuario> listarTodos() {
        return new ArrayList<>(listar("SELECT " + COLUMNAS + " FROM usuario ORDER BY username"));
    }

    private List<Usuario> listar(String sql) {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql);
                ResultSet r = s.executeQuery()) {
            while (r.next()) usuarios.add(mapear(r)); return usuarios;
        } catch (SQLException e) { throw new DBException("No se pudieron listar los usuarios", e); }
    }

    private Usuario buscar(String sql, Object... parameters) {
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof Integer) s.setInt(i + 1, (Integer) parameters[i]);
                else s.setString(i + 1, (String) parameters[i]);
            }
            try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar el usuario", e); }
    }

    private Usuario mapear(ResultSet r) throws SQLException {
        Usuario u = new Usuario();
        u.setId(r.getInt("id_usuario")); u.setUsername(r.getString("username")); u.setEmail(r.getString("email"));
        u.setFirstName(r.getString("first_name")); u.setLastName(r.getString("last_name"));
        u.setPasswordHash(r.getString("password_hash")); u.setRol(r.getString("rol"));
        u.setActivo(r.getBoolean("activo")); u.setFechaCreacion(r.getTimestamp("fecha_creacion"));
        return u;
    }
}
