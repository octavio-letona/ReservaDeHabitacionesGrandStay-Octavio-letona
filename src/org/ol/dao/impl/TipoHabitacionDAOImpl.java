package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import org.ol.dao.TipoHabitacionDAO;
import org.ol.exception.DBException;
import org.ol.model.TipoHabitacion;
import org.ol.util.Conexion;

public class TipoHabitacionDAOImpl implements TipoHabitacionDAO {
    private static final String INSERTAR = "{call sp_insertartipohabitacion(?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizartipohabitacion(?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminartipohabitacion(?)}";
    private static final String COLUMNAS = "id_tipo_habitacion, nombre_tipo, descripcion, capacidad_personas, tarifa_noche";

    @Override public boolean crear(TipoHabitacion tipo) {
        Objects.requireNonNull(tipo, "El tipo de habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(INSERTAR)) {
            s.setString(1, tipo.getNombreTipo()); s.setString(2, tipo.getDescripcion());
            s.setInt(3, tipo.getCapacidadPersonas()); s.setDouble(4, Objects.requireNonNull(tipo.getTarifaNoche()));
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo crear el tipo de habitación", e); }
    }

    @Override public boolean actualizar(TipoHabitacion tipo) {
        Objects.requireNonNull(tipo, "El tipo de habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ACTUALIZAR)) {
            s.setInt(1, tipo.getIdTipoHabitacion()); s.setString(2, tipo.getNombreTipo());
            s.setString(3, tipo.getDescripcion()); s.setInt(4, tipo.getCapacidadPersonas());
            s.setDouble(5, Objects.requireNonNull(tipo.getTarifaNoche()));
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo actualizar el tipo de habitación", e); }
    }

    @Override public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id del tipo de habitación no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ELIMINAR)) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo eliminar el tipo de habitación", e); }
    }

    @Override public TipoHabitacion buscarPorId(Integer id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM tipo_habitacion WHERE id_tipo_habitacion = ?", id);
    }

    @Override public TipoHabitacion buscarPorNombre(String nombre) {
        Objects.requireNonNull(nombre, "El nombre no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM tipo_habitacion WHERE nombre_tipo = ?")) {
            s.setString(1, nombre); try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar el tipo de habitación", e); }
    }

    private TipoHabitacion buscarUno(String sql, Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id); try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar el tipo de habitación", e); }
    }

    @Override public ArrayList<TipoHabitacion> listarTodos() {
        ArrayList<TipoHabitacion> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM tipo_habitacion ORDER BY id_tipo_habitacion"); ResultSet r = s.executeQuery()) {
            while (r.next()) items.add(mapear(r)); return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar los tipos de habitación", e); }
    }

    private TipoHabitacion mapear(ResultSet r) throws SQLException {
        return new TipoHabitacion(r.getInt("id_tipo_habitacion"), r.getString("nombre_tipo"),
                r.getString("descripcion"), r.getInt("capacidad_personas"), r.getDouble("tarifa_noche"));
    }
}
