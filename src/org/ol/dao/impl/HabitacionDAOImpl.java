package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ol.dao.HabitacionDAO;
import org.ol.exception.DBException;
import org.ol.model.Habitacion;
import org.ol.util.Conexion;

public class HabitacionDAOImpl implements HabitacionDAO {
    private static final String INSERTAR = "{call sp_insertarhabitacion(?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizarhabitacion(?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminarhabitacion(?)}";
    private static final String COLUMNAS = "id_habitacion, numero_habitacion, piso, estado_habitacion, id_tipo_habitacion";

    @Override public boolean crear(Habitacion h) {
        Objects.requireNonNull(h, "La habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(INSERTAR)) {
            s.setString(1, h.getNumeroHabitacion()); s.setInt(2, h.getPiso());
            s.setString(3, h.getEstadoHabitacion()); s.setInt(4, h.getIdTipoHabitacion());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo crear la habitación", e); }
    }

    @Override public boolean actualizar(Habitacion h) {
        Objects.requireNonNull(h, "La habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ACTUALIZAR)) {
            s.setInt(1, h.getIdHabitacion()); s.setString(2, h.getNumeroHabitacion());
            s.setInt(3, h.getPiso()); s.setString(4, h.getEstadoHabitacion()); s.setInt(5, h.getIdTipoHabitacion());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo actualizar la habitación", e); }
    }

    @Override public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id de habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ELIMINAR)) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo eliminar la habitación", e); }
    }

    @Override public Habitacion buscarPorId(Integer id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM habitacion WHERE id_habitacion = ?", id);
    }

    @Override public Habitacion buscarPorNumero(String numero) {
        Objects.requireNonNull(numero, "El número de habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM habitacion WHERE numero_habitacion = ?")) {
            s.setString(1, numero); try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar la habitación por número", e); }
    }

    @Override public List<Habitacion> listarPorEstado(String estado) {
        return listar("SELECT " + COLUMNAS + " FROM habitacion WHERE estado_habitacion = ? ORDER BY numero_habitacion", estado);
    }

    @Override public List<Habitacion> listarPorTipo(int idTipo) {
        List<Habitacion> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM habitacion WHERE id_tipo_habitacion = ? ORDER BY numero_habitacion")) {
            s.setInt(1, idTipo); try (ResultSet r = s.executeQuery()) { while (r.next()) items.add(mapear(r)); }
            return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar las habitaciones por tipo", e); }
    }

    private List<Habitacion> listar(String sql, String value) {
        Objects.requireNonNull(value, "El filtro no puede ser null"); List<Habitacion> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, value); try (ResultSet r = s.executeQuery()) { while (r.next()) items.add(mapear(r)); }
            return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar las habitaciones", e); }
    }

    private Habitacion buscarUno(String sql, Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id); try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar la habitación", e); }
    }

    @Override public ArrayList<Habitacion> listarTodos() {
        ArrayList<Habitacion> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM habitacion ORDER BY numero_habitacion"); ResultSet r = s.executeQuery()) {
            while (r.next()) items.add(mapear(r)); return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar las habitaciones", e); }
    }

    private Habitacion mapear(ResultSet r) throws SQLException {
        return new Habitacion(r.getInt("id_habitacion"), r.getString("numero_habitacion"),
                r.getInt("piso"), r.getString("estado_habitacion"), r.getInt("id_tipo_habitacion"));
    }
}
