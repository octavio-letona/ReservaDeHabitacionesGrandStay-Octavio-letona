package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ol.dao.HuespedDAO;
import org.ol.exception.DBException;
import org.ol.model.Huesped;
import org.ol.util.Conexion;

public class HuespedDAOImpl implements HuespedDAO {
    private static final String INSERTAR = "{call sp_insertarhuesped(?, ?, ?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizarhuesped(?, ?, ?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminarhuesped(?)}";
    private static final String COLUMNAS = "id_huesped, documento_identificacion, nombre_huesped, apellido_huesped, telefono_huesped, correo_electronico, nacionalidad";

    @Override public boolean crear(Huesped h) {
        Objects.requireNonNull(h, "El huésped no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(INSERTAR)) {
            s.setString(1, h.getDocumentoIdentificacion()); s.setString(2, h.getNombreHuesped());
            s.setString(3, h.getApellidoHuesped()); s.setString(4, h.getTelefonoHuesped());
            s.setString(5, h.getCorreoElectronico()); s.setString(6, h.getNacionalidad());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo crear el huésped", e); }
    }

    @Override public boolean actualizar(Huesped h) {
        Objects.requireNonNull(h, "El huésped no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ACTUALIZAR)) {
            s.setInt(1, h.getIdHuesped()); s.setString(2, h.getDocumentoIdentificacion());
            s.setString(3, h.getNombreHuesped()); s.setString(4, h.getApellidoHuesped());
            s.setString(5, h.getTelefonoHuesped()); s.setString(6, h.getCorreoElectronico()); s.setString(7, h.getNacionalidad());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo actualizar el huésped", e); }
    }

    @Override public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id del huésped no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ELIMINAR)) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo eliminar el huésped", e); }
    }

    @Override public Huesped buscarPorId(Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM huesped WHERE id_huesped = ?", id);
    }

    @Override public Huesped buscarPorDocumento(String documento) {
        Objects.requireNonNull(documento, "El documento no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM huesped WHERE documento_identificacion = ?", documento);
    }

    private Huesped buscar(String sql, Object key) {
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            if (key instanceof Integer) s.setInt(1, (Integer) key); else s.setString(1, (String) key);
            try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar el huésped", e); }
    }

    @Override public List<Huesped> buscarPorApellido(String apellido) {
        Objects.requireNonNull(apellido, "El apellido no puede ser null"); List<Huesped> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM huesped WHERE apellido_huesped LIKE ? ORDER BY apellido_huesped, nombre_huesped")) {
            s.setString(1, "%" + apellido + "%"); try (ResultSet r = s.executeQuery()) { while (r.next()) items.add(mapear(r)); }
            return items;
        } catch (SQLException e) { throw new DBException("No se pudieron buscar huéspedes por apellido", e); }
    }

    @Override public ArrayList<Huesped> listarTodos() {
        ArrayList<Huesped> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM huesped ORDER BY apellido_huesped, nombre_huesped"); ResultSet r = s.executeQuery()) {
            while (r.next()) items.add(mapear(r)); return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar los huéspedes", e); }
    }

    private Huesped mapear(ResultSet r) throws SQLException {
        return new Huesped(r.getInt("id_huesped"), r.getString("documento_identificacion"),
                r.getString("nombre_huesped"), r.getString("apellido_huesped"),
                r.getString("telefono_huesped"), r.getString("correo_electronico"), r.getString("nacionalidad"));
    }
}
